package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.EvolutionStatistics
import io.jenetics.stat.DoubleMomentStatistics
import io.jenetics.util.Factory
import io.jenetics.util.Seq
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
import kotlin.streams.asStream
import kotlin.streams.toList

@SpringBootApplication
@ConfigurationPropertiesScan
class MayhemApplication() : ApplicationRunner {

    @Autowired
    lateinit var engine: Engine<IntegerGene, Int>

    override fun run(args: ApplicationArguments?) {
        val statistics: EvolutionStatistics<Int, DoubleMomentStatistics> = EvolutionStatistics.ofNumber()

        val result = engine.stream()
            .limit(50)
            .peek {
                statistics.accept(it)
                println(statistics)
                val weights = it.weights()
                println(weights)
                PrintWriter(FileOutputStream("weights.txt")).use { out -> weights.forEach(out::println) }
            }
            .collect(EvolutionResult.toBestEvolutionResult())
    }

    @Bean
    fun factory() = object : Factory<Genotype<IntegerGene>> {
        private val genotype: Genotype<IntegerGene>

        init {
            BufferedReader(FileReader("weights.txt")).use { reader ->
                val weights = reader.lines().map { IntegerGene.of(it.toInt(), Int.MIN_VALUE, Int.MAX_VALUE) }.toList().chunked(30)
                genotype = Genotype.of(weights.map { IntegerChromosome.of(it)})
            }
        }

        override fun newInstance(): Genotype<IntegerGene> = genotype

        override fun instances(): Stream<Genotype<IntegerGene>> {
            val mutator = GaussianMutator<IntegerGene, Int>(0.01)

            return generateSequence(genotype) { genotype ->
                mutator.alter(Seq.of(Phenotype.of(genotype, 0)), 0).population().first().genotype()
            }.asStream()
        }

        //        Genotype.of(IntegerChromosome.of(-1000, 1000, 30 * 52))
    }

    @Bean
    fun eventLoopGroup() = NioEventLoopGroup()
}

private fun EvolutionResult<IntegerGene, Int>.weights() =
    bestPhenotype().genotype().flatMap { chromosome ->  chromosome.map(IntegerGene::allele)}

fun main(args: Array<String>) {
    runApplication<MayhemApplication>(*args)
}
