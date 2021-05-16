package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.util.Factory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EngineConfiguration {

    @Bean
    fun engine(evaluator: MayhemEvaluator, factory: Factory<Genotype<IntegerGene>>): Engine<IntegerGene, Int> {

        return Engine.Builder(evaluator, factory)
            .populationSize(20)
//            .alterers(
//                GaussianMutator(),
//                MultiPointCrossover(2)
//            )
            .build()
    }
}