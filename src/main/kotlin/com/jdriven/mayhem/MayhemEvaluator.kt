package com.jdriven.mayhem

import com.jdriven.mayhem.game.MayhemClient
import io.jenetics.IntegerGene
import io.jenetics.Phenotype
import io.jenetics.engine.Evaluator
import io.jenetics.util.ISeq
import io.jenetics.util.Seq
import org.springframework.stereotype.Component

@Component
class MayhemEvaluator(
    private val client: MayhemClient
) : Evaluator<IntegerGene, Int> {

    override fun eval(population: Seq<Phenotype<IntegerGene, Int>>): ISeq<Phenotype<IntegerGene, Int>> {
        val futures = population.map { phenotype -> client.play(GeneticGameStrategy(phenotype.genotype())) }.toList()

        return ISeq.of(population.zip(futures).map { (phenotype, future) ->
            val gameResult = future.get()
            phenotype.withFitness(gameResult.wins - gameResult.errors / 5000)
        })
    }
}