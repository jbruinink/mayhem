package com.jdriven.mayhem.game

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import ninja.robbert.mayhem.api.StatusMessage
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

//@Component
class MayhemClient(
    private val serverConfig: ServerConfigurationProperties,
    private val eventLoopGroup: EventLoopGroup,
    private val objectMapper: ObjectMapper,
    private val accountGenerator: AccountGenerator
) {
    fun play(strategy: GameStrategy): CompletableFuture<GameResult> {
        val futureResult = CompletableFuture<GameResult>()
        val protocolHandler = MayhemProtocolHandler(strategy, accountGenerator.generate(), futureResult::complete)

        val bootstrap = Bootstrap()
        bootstrap.group(eventLoopGroup)
        bootstrap.channel(NioSocketChannel::class.java)
        bootstrap.remoteAddress(serverConfig.address, serverConfig.port)
        bootstrap.handler(object : ChannelInitializer<NioSocketChannel>() {
            override fun initChannel(ch: NioSocketChannel) {
                ch.pipeline().addLast(
//                    LoggingHandler(LogLevel.INFO),
                    LineBasedFrameDecoder(Int.MAX_VALUE),
                    LineBasedFrameEncoder(),
                    MayhemMessageCodec(objectMapper),
                    protocolHandler
                )
            }
        })
        bootstrap.connect()

        return futureResult
    }
}