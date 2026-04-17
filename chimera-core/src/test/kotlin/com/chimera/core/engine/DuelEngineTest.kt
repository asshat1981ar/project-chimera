package com.chimera.core.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DuelEngineTest {

    private fun createEngine(
        opponentResolve: Int = 3,
        playerModifier: Float = 0f,
        rng: Random = Random.Default
    ) = DuelEngine("Player", "Opponent", opponentResolve, playerModifier, rng)

    // nextBits=0 → nextFloat()=0f → roll < 0.33f → opponent picks STRIKE; DRAW vs player STRIKE
    private val drawRng = object : Random() {
        override fun nextBits(bitCount: Int) = 0
    }

    // nextBits all-ones → nextFloat()≈1f → roll ≥ 0.66f → opponent picks FEINT
    private val feintRng = object : Random() {
        override fun nextBits(bitCount: Int) = (1 shl bitCount) - 1
    }

    // nextBits high-bit → nextFloat()=0.5f → roll ∈ [0.33f,0.66f) → opponent picks WARD
    private val wardRng = object : Random() {
        override fun nextBits(bitCount: Int) = 1 shl (bitCount - 1)
    }

    @Test
    fun `initial state has correct defaults`() {
        val engine = createEngine()
        val state = engine.getState()
        assertEquals(0, state.round)
        assertEquals(2, state.playerOmens)
        assertEquals(3, state.opponentResolve)
        assertFalse(state.isComplete)
        assertNull(state.playerWon)
        assertTrue(state.log.isEmpty())
    }

    @Test
    fun `strike beats feint`() {
        // Stance.STRIKE.beats == "Feint"
        val strike = DuelEngine.Stance.STRIKE
        assertEquals("Feint", strike.beats)
    }

    @Test
    fun `ward beats strike`() {
        val ward = DuelEngine.Stance.WARD
        assertEquals("Strike", ward.beats)
    }

    @Test
    fun `feint beats ward`() {
        val feint = DuelEngine.Stance.FEINT
        assertEquals("Ward", feint.beats)
    }

    @Test
    fun `round increments after each execution`() {
        val engine = createEngine()
        engine.executeRound(DuelEngine.Stance.STRIKE)
        assertEquals(1, engine.getState().round)
        engine.executeRound(DuelEngine.Stance.WARD)
        assertEquals(2, engine.getState().round)
    }

    @Test
    fun `win increases omens and decreases opponent resolve`() {
        val engine = createEngine()
        val initial = engine.getState()
        val result = engine.executeRound(DuelEngine.Stance.STRIKE)

        val state = engine.getState()
        if (result.outcome == DuelEngine.RoundOutcome.WIN) {
            assertEquals(initial.opponentResolve - 1, state.opponentResolve)
            assertEquals((initial.playerOmens + 1).coerceAtMost(4), state.playerOmens)
        }
    }

    @Test
    fun `loss decreases player omens`() {
        val engine = createEngine()
        val initial = engine.getState()
        val result = engine.executeRound(DuelEngine.Stance.STRIKE)

        val state = engine.getState()
        if (result.outcome == DuelEngine.RoundOutcome.LOSE) {
            assertEquals((initial.playerOmens - 1).coerceAtLeast(0), state.playerOmens)
        }
    }

    @Test
    fun `draw changes nothing`() {
        val engine = createEngine()
        val initial = engine.getState()
        val result = engine.executeRound(DuelEngine.Stance.STRIKE)

        if (result.outcome == DuelEngine.RoundOutcome.DRAW) {
            val state = engine.getState()
            assertEquals(initial.playerOmens, state.playerOmens)
            assertEquals(initial.opponentResolve, state.opponentResolve)
        }
    }

    @Test
    fun `omens capped at 4`() {
        val engine = createEngine()
        // Run multiple rounds; omens should never exceed 4
        repeat(7) {
            if (!engine.getState().isComplete) {
                engine.executeRound(DuelEngine.Stance.STRIKE)
            }
        }
        assertTrue(engine.getState().playerOmens <= 4)
    }

    @Test
    fun `omens cannot go below 0`() {
        val engine = createEngine()
        repeat(7) {
            if (!engine.getState().isComplete) {
                engine.executeRound(DuelEngine.Stance.WARD)
            }
        }
        assertTrue(engine.getState().playerOmens >= 0)
    }

    @Test
    fun `duel ends when opponent resolve reaches 0`() {
        val engine = createEngine(opponentResolve = 1)
        // With resolve=1, a single win should end the duel
        var attempts = 0
        while (!engine.getState().isComplete && attempts < 20) {
            engine.executeRound(DuelEngine.Stance.STRIKE)
            attempts++
        }
        assertTrue(engine.getState().isComplete)
    }

    @Test
    fun `duel ends after 7 rounds maximum`() {
        // drawRng always returns 0f: opponent picks STRIKE, player STRIKE → DRAW, no omen loss
        val engine = createEngine(opponentResolve = 100, rng = drawRng)
        repeat(7) {
            if (!engine.getState().isComplete) {
                engine.executeRound(DuelEngine.Stance.STRIKE)
            }
        }
        assertTrue(engine.getState().isComplete)
        assertEquals(7, engine.getState().round)
    }

    @Test
    fun `player wins when opponent resolve reaches 0`() {
        val engine = createEngine(opponentResolve = 1)
        var result: DuelEngine.RoundResult
        var attempts = 0
        do {
            result = engine.executeRound(DuelEngine.Stance.STRIKE)
            attempts++
        } while (!engine.getState().isComplete && attempts < 20)

        if (engine.getState().opponentResolve <= 0) {
            assertEquals(true, engine.getState().playerWon)
        }
    }

    @Test
    fun `round result contains narrative text`() {
        val engine = createEngine()
        val result = engine.executeRound(DuelEngine.Stance.STRIKE)
        assertTrue(result.narrative.isNotBlank())
        assertNotNull(result.playerStance)
        assertNotNull(result.opponentStance)
    }

    @Test
    fun `log accumulates all round results`() {
        val engine = createEngine()
        repeat(3) {
            if (!engine.getState().isComplete) {
                engine.executeRound(DuelEngine.Stance.values().random())
            }
        }
        assertEquals(engine.getState().round, engine.getState().log.size)
    }

    @Test(expected = IllegalStateException::class)
    fun `executing round after completion throws`() {
        val engine = createEngine(opponentResolve = 1)
        // Force completion
        repeat(20) {
            if (!engine.getState().isComplete) {
                engine.executeRound(DuelEngine.Stance.STRIKE)
            }
        }
        assertTrue(engine.getState().isComplete)
        // This should throw
        engine.executeRound(DuelEngine.Stance.STRIKE)
    }

    @Test
    fun `player modifier is clamped to valid range`() {
        // Extreme modifier should be clamped internally
        val engine = createEngine(playerModifier = 5.0f)
        val result = engine.executeRound(DuelEngine.Stance.STRIKE)
        // Should not crash -- modifier is clamped to -0.3..0.3
        assertNotNull(result)
    }

    // --- Deterministic outcome tests ---

    @Test
    fun `ward beats strike deterministically`() {
        val engine = createEngine(opponentResolve = 3, rng = drawRng) // opponent always STRIKE
        val result = engine.executeRound(DuelEngine.Stance.WARD)
        assertEquals(DuelEngine.Stance.STRIKE, result.opponentStance)
        assertEquals(DuelEngine.RoundOutcome.WIN, result.outcome)
        assertEquals(2, engine.getState().opponentResolve)
        assertEquals(3, engine.getState().playerOmens) // 2 + 1
    }

    @Test
    fun `strike beats feint deterministically`() {
        val engine = createEngine(opponentResolve = 3, rng = feintRng) // opponent always FEINT
        val result = engine.executeRound(DuelEngine.Stance.STRIKE)
        assertEquals(DuelEngine.Stance.FEINT, result.opponentStance)
        assertEquals(DuelEngine.RoundOutcome.WIN, result.outcome)
        assertEquals(2, engine.getState().opponentResolve)
        assertEquals(3, engine.getState().playerOmens)
    }

    @Test
    fun `feint beats ward deterministically`() {
        val engine = createEngine(opponentResolve = 3, rng = wardRng) // opponent always WARD
        val result = engine.executeRound(DuelEngine.Stance.FEINT)
        assertEquals(DuelEngine.Stance.WARD, result.opponentStance)
        assertEquals(DuelEngine.RoundOutcome.WIN, result.outcome)
        assertEquals(2, engine.getState().opponentResolve)
        assertEquals(3, engine.getState().playerOmens)
    }

    @Test
    fun `omen depletion ends duel with player loss`() {
        // feintRng opponent FEINT, player WARD → LOSE every round, omens 2→1→0
        val engine = createEngine(opponentResolve = 3, rng = feintRng)
        engine.executeRound(DuelEngine.Stance.WARD)
        assertFalse(engine.getState().isComplete)
        assertEquals(1, engine.getState().playerOmens)
        engine.executeRound(DuelEngine.Stance.WARD)
        assertTrue(engine.getState().isComplete)
        assertEquals(0, engine.getState().playerOmens)
        assertEquals(false, engine.getState().playerWon)
    }

    @Test
    fun `round 7 timeout with all draws results in player loss`() {
        // drawRng → DRAW every round; no resolve change → no partial progress at round 7
        val engine = createEngine(opponentResolve = 5, rng = drawRng)
        repeat(7) { engine.executeRound(DuelEngine.Stance.STRIKE) }
        val state = engine.getState()
        assertTrue(state.isComplete)
        assertEquals(7, state.round)
        assertEquals(false, state.playerWon)
    }

    @Test
    fun `round 7 timeout with winning progress results in player win`() {
        // drawRng opponent STRIKE, player WARD → WIN every round; opponentResolve=10 won't hit 0 in 7
        val engine = createEngine(opponentResolve = 10, rng = drawRng)
        repeat(7) { engine.executeRound(DuelEngine.Stance.WARD) }
        val state = engine.getState()
        assertTrue(state.isComplete)
        assertEquals(7, state.round)
        assertEquals(true, state.playerWon)
    }

    @Test
    fun `escalation text appears from round 2 onwards`() {
        val engine = createEngine(opponentResolve = 100, rng = drawRng)
        val r1 = engine.executeRound(DuelEngine.Stance.STRIKE) // round 1: no prefix
        assertFalse(r1.narrative.contains("You test each other"))
        val r2 = engine.executeRound(DuelEngine.Stance.STRIKE) // round 2: has prefix
        assertTrue(r2.narrative.startsWith("You test each other's measure."))
    }
}
