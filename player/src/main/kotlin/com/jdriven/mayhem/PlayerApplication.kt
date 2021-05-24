package com.jdriven.mayhem

import com.jdriven.mayhem.game.MayhemClient
import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.EvolutionStatistics
import io.jenetics.stat.DoubleMomentStatistics
import io.jenetics.util.Factory
import io.jenetics.util.ISeq
import io.netty.channel.nio.NioEventLoopGroup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.FileReader
import java.io.PrintWriter
import java.util.stream.Stream
import kotlin.streams.toList

@SpringBootApplication
@ConfigurationPropertiesScan
class PlayerApplication() : ApplicationRunner {

    @Autowired
    lateinit var client: MayhemClient

    override fun run(args: ApplicationArguments?) {
        val genotype: Genotype<IntegerGene>

        val chromosomeLength = 5
        val min = -1500
        val max = 1500

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
