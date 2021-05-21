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
                LineCrossover(0.4, 1.1),
                GaussianMutator(0.003)
            )
            .selector(EliteSelector(5, RouletteWheelSelector()))
            .offspringFraction(0.7)
            .build()
    }
}
