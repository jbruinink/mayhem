package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.util.Factory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfiguration {

    @Bean
    fun engine(evaluator: MayhemEvaluator, factory: Factory<Genotype<IntegerGene>>): Engine<IntegerGene, Float> {

        return Engine.Builder(evaluator, factory)
            .populationSize(200)
            .alterers(
                MultiPointCrossover(0.01, 2),
                GaussianMutator(0.02)
            )
            .selector(EliteSelector(5, ExponentialRankSelector(0.9)))
            .offspringFraction(0.8)
            .build()
    }
}
