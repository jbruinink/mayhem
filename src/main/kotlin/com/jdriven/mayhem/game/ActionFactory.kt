package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.Hero
import ninja.robbert.mayhem.api.Hero.Skill.EffectType.*
import ninja.robbert.mayhem.api.StatusMessage

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

        if (!hero.isAlive || hero.isBusy || hero.power + skill.power < 0 || hero.cooldowns.contains(skill.id) || !target.isAlive
        ) {
            return null
        }

        val state = mapState(skill, target)

        val score = state.zip(weights).sumOf { (a, b) -> a * b }

        return Action(hero.id, skill, target.id, score)
    }

    private fun mapState(skill: Hero.Skill, target: Hero): List<Int> {
        val healthEffect = if (skill.duration == 0) {
            actualEffect(skill, target, health, Hero::getHealth, Hero::getMaxHealth)
        } else {
            0
        }
        val myPowerEffect = skill.power
        val bias = 1
        val targetPowerEffect = actualEffect(skill, target, power, Hero::getPower, Hero::getMaxPower)
        val resistanceEffect = actualEffect(skill, target, resistance, Hero::getResistance) { 100 }
        val armorEffect = actualEffect(skill, target, armor, Hero::getArmor) { 100 }
        val maxHealthEffect = if (skill.duration > 0) {
            actualEffect(skill, target, health, { 0 }, { 100 })
        } else {
            0
        }
        val expectedKill = if (skill.effect < 0 && healthEffect > target.health) {
            1
        } else {
            0
        }
        return listOf(
            bias,
            healthEffect,
            myPowerEffect,
            targetPowerEffect,
            resistanceEffect,
            armorEffect,
            maxHealthEffect,
            target.armor,
            target.resistance,
            target.power,
            target.health,
            expectedKill
        )
    }

    private fun actualEffect(
        skill: Hero.Skill,
        target: Hero,
        type: Hero.Skill.EffectType,
        current: (Hero) -> Int,
        max: (Hero) -> Int
    ): Int {
        if (skill.type != type) {
            return 0
        }

        val effect = when {
            skill.effect > 0 -> skill.effect
            skill.type == resistance || skill.type == armor -> skill.effect * -1
            else -> skill.effect * -1 * (200 - target.resistance) * (100 - target.armor) / (200 * 100)
        }
        return if (skill.effect > 0) {
            //can't give more than max - current
            effect.coerceAtMost(max.invoke(target) - current.invoke(target))
        } else {
            //can't take more than current
            effect.coerceAtMost(current.invoke(target))
        }
    }

    private fun targetTeam(statusMessage: StatusMessage) = when (targetTeam) {
        Team.OPPONENT -> statusMessage.opponent
        Team.YOU -> statusMessage.you
    }
}
