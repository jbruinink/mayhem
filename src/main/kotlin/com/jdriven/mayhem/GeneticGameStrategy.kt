package com.jdriven.mayhem

import com.jdriven.mayhem.game.*
import io.jenetics.Chromosome
import io.jenetics.Genotype
import io.jenetics.IntegerGene
import io.netty.channel.ChannelHandlerContext
import ninja.robbert.mayhem.api.*

class GeneticGameStrategy(genotype: Genotype<IntegerGene>) : GameStrategy {

    private val actionExecutorFactories: List<ActionExecutorFactory> = Skill.values().flatMap { skill ->
        Team.values().flatMap { team ->
            Player.values().filter { target -> skill.targetFilter(team, target) }
                .map { target ->
                    { chromosome: Chromosome<IntegerGene> -> ActualActionExecutorFactory(skill.player, skill, team, target, chromosome.map {it.allele()}) }
                }
        }
    }
        .plus(Player.values().map { player ->
            { chromosome: Chromosome<IntegerGene> -> NoActionExecutorFactory(player, chromosome.map { it.allele() }) }
        }).zip(genotype)
        .map { (a, b) -> a.invoke(b) }

    override fun handle(msg: StatusMessage, ctx: ChannelHandlerContext) {
        if (msg.status == StatusMessage.FightStatus.fighting || msg.status == StatusMessage.FightStatus.overtime) {
            val state = msg.toStateList()
            actionExecutorFactories
                .mapNotNull { it.getActionExecutor(msg, state) }
                .groupBy { candidate -> candidate.player }
                .mapNotNull { (_, v) ->
                    v.maxByOrNull { it.score }?.execute(ctx)
                }
        } else {
            listOf()
        }
    }
}

private fun StatusMessage.toStateList(): List<Int> = listOf(you, opponent).flatMap { toStateList(it) }

private fun toStateList(heroes: List<Hero>) = heroes.sortedBy { it.name }.flatMap {
    listOf(
        normalize(it.maxHealth, it.health),
        normalize(it.maxPower, it.power),
        normalize(100, it.armor),
        normalize(6, it.currentSkill),
        normalize(100, it.resistance)
    )
}

private fun normalize(max: Int, value: Int) = (value * 1000) / max
