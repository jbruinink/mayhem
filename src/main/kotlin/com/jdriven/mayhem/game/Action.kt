package com.jdriven.mayhem.game

import ninja.robbert.mayhem.api.Hero

data class Action(
    val heroId: Int,
    val skill: Hero.Skill,
    val targetId: Int,
    val score: Int
)
