package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.Hero
import ninja.robbert.mayhem.api.Hero.Skill.EffectType.*
import ninja.robbert.mayhem.api.StatusMessage
import kotlin.math.abs

class ActionFactory(
    private val player: Player,
    private val skill: Skill,
    private val targetTeam: Team,
    private val targetPlayer: Player,
    private val weights: List<Int>
) {

    fun getAction(statusMessage: StatusMessage): Action? {
        val hero = statusMessage.you.first { it.name.equals(player.text) }
        val target = targetTeam(statusMessage).first { it.name.equals(targetPlayer.text) }
        val skill = hero.skills.first { it.name == skill.text }

        if (!hero.isAlive || hero.isBusy || hero.power + skill.power < 0 || hero.cooldowns.contains(skill.id) ||
            !target.isAlive || target.buffs.contains(skill.name)
        ) {
            return null
        }

        val state = mapState(skill, target)

        val score = state.zip(weights).sumOf { (a, b) -> a * b }

        return Action(hero.id, skill, target.id, score)
    }

    private fun mapState(skill: Hero.Skill, target: Hero): List<Int> {
        val bias = 500
        val (effect, expectedKill) = normalizedEffect(skill, target)

        return listOf(
            bias,
            effect,
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
            skill.effect
        } else {
            skill.effect * -1 * (200 - target.resistance) * (100 - target.armor) / (200 * 100)
        }

        return if (skill.effect > 0) {
            //can't give more than max - current
            Pair(effect.coerceAtMost(max - current), 0)
        } else {
            //can't take more than current
            Pair(
                effect.coerceAtMost(current) * 1000 / abs(skill.effect), if (effect > target.health) {
                    1000
                } else {
                    0
                }
            )
        }
    }

    private fun targetTeam(statusMessage: StatusMessage) = when (targetTeam) {
        Team.OPPONENT -> statusMessage.opponent
        Team.YOU -> statusMessage.you
    }
}
