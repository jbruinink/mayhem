package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.internal.math.Probabilities
import java.util.*

class FactorMutator(p: Double, private val factor:Double) : Mutator<IntegerGene, Long>(p) {

    override fun mutate(
        genotype: Genotype<IntegerGene>?,
        p: Double,
        random: Random
    ): MutatorResult<Genotype<IntegerGene>> {
        val P = Probabilities.toInt(p)
        return if(random.nextInt() < P) {
            super.mutate(genotype, 1.0, random)
        } else {
            super.mutate(genotype, p, random)
        }
    }

    override fun mutate(
        chromosome: Chromosome<IntegerGene>?,
        p: Double,
        random: Random?
    ): MutatorResult<Chromosome<IntegerGene>> {
        return super.mutate(chromosome, 1.0, random)
    }

    override fun mutate(gene: IntegerGene, random: Random?): IntegerGene {
        return gene.newInstance((gene.doubleValue() * factor).coerceIn(gene.min().toDouble(), gene.max().toDouble()))
    }
}
