package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.util.Factory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfiguration {

    @Bean
    fun engine(
        referenceBot: ReferenceBot,
        evaluator: MayhemEvaluator,
        factory: Factory<Genotype<IntegerGene>>
    ): Engine<IntegerGene, Int> =
//      Engine.builder(referenceBot::fitness, factory)
        Engine.Builder(evaluator, factory)
            .populationSize(180)
            .alterers(
                MultiPointCrossover(0.2, 2),
                LineCrossover(0.3, 1.05),
                GaussianMutator(0.003),
                FactorMutator(0.01, 0.98),
                FactorMutator(0.01, 1.02),
                AddMutator(0.1, 1),
                AddMutator(0.1, -1)
            )
            .selector(EliteSelector(5, TournamentSelector()))
//            .optimize(Optimize.MINIMUM)
            .build()
}

