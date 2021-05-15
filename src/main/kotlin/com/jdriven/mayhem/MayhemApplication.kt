package com.jdriven.mayhem

import io.jenetics.Genotype
import io.jenetics.IntegerChromosome
import io.jenetics.IntegerGene
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.EvolutionStatistics
import io.jenetics.stat.DoubleMomentStatistics
import io.jenetics.util.Factory
import io.netty.channel.nio.NioEventLoopGroup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.io.*
import kotlin.streams.toList

@SpringBootApplication
@ConfigurationPropertiesScan
class MayhemApplication() : ApplicationRunner {

    @Autowired
    lateinit var engine: Engine<IntegerGene, Int>

    override fun run(args: ApplicationArguments?) {
        val statistics: EvolutionStatistics<Int, DoubleMomentStatistics> = EvolutionStatistics.ofNumber()

        val result = engine.stream()
            .limit(3)
            .peek(statistics)
            .collect(EvolutionResult.toBestEvolutionResult())

        val weights = result.bestPhenotype().genotype().chromosome().map(IntegerGene::allele)
        PrintWriter(FileOutputStream("weights.txt")).use { out -> weights.forEach(out::println) }

        println(statistics)
        println(weights)
    }

    @Bean
    fun factory() = Factory<Genotype<IntegerGene>> {
        BufferedReader(FileReader("weights.txt")).use { reader ->
            val genes = reader.lines().map { IntegerGene.of(it.toInt(), -1000, 1000) }.toList()
            Genotype.of(IntegerChromosome.of(genes))
    }
//        Genotype.of(IntegerChromosome.of(-1000, 1000, 30 * 52))
    }

    @Bean
    fun eventLoopGroup() = NioEventLoopGroup()
}

fun main(args: Array<String>) {
    runApplication<MayhemApplication>(*args)
}
