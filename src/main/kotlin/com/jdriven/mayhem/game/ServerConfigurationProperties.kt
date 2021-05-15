package com.jdriven.mayhem.game

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.InetAddress

@ConfigurationProperties("mayhem.server")
@ConstructorBinding
data class ServerConfigurationProperties(
    val address: InetAddress,
    val port: Int
)
