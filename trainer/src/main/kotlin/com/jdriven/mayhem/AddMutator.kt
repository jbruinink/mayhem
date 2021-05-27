package com.jdriven.mayhem

import io.jenetics.*
import io.jenetics.util.Seq
import java.util.*

class AddMutator(p: Double, private val delta: Int) : Mutator<IntegerGene, Long>(p) {
    override fun mutate(
        phenotype: Phenotype<IntegerGene, Long>?,
        generation: Long,
        p: Double,
        random: Random?
    ): MutatorResult<Phenotype<IntegerGene, Long>> {
        return super.mutate(phenotype, generation, 1.0, random)
    }

    override fun mutate(
        genotype: Genotype<IntegerGene>?,
        p: Double,
        random: Random?
    ): MutatorResult<Genotype<IntegerGene>> {
        return super.mutate(genotype, 1.0, random)
    }

    override fun mutate(
        chromosome: Chromosome<IntegerGene>?,
        p: Double,
        random: Random?
    ): MutatorResult<Chromosome<IntegerGene>> {
        return super.mutate(chromosome, probability(), random)
    }

    override fun mutate(gene: IntegerGene, random: Random): IntegerGene {
        return gene.newInstance(gene.allele() + delta)
    }
}
