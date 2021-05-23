package com.jdriven.mayhem

import com.fasterxml.jackson.databind.ObjectMapper
import com.jdriven.mayhem.game.ServerConfigurationProperties
import io.netty.channel.nio.NioEventLoopGroup
import ninja.robbert.mayhem.api.OutputMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@ConfigurationPropertiesScan
class ServerApplication() : ApplicationRunner {
    @Autowired
    lateinit var mayhemServer: MayhemServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var serverConfigurationProperties: ServerConfigurationProperties

    override fun run(args: ApplicationArguments?) {
        mayhemServer.start()
        eventLoopGroup().terminationFuture().await()
    }

    @Bean
    fun eventLoopGroup() = NioEventLoopGroup()
}

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
