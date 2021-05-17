package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.Hero
import ninja.robbert.mayhem.api.StatusMessage

interface ActionCreatorFactory {
    fun getActionExecutor(statusMessage: StatusMessage, state: List<Int>): ActionCreator?
}

abstract class ActionCreator(val player: Int, val score: Int) {
    abstract fun getAction(): Action?
}

class ActualActionCreatorFactory(
    private val player: Player,
    private val skill: Skill,
    private val targetTeam: Team,
    private val targetPlayer: Player,
    private val weights: List<Int>
) : ActionCreatorFactory {

    override fun getActionExecutor(statusMessage: StatusMessage, state: List<Int>): ActionCreator? {
        val hero = statusMessage.you.firstOrNull { it.isAlive && it.name.equals(player.text) }
        val target = targetTeam(statusMessage).firstOrNull { it.isAlive && it.name.equals(targetPlayer.text) }
        return if (hero != null && target != null) {
            val score = state.zip(weights).sumOf { (a, b) -> a * b }
            val skill = hero.skills.first { it.name == skill.text }

            object : ActionCreator(hero.id, score) {
                override fun getAction(): Action {
                    return Action(hero.id, skill, target.id, Effects.of(skill, target))
                }
            }
        } else {
            null
        }
    }

    private fun targetTeam(statusMessage: StatusMessage) = when (targetTeam) {
        Team.OPPONENT -> statusMessage.opponent
        Team.YOU -> statusMessage.you
    }
}

data class Effects(val health: Int, val power: Int, val resistanceMs: Int, val armorMs: Int, val healthMs: Int) {

    operator fun plus(other: Effects) = Effects(
        health = health + other.health ,
        power = power + other.power,
        resistanceMs = resistanceMs + other.resistanceMs,
        armorMs = armorMs + other.armorMs,
        healthMs = healthMs + other.healthMs
    )

    companion object {
        fun of(skill: Hero.Skill, target: Hero) =
            Effects(
                health = if (skill.duration == 0) {
                    actualEffect(skill, target, Hero.Skill.EffectType.health, Hero::getHealth, Hero::getMaxHealth)
                } else {
                    0
                },
                power = skill.power + actualEffect(
                    skill,
                    target,
                    Hero.Skill.EffectType.power,
                    Hero::getPower,
                    Hero::getMaxPower
                ),
                resistanceMs = skill.duration * actualEffect(
                    skill,
                    target,
                    Hero.Skill.EffectType.resistance,
                    Hero::getResistance
                ) { 100 },
                armorMs = skill.duration * actualEffect(
                    skill,
                    target,
                    Hero.Skill.EffectType.armor,
                    Hero::getArmor
                ) { 100 },
                healthMs = skill.duration * actualEffect(
                    skill,
                    target,
                    Hero.Skill.EffectType.health,
                    Hero::getHealth,
                    Hero::getMaxHealth
                )
            )

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
                skill.type == Hero.Skill.EffectType.resistance || skill.type == Hero.Skill.EffectType.armor -> skill.effect * -1
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
    }
}

class NoActionCreatorFactory(private val player: Player, private val weights: List<Int>) :
    ActionCreatorFactory {
    override fun getActionExecutor(statusMessage: StatusMessage, state: List<Int>): ActionCreator {
        val score = state.zip(weights).sumOf { (a, b) -> a * b }
        return object : ActionCreator(statusMessage.you.first { it.name == player.text }.id, score) {
            override fun getAction(): Action? = null
        }
    }
}
