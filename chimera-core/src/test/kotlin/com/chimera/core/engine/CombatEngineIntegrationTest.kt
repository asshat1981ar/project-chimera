package com.chimera.core.engine

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class CombatEngineIntegrationTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun engine(
        opponentModifier: Float = 0f,
        rng: Random = Random.Default
    ) = CombatEngine(
        playerName = "Kael",
        opponentName = "Shadow",
        opponentArchetype = "guardian",
        availableIntents = listOf(intent()),
        opponentModifier = opponentModifier,
        rng = rng
    )

    private fun intent(statBonus: Int = 0) = CombatEngine.IntentCard(
        id = "strike", label = "Strike", description = "test", statBonus = statBonus
    )

    private fun fixedRng(roll: Int) = object : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextInt(from: Int, until: Int) = roll.coerceIn(from, until - 1)
    }

    private fun sequenceRng(vararg rolls: Int): Random {
        var idx = 0
        return object : Random() {
            override fun nextBits(bitCount: Int) = 0
            override fun nextInt(from: Int, until: Int) = rolls[idx++ % rolls.size].coerceIn(from, until - 1)
        }
    }

    // ── Integration tests ─────────────────────────────────────────────────────

    /**
     * Test 1: Three consecutive SUCCESS rolls deplete opponent resolve fully.
     * fixedRng(15) → SUCCESS every round → opponent 3→2→1→0 over 3 rounds.
     */
    @Test
    fun `fullSequence_threeSuccesses_opponentDefeated`() {
        val e = engine(rng = fixedRng(15))

        e.executeRound(intent())
        e.executeRound(intent())
        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(true, state.playerWon)
        assertEquals(0, state.opponentResolve)
        assertEquals(3, state.rollCount)
        assertEquals(3, state.log.size)
    }

    /**
     * Test 2: Two CRIT_FAILURE rolls deplete player resolve (player -2 per round).
     * fixedRng(4) → CRITICAL_FAILURE → player 3→1→0 after round 2.
     */
    @Test
    fun `fullSequence_threeCriticalFailures_playerDefeated`() {
        val e = engine(rng = fixedRng(4))

        e.executeRound(intent())
        assertFalse(e.getState().isComplete)

        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(false, state.playerWon)
        assertEquals(0, state.playerResolve)
    }

    /**
     * Test 3: Three consecutive PARTIAL rolls deplete player resolve (player -1 per round).
     * fixedRng(10) → PARTIAL → player 3→2→1→0 after round 3.
     * Combat completes because playerResolve reaches 0, and since opponent was never damaged,
     * playerWon=false.
     */
    @Test
    fun `fullSequence_mixedResults_drawAtMaxRolls`() {
        val e = engine(rng = fixedRng(10))

        e.executeRound(intent())
        e.executeRound(intent())
        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(3, state.rollCount)
        assertEquals(0, state.playerResolve)
        assertEquals(false, state.playerWon)
    }

    /**
     * Test 4: CRIT_SUCCESS in round 1 (opp 3→1), SUCCESS in round 2 (opp 1→0) → player wins in 2 rounds.
     * sequenceRng(20, 15): roll=20 → CRITICAL_SUCCESS (opp-2), roll=15 → SUCCESS (opp-1).
     */
    @Test
    fun `fullSequence_critSuccessThenPartial_playerWins`() {
        val e = engine(rng = sequenceRng(20, 15))

        e.executeRound(intent())
        assertFalse(e.getState().isComplete)

        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(true, state.playerWon)
        assertEquals(0, state.opponentResolve)
        assertEquals(2, state.rollCount)
    }

    /**
     * Test 5: CRITICAL_SUCCESS (round 1, opp-2), CRITICAL_FAILURE (round 2, player-2),
     * SUCCESS (round 3, opp-1). Both take hits; player ultimately wins.
     * After round 1: player=3, opp=1
     * After round 2: player=1, opp=1
     * After round 3: player=1, opp=0 → complete, playerWon=true
     */
    @Test
    fun `fullSequence_successAndCritFailure_bothTakeHits`() {
        val e = engine(rng = sequenceRng(20, 4, 15))

        e.executeRound(intent())
        val stateAfterR1 = e.getState()
        assertFalse(stateAfterR1.isComplete)
        assertEquals(3, stateAfterR1.playerResolve)
        assertEquals(1, stateAfterR1.opponentResolve)

        e.executeRound(intent())
        val stateAfterR2 = e.getState()
        assertFalse(stateAfterR2.isComplete)
        assertEquals(1, stateAfterR2.playerResolve)
        assertEquals(1, stateAfterR2.opponentResolve)

        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(true, state.playerWon)
        assertEquals(1, state.playerResolve)
        assertEquals(0, state.opponentResolve)
    }

    /**
     * Test 6: Strong opponent (opponentModifier=-2f) shifts totals down.
     * fixedRng(15) + modifier=-2f: total = 15 + (-2*5) = 5 → FAILURE (player -1 per round).
     * Player 3→2→1→0 over 3 rounds. isComplete=true, playerWon=false.
     */
    @Test
    fun `fullSequence_strongOpponent_reducesPlayerAdvantage`() {
        val e = engine(opponentModifier = -2f, rng = fixedRng(15))

        e.executeRound(intent())
        e.executeRound(intent())
        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(0, state.playerResolve)
        assertEquals(false, state.playerWon)
    }

    /**
     * Test 7: Weak opponent (opponentModifier=3f) boosts totals.
     * fixedRng(10) + modifier=3f: total = 10 + (3*5) = 25 → CRITICAL_SUCCESS (opp-2 per round).
     * Opponent 3→1 (round 1), 1→0 (round 2) → player wins in 2 rounds.
     */
    @Test
    fun `fullSequence_weakOpponent_playerWinsQuickly`() {
        val e = engine(opponentModifier = 3f, rng = fixedRng(10))

        e.executeRound(intent())
        assertFalse(e.getState().isComplete)

        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(true, state.playerWon)
        assertEquals(0, state.opponentResolve)
        assertEquals(2, state.rollCount)
    }

    /**
     * Test 8: Log tracks all three rounds with distinct bands.
     * sequenceRng(10, 15, 20):
     *   Round 1 (roll=10): PARTIAL, player -1 (player 3→2, opp=3)
     *   Round 2 (roll=15): SUCCESS, opp -1 (opp 3→2, player=2)
     *   Round 3 (roll=20): CRITICAL_SUCCESS, opp -2 (opp 2→0) → complete, playerWon=true
     * log.size=3, rolls and bands tracked correctly.
     */
    @Test
    fun `fullSequence_logTrackingAllRounds`() {
        val e = engine(rng = sequenceRng(10, 15, 20))

        e.executeRound(intent())
        e.executeRound(intent())
        e.executeRound(intent())

        val state = e.getState()
        assertTrue(state.isComplete)
        assertEquals(3, state.log.size)

        assertEquals(10, state.log[0].roll)
        assertEquals(CombatEngine.ResultBand.PARTIAL, state.log[0].band)

        assertEquals(15, state.log[1].roll)
        assertEquals(CombatEngine.ResultBand.SUCCESS, state.log[1].band)

        assertEquals(20, state.log[2].roll)
        assertEquals(CombatEngine.ResultBand.CRITICAL_SUCCESS, state.log[2].band)
    }
}
