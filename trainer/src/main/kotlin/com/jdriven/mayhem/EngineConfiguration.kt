package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.util.Factory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfiguration {

    @Bean
    fun engine(referenceBot: ReferenceBot,
               factory: Factory<Genotype<IntegerGene>>): Engine<IntegerGene, Long> {

        return Engine.builder(referenceBot::fitness, factory)
            .populationSize(2000)
            .alterers(
                MultiPointCrossover(0.3, 2),
                LineCrossover(0.1, 1.05),
                GaussianMutator(0.003),
                FactorMutator(0.01, 0.98),
                FactorMutator(0.01, 1.02),
                AddMutator(0.1, 1)
            )
            .selector(EliteSelector(10, TournamentSelector()))
            .optimize(Optimize.MINIMUM)
            .build()    }
}
