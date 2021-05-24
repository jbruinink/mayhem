package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.LineBasedFrameEncoder
import com.jdriven.mayhem.game.ServerConfigurationProperties
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import ninja.robbert.mayhem.api.OutputMessage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ServerChannelInitializer(
    private val objectMapper: ObjectMapper,
    private val eventLoopGroup: EventLoopGroup,
    private val serverConfig: ServerConfigurationProperties
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().addLast(
            LoggingHandler(LogLevel.INFO),
            LineBasedFrameDecoder(Int.MAX_VALUE),
            LineBasedFrameEncoder(),
            MayhemServerMessageCodec(objectMapper),
            GameRecorder(objectMapper),
            ServerProtocolHandler(objectMapper, eventLoopGroup, serverConfig)
        )
    }
}