package com.jdriven.mayhem

import com.jdriven.mayhem.game.GeneticGameStrategy
import com.jdriven.mayhem.game.MayhemClient
import io.jenetics.*
import io.netty.channel.nio.NioEventLoopGroup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.BufferedReader
import java.io.FileReader
import kotlin.streams.toList

@SpringBootApplication
@ConfigurationPropertiesScan
class PlayerApplication() : ApplicationRunner {

    @Autowired
    lateinit var client: MayhemClient

    override fun run(args: ApplicationArguments?) {
        val genotype: Genotype<IntegerGene>

        val chromosomeLength = 5
        val min = -2000
        val max = 2000

        BufferedReader(FileReader("bestweights.txt")).use { reader ->
            val weights = reader.lines().map { IntegerGene.of(it.toInt(), min, max) }
                .toList()
                .chunked(chromosomeLength)
            genotype = Genotype.of(weights.map { IntegerChromosome.of(it) })
        }

        while (true) {
            client.play(GeneticGameStrategy(genotype)).join()
        }
    }

    @Bean
    fun eventLoopGroup() = NioEventLoopGroup()
}

fun main(args: Array<String>) {
    runApplication<PlayerApplication>(*args)
}
