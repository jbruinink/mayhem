package com.jdriven.mayhem.game

import com.jdriven.mayhem.game.Player.*
import com.jdriven.mayhem.game.Team.OPPONENT
import com.jdriven.mayhem.game.Team.YOU

enum class Skill(
    val player: Player,
    val text: String,
    val targetFilter: (targetTeam: Team, targetPlayer: Player) -> Boolean
) {
    YAMLIZE(CI_CD_GOD, "yamlize", opponent()),
    CONTAINERIZE(CI_CD_GOD, "containerize", opponent()),
    SVN2GIT(CI_CD_GOD, "svn2git", opponent()),
    INFRAASCODE(CI_CD_GOD, "infraascode", ally()),
    MULTICLOUD(CI_CD_GOD, "multicloud", opponent()),
    GREENFIELD(CI_CD_GOD, "greenfield", one(CI_CD_GOD)),

    COFFEE(J_HIPSTER, "coffee", ally()),
    YOGACLASS(J_HIPSTER, "yogaclass", ally()),
    GLASSES(J_HIPSTER, "glasses", ally()),
    MEDITATION(J_HIPSTER, "meditation", one(J_HIPSTER)),
    SVELTE(J_HIPSTER, "svelte", ally()),
    WET(J_HIPSTER, "wet", opponent()),
    KOTLIN(J_HIPSTER, "kotlin", ally()),

    DUST_MAINFRAME(LEGACY_DUSTER, "dust mainframe", notLegacyDuster()),
    COBOL_COMPILED(LEGACY_DUSTER, "COBOL compiled", notLegacyDuster()),
    PL_SQL_HELL(LEGACY_DUSTER, "PL/SQL Hell", opponent()),
    CODE_HANDOVER(LEGACY_DUSTER, "code handover", opponent()),
    REBOOT(LEGACY_DUSTER, "reboot", one(LEGACY_DUSTER)),
    EJB(LEGACY_DUSTER, "ejbejbejb", opponent())
}

private fun opponent() = { targetTeam: Team, _: Player -> targetTeam == OPPONENT }

private fun ally() = { targetTeam: Team, _: Player -> targetTeam == YOU }

private fun one(player: Player) =
    { targetTeam: Team, candidate: Player -> targetTeam == YOU && candidate.name == player.name }

private fun notLegacyDuster() =
    { targetTeam: Team, candidate: Player -> targetTeam == YOU && candidate.name != LEGACY_DUSTER.name }

