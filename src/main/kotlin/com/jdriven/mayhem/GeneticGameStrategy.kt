package com.jdriven.mayhem

import com.jdriven.mayhem.game.*
import io.jenetics.Genotype
import io.jenetics.IntegerGene
import ninja.robbert.mayhem.api.*

class GeneticGameStrategy(genotype: Genotype<IntegerGene>) : GameStrategy {

    private val actionFactories = Skill.values().flatMap { skill ->
        Team.values().flatMap { team ->
            Player.values().filter { target -> skill.targetFilter(team, target) }
                .zip(genotype) { target, chromosome ->
                    ActionFactory(
                        skill.player,
                        skill,
                        team,
                        target,
                        chromosome.map { it.allele() })
                }
        }
    }

    override fun createResponse(msg: StatusMessage): Collection<Action> {
        return if (msg.status == StatusMessage.FightStatus.fighting || msg.status == StatusMessage.FightStatus.overtime) {
            actionFactories
                .mapNotNull { it.getAction(msg) }
                .filter { it.score > 0 }
                .groupBy { candidate -> candidate.heroId }
                .mapNotNull { (_, v) ->
                    v.maxByOrNull { it.score }
                }
        } else {
            listOf()
        }
    }
}
