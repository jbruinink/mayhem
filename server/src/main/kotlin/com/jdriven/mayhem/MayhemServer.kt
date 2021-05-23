package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.ServerConfigurationProperties
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.EventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.springframework.stereotype.Component

@Component
class MayhemServer(
    private val eventLoopGroup: EventLoopGroup,
    private val objectMapper: ObjectMapper,
    private val serverConfig: ServerConfigurationProperties
) {
    fun start() {
        val bootstrap = ServerBootstrap()
        bootstrap.group(eventLoopGroup)
        bootstrap.channel(NioServerSocketChannel::class.java)
        bootstrap.childHandler(ServerChannelInitializer(objectMapper, eventLoopGroup, serverConfig))
        bootstrap.bind(1337)
    }
}