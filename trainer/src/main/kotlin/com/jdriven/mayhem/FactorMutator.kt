package com.jdriven.mayhem

import io.jenetics.*
import java.util.*

class FactorMutator(p: Double, private val factor:Double) : Mutator<IntegerGene, Long>(p) {
    override fun mutate(
        chromosome: Chromosome<IntegerGene>?,
        p: Double,
        random: Random?
    ): MutatorResult<Chromosome<IntegerGene>> {
        return super.mutate(chromosome, 1.0, random)
    }

    override fun mutate(gene: IntegerGene, random: Random?): IntegerGene {
        return gene.newInstance(gene.doubleValue() * factor)
    }
}
