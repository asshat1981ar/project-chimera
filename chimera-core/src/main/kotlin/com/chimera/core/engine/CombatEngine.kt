package com.chimera.core.engine

import kotlin.random.Random

/**
 * CombatEngine — Chimera ADR-001 Candidate C implementation.
 *
 * Design: Intent Cards + Single d20 Resolution + 3-segment Resolve bar + 3-roll maximum.
 * Pure engine with no Android or Hilt dependencies. Fully unit-testable.
 *
 * Flow per round:
 *  1. Player selects an [IntentCard] from the available set
 *  2. Engine rolls d20, applies [IntentCard.statBonus] + [opponentModifier]
 *  3. Total mapped to [ResultBand] via fixed thresholds
 *  4. Resolve changes applied, narrative string produced
 *  5. After max [MAX_ROLLS] rounds OR either side reaches 0 Resolve → [CombatState.isComplete]
 */
class CombatEngine(
    val playerName: String,
    val opponentName: String,
    val opponentArchetype: String,
    availableIntents: List<IntentCard>,
    private val opponentModifier: Float = 0f,  // derived from NPC disposition: negative = opponent weaker
    private val rng: Random = Random.Default
) {
    companion object {
        const val MAX_ROLLS      = 3
        const val MAX_RESOLVE    = 3
        private const val CRIT_THRESHOLD   = 20
        private const val SUCCESS_THRESHOLD = 15
        private const val PARTIAL_THRESHOLD = 10
        private const val FAIL_THRESHOLD    = 5
    }

    enum class ResultBand(val label: String) {
        CRITICAL_SUCCESS("Critical success"),
        SUCCESS("Success"),
        PARTIAL("Partial success"),
        FAILURE("Failure"),
        CRITICAL_FAILURE("Critical failure")
    }

    data class IntentCard(
        val id: String,
        val label: String,
        val description: String,
        val statBonus: Int,
        val requiresDispositionAbove: Float? = null,
        val resultBands: Map<String, String> = emptyMap()
    )

    data class RollResult(
        val roll: Int,          // raw d20 result (1–20)
        val modifier: Int,      // statBonus applied
        val total: Int,         // roll + modifier (may exceed 20)
        val band: ResultBand,
        val narrative: String
    )

    data class CombatState(
        val rollCount: Int = 0,
        val playerResolve: Int = MAX_RESOLVE,
        val opponentResolve: Int = MAX_RESOLVE,
        val lastResult: RollResult? = null,
        val log: List<RollResult> = emptyList(),
        val isComplete: Boolean = false,
        val playerWon: Boolean? = null
    )

    val intents: List<IntentCard> = availableIntents
    private var state = CombatState()

    fun getState(): CombatState = state

    /**
     * Execute one combat round with the selected [intent].
     * Returns the [RollResult] and updates internal [CombatState].
     * Throws [IllegalStateException] if called after combat is complete.
     */
    fun executeRound(intent: IntentCard): RollResult {
        check(!state.isComplete) { "Combat is already over" }

        val roll    = rng.nextInt(1, 21)
        val modVal  = intent.statBonus
        val total   = (roll + modVal + (opponentModifier * 5).toInt()).coerceIn(1, 25)
        val band    = resolveBand(total)
        val narrative = resolveNarrative(intent, band)

        var playerResolve   = state.playerResolve
        var opponentResolve = state.opponentResolve

        when (band) {
            ResultBand.CRITICAL_SUCCESS -> opponentResolve -= 2
            ResultBand.SUCCESS          -> opponentResolve -= 1
            ResultBand.PARTIAL          -> { playerResolve -= 1 }
            ResultBand.FAILURE          -> playerResolve -= 1
            ResultBand.CRITICAL_FAILURE -> playerResolve -= 2
        }
        playerResolve   = playerResolve.coerceAtLeast(0)
        opponentResolve = opponentResolve.coerceAtLeast(0)

        val rollCount  = state.rollCount + 1
        val isComplete = playerResolve <= 0 || opponentResolve <= 0 || rollCount >= MAX_ROLLS
        val playerWon  = when {
            opponentResolve <= 0 && playerResolve > 0 -> true
            playerResolve <= 0   -> false
            rollCount >= MAX_ROLLS -> opponentResolve < state.opponentResolve  // net damage dealt
            else -> null
        }

        val result = RollResult(
            roll       = roll,
            modifier   = modVal,
            total      = total,
            band       = band,
            narrative  = narrative
        )

        state = state.copy(
            rollCount       = rollCount,
            playerResolve   = playerResolve,
            opponentResolve = opponentResolve,
            lastResult      = result,
            log             = state.log + result,
            isComplete      = isComplete,
            playerWon       = playerWon
        )
        return result
    }

    private fun resolveBand(total: Int): ResultBand = when {
        total >= CRIT_THRESHOLD   -> ResultBand.CRITICAL_SUCCESS
        total >= SUCCESS_THRESHOLD -> ResultBand.SUCCESS
        total >= PARTIAL_THRESHOLD -> ResultBand.PARTIAL
        total >= FAIL_THRESHOLD    -> ResultBand.FAILURE
        else                       -> ResultBand.CRITICAL_FAILURE
    }

    private fun resolveNarrative(intent: IntentCard, band: ResultBand): String {
        val bandKey = when (band) {
            ResultBand.CRITICAL_SUCCESS -> "critical"
            ResultBand.SUCCESS          -> "success"
            ResultBand.PARTIAL          -> "partial"
            ResultBand.FAILURE          -> "failure"
            ResultBand.CRITICAL_FAILURE -> "criticalFailure"
        }
        return intent.resultBands[bandKey]
            ?: generateFallbackNarrative(intent, band)
    }

    private fun generateFallbackNarrative(intent: IntentCard, band: ResultBand): String {
        val subject = opponentName
        return when (band) {
            ResultBand.CRITICAL_SUCCESS -> "$subject reels. A decisive opening."
            ResultBand.SUCCESS          -> "The intent lands cleanly. $subject loses ground."
            ResultBand.PARTIAL          -> "A glancing blow — but you absorb the counter."
            ResultBand.FAILURE          -> "$subject turns your effort aside."
            ResultBand.CRITICAL_FAILURE -> "An overreach. $subject exploits the gap."
        }
    }
}
