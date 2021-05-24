package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Codecs
import io.jenetics.engine.Engine
import io.jenetics.util.Factory
import io.jenetics.util.ISeq
import io.jenetics.util.Seq
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfiguration {

    @Bean
    fun engine(referenceBot: ReferenceBot,
               factory: Factory<Genotype<IntegerGene>>): Engine<IntegerGene, Int> {

        return Engine.builder(referenceBot::fitness, factory)
            .populationSize(2000)
            .alterers(
                LineCrossover(0.4, 1.1),
                GaussianMutator(0.003)
            )
            .selector(EliteSelector(10, RouletteWheelSelector()))
            .optimize(Optimize.MINIMUM)
            .build()
    }
}
