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
            .populationSize(3000)
            .alterers(
                LineCrossover(0.1, 1.05),
                MultiPointCrossover(0.1, 2),
                GaussianMutator(0.003),
                FactorMutator(0.01, 0.99),
                FactorMutator(0.01, 1.01)
            )
            .selector(EliteSelector(10, TournamentSelector()))
            .optimize(Optimize.MINIMUM)
            .build()
    }
}
