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

        val state = mapState(hero, skill, target, statusMessage.timestamp.time)

        val score = state.zip(weights).sumOf { (a, b) -> a * b }

        return Action(hero.id, skill, target.id, score)
    }

    private fun mapState(hero: Hero, skill: Hero.Skill, target: Hero, time: Long): List<Int> {
        val bias = 50
        val effect = normalizedEffect(skill, target)
        val buff = target.buffs[skill.name]
        val durationLeft = if (skill.duration > 0 && buff != null) {
            (buff.timeout - time - skill.delay).toInt().coerceAtLeast(0) ?: 0
        } else {
            0
        } / 100

        return listOf(
            bias,
            effect,
            durationLeft,
            target.health / 6,
            hero.power / 3
        )
    }

    private fun normalizedEffect(skill: Hero.Skill, target: Hero): Int {

        val (current: Int, max: Int) = when (skill.type) {
            health -> if (skill.duration > 0) {
                Pair(target.maxHealth, target.maxHealth + 100)
            } else {
                Pair(target.health, target.maxHealth)
            }
            power -> Pair(target.power, target.maxPower)
            armor -> Pair(target.armor, 100)
            resistance -> Pair(target.resistance, 100)
        }

        val effect =
            if (skill.effect > 0 || skill.type == resistance || skill.type == armor || skill.duration > 0) {
                abs(skill.effect)
            } else {
                skill.effect * -1 * (200 - target.resistance) * (100 - target.armor) / (200 * 100)
            }

        val actualEffect = if (skill.effect > 0) {
            //can't give more than max - current
            effect.coerceAtMost(max - current)
        } else {
            //can't take more than current
            effect.coerceAtMost(current) * 100 / abs(skill.effect)
        }

        return actualEffect
    }

    private fun targetTeam(statusMessage: StatusMessage) = when (targetTeam) {
        Team.OPPONENT -> statusMessage.opponent
        Team.YOU -> statusMessage.you
    }
}
