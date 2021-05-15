package com.jdriven.mayhem

import com.jdriven.mayhem.game.*
import io.netty.channel.ChannelHandlerContext
import ninja.robbert.mayhem.api.*

class ScoredActionStrategy(val allWeights: List<Int>) : GameStrategy {

    private val actionExecutorFactories: List<ActionExecutorFactory> = Skill.values().flatMap { skill ->
        Team.values().flatMap { team ->
            Player.values().filter { target -> skill.targetFilter(team, target) }
                .map { target ->
                    { weights: List<Int> -> ActualActionExecutorFactory(skill.player, skill, team, target, weights) }
                }
        }
    }
        .plus(Player.values().map { player ->
            { weights: List<Int> -> NoActionExecutorFactory(player, weights) }
        }).zip(allWeights.chunked(30))
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
