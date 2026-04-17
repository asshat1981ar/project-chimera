package com.chimera.core.engine

import kotlin.random.Random

/**
 * Lightweight ritual duel rules engine.
 * Stance-based rock-paper-scissors with omen resources and resolve attrition.
 */
class DuelEngine(
    val playerName: String,
    val opponentName: String,
    opponentResolve: Int = 3,
    playerModifier: Float = 0f, // from prior dialogue choices
    private val rng: Random = Random.Default
) {
    enum class Stance(val label: String, val beats: String) {
        STRIKE("Strike", "Feint"),
        WARD("Ward", "Strike"),
        FEINT("Feint", "Ward")
    }

    enum class RoundOutcome { WIN, LOSE, DRAW }

    data class DuelState(
        val round: Int = 0,
        val playerOmens: Int = 2,
        val opponentResolve: Int = 3,
        val log: List<RoundResult> = emptyList(),
        val isComplete: Boolean = false,
        val playerWon: Boolean? = null
    )

    data class RoundResult(
        val round: Int,
        val playerStance: Stance,
        val opponentStance: Stance,
        val outcome: RoundOutcome,
        val narrative: String
    )

    private var state = DuelState(opponentResolve = opponentResolve)
    private val modifier = playerModifier.coerceIn(-0.3f, 0.3f)

    fun getState() = state

    fun executeRound(playerStance: Stance): RoundResult {
        if (state.isComplete) throw IllegalStateException("Duel is already over")

        val round = state.round + 1
        val opponentStance = chooseOpponentStance()
        val outcome = resolveStances(playerStance, opponentStance)

        var newOmens = state.playerOmens
        var newResolve = state.opponentResolve

        when (outcome) {
            RoundOutcome.WIN -> {
                newResolve--
                newOmens = (newOmens + 1).coerceAtMost(4)
            }
            RoundOutcome.LOSE -> {
                newOmens = (newOmens - 1).coerceAtLeast(0)
            }
            RoundOutcome.DRAW -> {} // no change
        }

        val narrative = buildNarrative(playerStance, opponentStance, outcome, round)
        val isComplete = newResolve <= 0 || newOmens <= 0 || round >= 7
        val playerWon = when {
            newResolve <= 0 -> true
            newOmens <= 0 -> false
            round >= 7 -> newResolve < state.opponentResolve // partial progress counts
            else -> null
        }

        val result = RoundResult(round, playerStance, opponentStance, outcome, narrative)
        state = state.copy(
            round = round,
            playerOmens = newOmens,
            opponentResolve = newResolve,
            log = state.log + result,
            isComplete = isComplete,
            playerWon = playerWon
        )
        return result
    }

    private fun resolveStances(player: Stance, opponent: Stance): RoundOutcome {
        if (player == opponent) return RoundOutcome.DRAW
        return if (player.beats == opponent.name) RoundOutcome.WIN else RoundOutcome.LOSE
    }

    private fun chooseOpponentStance(): Stance {
        // Simple AI with slight bias adjustment from dialogue modifier
        val roll = rng.nextFloat() + modifier
        return when {
            roll < 0.33f -> Stance.STRIKE
            roll < 0.66f -> Stance.WARD
            else -> Stance.FEINT
        }
    }

    private fun buildNarrative(player: Stance, opponent: Stance, outcome: RoundOutcome, round: Int): String {
        val escalation = getEscalationPrefix(round)
        val core = when (outcome) {
            RoundOutcome.WIN -> when (player) {
                Stance.STRIKE -> "Your strike cuts through ${opponentName}'s feint. Their resolve wavers."
                Stance.WARD -> "You hold firm against ${opponentName}'s blow. The impact rebounds."
                Stance.FEINT -> "Your deception catches ${opponentName} off-guard. An opening appears."
            }
            RoundOutcome.LOSE -> when (opponent) {
                Stance.STRIKE -> "${opponentName}'s strike finds its mark. Your omens dim."
                Stance.WARD -> "${opponentName} reads your move and turns it aside."
                Stance.FEINT -> "${opponentName}'s feint draws you out of position."
            }
            RoundOutcome.DRAW -> "You mirror each other's stance. The ritual holds its breath."
        }
        return if (escalation.isNotEmpty()) "$escalation $core" else core
    }

    private fun getEscalationPrefix(round: Int): String {
        return when (round) {
            1 -> ""
            2 -> "You test each other's measure."
            3 -> "The ritual crackles with building energy."
            4 -> "Sweat beads on both brows. The air grows heavy."
            5 -> "The stones beneath your feet hum with power."
            6 -> "One of you will break. The ritual demands it."
            7 -> "This is the final exchange. Everything rides on this moment."
            else -> ""
        }
    }
}
