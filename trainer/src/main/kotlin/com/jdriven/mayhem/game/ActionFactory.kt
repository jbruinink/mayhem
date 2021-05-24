package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.Hero
import ninja.robbert.mayhem.api.Hero.Skill.EffectType.*
import ninja.robbert.mayhem.api.StatusMessage
import kotlin.math.abs

data class ActionFactory(
    val player: Player,
    val skill: Skill,
    val targetTeam: Team,
    val targetPlayer: Player,
    val weights: List<Int>
) {

    fun getAction(statusMessage: StatusMessage): Action? {
        val hero = statusMessage.you.first { it.name.equals(player.text) }
        val target = targetTeam(statusMessage).first { it.name.equals(targetPlayer.text) }
        val skill = hero.skills.first { it.name == skill.text }

        if (!hero.isAlive || hero.isBusy || hero.power + skill.power < 0 || hero.cooldowns.contains(skill.id) ||
            !target.isAlive
        ) {
            return null
        }

        val state = mapState(skill, target, statusMessage.timestamp.time)

        val score = state.zip(weights).sumOf { (a, b) -> a * b }

        return Action(hero.id, skill, target.id, score)
    }

    private fun mapState(skill: Hero.Skill, target: Hero, time: Long): List<Int> {
        val bias = 50
        val (effect, expectedKill) = normalizedEffect(skill, target)
        val durationLeft: Int = if (skill.duration > 0) {
            target.buffs[skill.name]?.timeout?.minus(time)?.toInt() ?: 0
        } else {
            0
        }

        return listOf(
            bias,
            effect,
            durationLeft,
            (target.health * 1000) / 600,
            expectedKill
        )
    }

    private fun normalizedEffect(skill: Hero.Skill, target: Hero): Pair<Int, Int> {

        val (current: Int, max: Int) = when (skill.type) {
            health -> Pair(target.health, target.maxHealth)
            power -> Pair(target.power, target.maxPower)
            armor -> Pair(target.armor, 100)
            resistance -> Pair(target.resistance, 100)
        }

        val effect = if (skill.effect > 0 || skill.type == resistance || skill.type == armor) {
            abs(skill.effect)
        } else {
            skill.effect * -1 * (200 - target.resistance) * (100 - target.armor) / (200 * 100)
        }

        val actualEffect = if (skill.effect > 0) {
            //can't give more than max - current
            effect.coerceAtMost(max - current)
        } else {
            //can't take more than current
            effect.coerceAtMost(current) * 1000 / abs(skill.effect)
        }

        val expectedKill = if (skill.type == health && skill.effect < 0 && actualEffect >= target.health) {
            1000
        } else {
            0
        }
        return Pair(actualEffect, expectedKill)
    }

    private fun targetTeam(statusMessage: StatusMessage) = when (targetTeam) {
        Team.OPPONENT -> statusMessage.opponent
        Team.YOU -> statusMessage.you
    }
}
