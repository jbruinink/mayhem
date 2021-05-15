package com.jdriven.mayhem.game

import io.netty.channel.ChannelHandlerContext
import ninja.robbert.mayhem.api.ActionMessage
import ninja.robbert.mayhem.api.StatusMessage

interface ActionExecutorFactory {
    fun getActionExecutor(statusMessage: StatusMessage, state: List<Int>): ActionExecutor?
}

abstract class ActionExecutor(val player: Int, val score: Int) {
    abstract fun execute(ctx: ChannelHandlerContext)
}

class ActualActionExecutorFactory(
    private val player: Player,
    private val skill: Skill,
    private val targetTeam: Team,
    private val targetPlayer: Player,
    private val weights: List<Int>
) : ActionExecutorFactory {

    override fun getActionExecutor(statusMessage: StatusMessage, state: List<Int>): ActionExecutor? {
        val hero = statusMessage.you.firstOrNull { it.isAlive && it.name.equals(player.text) }
        val target = targetTeam(statusMessage).firstOrNull { it.isAlive && it.name.equals(targetPlayer.text) }
        if (hero != null && target != null) {
            val score = state.zip(weights).sumOf { (a, b) -> a * b }
            return object : ActionExecutor(hero.id, score) {
                override fun execute(ctx: ChannelHandlerContext) {
                    ctx.writeAndFlush(
                        ActionMessage(
                            hero.id,
                            hero.skills.first { it.name.equals(skill.text) }.id,
                            target.id,
                            false
                        )
                    )
                }
            }
        } else {
            return null
        }
    }

    private fun targetTeam(statusMessage: StatusMessage) = when (targetTeam) {
        Team.OPPONENT -> statusMessage.opponent
        Team.YOU -> statusMessage.you
    }
}

class NoActionExecutorFactory(private val player: Player, private val weights: List<Int>) :
    ActionExecutorFactory {
    override fun getActionExecutor(statusMessage: StatusMessage, state: List<Int>): ActionExecutor {
        val score = state.zip(weights).sumOf { (a, b) -> a * b }
        return object : ActionExecutor(statusMessage.you.first { it.name == player.text }.id, score) {
            override fun execute(ctx: ChannelHandlerContext) {
            }
        }
    }
}
