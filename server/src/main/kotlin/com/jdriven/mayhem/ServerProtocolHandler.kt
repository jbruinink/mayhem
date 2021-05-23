package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.LineBasedFrameEncoder
import com.jdriven.mayhem.game.MayhemClientMessageCodec
import com.jdriven.mayhem.game.ServerConfigurationProperties
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder

class ServerProtocolHandler(
    private val objectMapper: ObjectMapper,
    private val eventLoopGroup: EventLoopGroup,
    private val serverConfig: ServerConfigurationProperties

) : ChannelInboundHandlerAdapter() {
    private var clientChannel: Channel? = null

    override fun channelActive(serverCtx: ChannelHandlerContext) {
        val bootstrap = Bootstrap()
        bootstrap.group(eventLoopGroup)
        bootstrap.channel(NioSocketChannel::class.java)
        bootstrap.remoteAddress(serverConfig.address, serverConfig.gamePort)
        bootstrap.handler(object : ChannelInitializer<NioSocketChannel>() {
            override fun initChannel(ch: NioSocketChannel) {
                ch.pipeline().addLast(
//                    LoggingHandler(LogLevel.INFO),
                    LineBasedFrameDecoder(Int.MAX_VALUE),
                    LineBasedFrameEncoder(),
                    MayhemClientMessageCodec(objectMapper),
                    ProxyProtocolHandler(serverCtx)
                )
            }
        })
        clientChannel = bootstrap.connect().channel()
    }

    override fun channelRead(serverCtx: ChannelHandlerContext?, msg: Any) {
        clientChannel?.writeAndFlush(msg)
    }
}