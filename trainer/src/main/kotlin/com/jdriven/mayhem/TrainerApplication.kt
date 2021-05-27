package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.EvolutionStatistics
import io.jenetics.engine.Limits
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
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Stream
import kotlin.streams.toList

@SpringBootApplication
@ConfigurationPropertiesScan
class TrainerApplication() : ApplicationRunner {

    @Autowired
    lateinit var engine: Engine<IntegerGene, Int>

    override fun run(args: ApplicationArguments?) {
        val statistics: EvolutionStatistics<Int, DoubleMomentStatistics> = EvolutionStatistics.ofNumber()

        val result = engine.stream()
            .peek {
                statistics.accept(it)
                println(statistics)
                val weights = it.genotypes().weights()
                PrintWriter(FileOutputStream("weights.txt")).use { out -> weights.forEach(out::println) }

                val bestWeights = ISeq.of(listOf(it.bestPhenotype().genotype())).weights()
                PrintWriter(FileOutputStream("bestweights.txt")).use { out -> bestWeights.forEach(out::println) }
            }
            .collect(EvolutionResult.toBestEvolutionResult())
    }

    @Bean
    fun factory() = object : Factory<Genotype<IntegerGene>> {
        private val genotypes: List<Genotype<IntegerGene>>

        init {
            val chromosomeLength = 5
            val geneLength = 49
            val min = -2000
            val max = 2000

            BufferedReader(FileReader("weights.txt")).use { reader ->
                val weights = reader.lines().map { IntegerGene.of(it.toInt(), min, max) }
                    .toList()
                    .chunked(chromosomeLength)
                    .chunked(geneLength)
//                val weights = ThreadLocalRandom.current().let { rnd ->
//                    (0..chromosomeLength * geneLength * 2000).map { IntegerGene.of(rnd.nextInt(min, max), min, max) }
//                        .chunked(chromosomeLength)
//                        .chunked(geneLength)
//                }
                genotypes = weights.map { w -> Genotype.of(w.map { IntegerChromosome.of(it) }) }
            }
        }

        override fun newInstance(): Genotype<IntegerGene> = genotypes.first()

        override fun instances(): Stream<Genotype<IntegerGene>> {
            return genotypes.stream()
        }
    }

    @Bean
    fun eventLoopGroup() = NioEventLoopGroup()
}

private fun ISeq<Genotype<IntegerGene>>.weights() =
    flatMap { it.flatMap { chromosome -> chromosome.map(IntegerGene::allele) } }

fun main(args: Array<String>) {
    runApplication<TrainerApplication>(*args)
}
