package com.jdriven.mayhem.game

import com.jdriven.mayhem.game.*
import io.jenetics.Chromosome
import io.jenetics.Genotype
import io.jenetics.IntegerGene
import ninja.robbert.mayhem.api.*

class GeneticGameStrategy(genotype: Genotype<IntegerGene>) : GameStrategy {

    private val actionFactories = Skill.values().flatMap { skill ->
        Team.values().flatMap { team ->
            Player.values().filter { target -> skill.targetFilter(team, target) }
                .map { target ->
                    { chromosome: Chromosome<IntegerGene> ->
                        ActionFactory(
                            skill.player,
                            skill,
                            team,
                            target,
                            chromosome.map { it.allele() })
                    }
                }
        }
    }.zip(genotype) { a, b -> a.invoke(b) }

    fun createUnfilteredResponse(msg: StatusMessage): Map<Int, List<Action>> {
//        val txt = actionFactories.flatMap { f ->
//            f.weights.map { w ->
//                listOf(
//                    w,
//                    f.player.text,
//                    f.skill.name,
//                    f.targetPlayer.text
//                ).joinToString(",")
//            }
//        }.joinToString("\n")

        return if (msg.status == StatusMessage.FightStatus.fighting || msg.status == StatusMessage.FightStatus.overtime) {
            actionFactories
                .mapNotNull { it.getAction(msg) }
                .groupBy { candidate -> candidate.heroId }
        } else {
            emptyMap()
        }
    }

    override fun createResponse(msg: StatusMessage): Collection<Action> {
//        val txt = actionFactories.flatMap { f ->
//            f.weights.map { w ->
//                listOf(
//                    w,
//                    f.player.text,
//                    f.skill.name,
//                    f.targetPlayer.text
//                ).joinToString(",")
//            }
//        }.joinToString("\n")
        return if (msg.status == StatusMessage.FightStatus.fighting || msg.status == StatusMessage.FightStatus.overtime) {
            val actionsByHero = actionFactories
                .mapNotNull { it.getAction(msg) }
                .groupBy { candidate -> candidate.heroId }

            actionsByHero.mapNotNull { (_, v) ->
                v.filter { it.score > 0 }
                    .maxByOrNull { it.score }
            }
        } else {
            listOf()
        }
    }
}
