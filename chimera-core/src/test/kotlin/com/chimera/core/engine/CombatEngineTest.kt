package com.chimera.core.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CombatEngineTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun engine(
        opponentModifier: Float = 0f,
        intents: List<CombatEngine.IntentCard> = listOf(intent()),
        rng: Random = Random.Default
    ) = CombatEngine(
        playerName = "Kael",
        opponentName = "Hollow Knight",
        opponentArchetype = "guardian",
        availableIntents = intents,
        opponentModifier = opponentModifier,
        rng = rng
    )

    private fun intent(statBonus: Int = 0, bands: Map<String, String> = emptyMap()) =
        CombatEngine.IntentCard(
            id = "strike",
            label = "Strike",
            description = "Test strike",
            statBonus = statBonus,
            resultBands = bands
        )

    // Returns a Random that always produces `roll` for nextInt(from, until)
    private fun fixedRng(roll: Int) = object : Random() {
        override fun nextBits(bitCount: Int) = 0
        override fun nextInt(from: Int, until: Int) = roll.coerceIn(from, until - 1)
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test fun `initial state has correct defaults`() {
        val e = engine()
        val s = e.getState()
        assertEquals(0, s.rollCount)
        assertEquals(CombatEngine.MAX_RESOLVE, s.playerResolve)
        assertEquals(CombatEngine.MAX_RESOLVE, s.opponentResolve)
        assertNull(s.lastResult)
        assertTrue(s.log.isEmpty())
        assertFalse(s.isComplete)
        assertNull(s.playerWon)
    }

    // ── Band threshold mapping ─────────────────────────────────────────────

    @Test fun `roll 20 maps to CRITICAL_SUCCESS`() {
        val result = engine(rng = fixedRng(20)).executeRound(intent())
        assertEquals(CombatEngine.ResultBand.CRITICAL_SUCCESS, result.band)
    }

    @Test fun `roll 15 maps to SUCCESS`() {
        val result = engine(rng = fixedRng(15)).executeRound(intent())
        assertEquals(CombatEngine.ResultBand.SUCCESS, result.band)
    }

    @Test fun `roll 10 maps to PARTIAL`() {
        val result = engine(rng = fixedRng(10)).executeRound(intent())
        assertEquals(CombatEngine.ResultBand.PARTIAL, result.band)
    }

    @Test fun `roll 5 maps to FAILURE`() {
        val result = engine(rng = fixedRng(5)).executeRound(intent())
        assertEquals(CombatEngine.ResultBand.FAILURE, result.band)
    }

    @Test fun `roll 4 maps to CRITICAL_FAILURE`() {
        val result = engine(rng = fixedRng(4)).executeRound(intent())
        assertEquals(CombatEngine.ResultBand.CRITICAL_FAILURE, result.band)
    }

    // ── Resolve changes per band ──────────────────────────────────────────

    @Test fun `CRITICAL_SUCCESS removes 2 from opponent resolve`() {
        engine(rng = fixedRng(20)).apply {
            executeRound(intent())
            assertEquals(CombatEngine.MAX_RESOLVE - 2, getState().opponentResolve)
            assertEquals(CombatEngine.MAX_RESOLVE, getState().playerResolve)
        }
    }

    @Test fun `SUCCESS removes 1 from opponent resolve`() {
        engine(rng = fixedRng(15)).apply {
            executeRound(intent())
            assertEquals(CombatEngine.MAX_RESOLVE - 1, getState().opponentResolve)
            assertEquals(CombatEngine.MAX_RESOLVE, getState().playerResolve)
        }
    }

    @Test fun `PARTIAL removes 1 from player resolve`() {
        engine(rng = fixedRng(10)).apply {
            executeRound(intent())
            assertEquals(CombatEngine.MAX_RESOLVE, getState().opponentResolve)
            assertEquals(CombatEngine.MAX_RESOLVE - 1, getState().playerResolve)
        }
    }

    @Test fun `FAILURE removes 1 from player resolve`() {
        engine(rng = fixedRng(5)).apply {
            executeRound(intent())
            assertEquals(CombatEngine.MAX_RESOLVE, getState().opponentResolve)
            assertEquals(CombatEngine.MAX_RESOLVE - 1, getState().playerResolve)
        }
    }

    @Test fun `CRITICAL_FAILURE removes 2 from player resolve`() {
        engine(rng = fixedRng(4)).apply {
            executeRound(intent())
            assertEquals(CombatEngine.MAX_RESOLVE, getState().opponentResolve)
            assertEquals(CombatEngine.MAX_RESOLVE - 2, getState().playerResolve)
        }
    }

    // ── Terminal conditions ────────────────────────────────────────────────

    @Test fun `game ends when opponent resolve reaches 0`() {
        // CRITICAL_SUCCESS: opp -2 per round → 3→1→0 after 2 rounds
        val e = engine(rng = fixedRng(20))
        e.executeRound(intent())
        assertFalse(e.getState().isComplete)
        e.executeRound(intent())
        assertTrue(e.getState().isComplete)
        assertEquals(0, e.getState().opponentResolve)
    }

    @Test fun `game ends when player resolve reaches 0`() {
        // CRITICAL_FAILURE: player -2 per round → 3→1→0 after 2 rounds
        val e = engine(rng = fixedRng(4))
        e.executeRound(intent())
        assertFalse(e.getState().isComplete)
        e.executeRound(intent())
        assertTrue(e.getState().isComplete)
        assertEquals(0, e.getState().playerResolve)
    }

    @Test fun `game completes after MAX_ROLLS rounds`() {
        // 3 rounds of SUCCESS: opponentResolve 3→2→1→0 at round 3
        val e = engine(rng = fixedRng(15))
        repeat(CombatEngine.MAX_ROLLS) { e.executeRound(intent()) }
        assertTrue(e.getState().isComplete)
        assertEquals(CombatEngine.MAX_ROLLS, e.getState().rollCount)
    }

    @Test fun `player wins when opponent resolve reaches 0 first`() {
        val e = engine(rng = fixedRng(20)) // CRITICAL_SUCCESS, opp -2 per round
        e.executeRound(intent())
        e.executeRound(intent())
        assertEquals(true, e.getState().playerWon)
    }

    @Test fun `player loses when player resolve reaches 0 first`() {
        val e = engine(rng = fixedRng(4)) // CRITICAL_FAILURE, player -2 per round
        e.executeRound(intent())
        e.executeRound(intent())
        assertEquals(false, e.getState().playerWon)
    }

    @Test fun `resolve is not allowed to go below zero`() {
        val e = engine(rng = fixedRng(4)) // CRITICAL_FAILURE: player -2
        e.executeRound(intent()) // player: 1
        e.executeRound(intent()) // player: 0 (would be -1, coerced)
        assertEquals(0, e.getState().playerResolve)
    }

    // ── Modifiers ─────────────────────────────────────────────────────────

    @Test fun `statBonus is added to roll total`() {
        // roll=10 + statBonus=5 = total=15 → SUCCESS (not PARTIAL)
        val result = engine(rng = fixedRng(10)).executeRound(intent(statBonus = 5))
        assertEquals(CombatEngine.ResultBand.SUCCESS, result.band)
        assertEquals(15, result.total)
        assertEquals(5, result.modifier)
    }

    @Test fun `opponentModifier shifts roll total`() {
        // roll=15 + opponentModifier=-3 → total=15+(−3*5)=15−15=0, coerced to 1 → CRITICAL_FAILURE
        val result = engine(opponentModifier = -3f, rng = fixedRng(15)).executeRound(intent())
        assertEquals(CombatEngine.ResultBand.CRITICAL_FAILURE, result.band)
    }

    @Test fun `total roll is clamped to maximum of 25`() {
        // roll=20 + statBonus=10 + opponentModifier=0 → 30, clamped to 25
        val result = engine(rng = fixedRng(20)).executeRound(intent(statBonus = 10))
        assertEquals(25, result.total)
    }

    @Test fun `total roll is clamped to minimum of 1`() {
        // roll=1, statBonus=-10 → -9, clamped to 1
        val result = engine(rng = fixedRng(1)).executeRound(intent(statBonus = -10))
        assertEquals(1, result.total)
    }

    // ── Guard and invariants ──────────────────────────────────────────────

    @Test(expected = IllegalStateException::class)
    fun `executing round after complete throws IllegalStateException`() {
        val e = engine(rng = fixedRng(20)) // opponent dies in 2 rounds
        e.executeRound(intent())
        e.executeRound(intent())
        e.executeRound(intent()) // combat already complete — must throw
    }

    // ── Log and result tracking ────────────────────────────────────────────

    @Test fun `roll result is stored in log after each round`() {
        val e = engine(rng = fixedRng(15))
        e.executeRound(intent())
        assertEquals(1, e.getState().log.size)
        e.executeRound(intent())
        assertEquals(2, e.getState().log.size)
    }

    @Test fun `lastResult reflects most recent round`() {
        val e = engine(rng = fixedRng(15))
        val r1 = e.executeRound(intent())
        assertEquals(r1, e.getState().lastResult)
    }

    @Test fun `rollCount increments correctly`() {
        val e = engine(rng = fixedRng(15))
        assertEquals(0, e.getState().rollCount)
        e.executeRound(intent())
        assertEquals(1, e.getState().rollCount)
        e.executeRound(intent())
        assertEquals(2, e.getState().rollCount)
    }

    // ── Narrative ────────────────────────────────────────────────────────

    @Test fun `fallback narrative is non-blank for every band`() {
        CombatEngine.ResultBand.values().forEach { band ->
            val roll = when (band) {
                CombatEngine.ResultBand.CRITICAL_SUCCESS -> 20
                CombatEngine.ResultBand.SUCCESS          -> 15
                CombatEngine.ResultBand.PARTIAL          -> 10
                CombatEngine.ResultBand.FAILURE          -> 5
                CombatEngine.ResultBand.CRITICAL_FAILURE -> 4
            }
            val result = engine(rng = fixedRng(roll)).executeRound(intent())
            assertTrue("narrative blank for $band", result.narrative.isNotBlank())
        }
    }

    @Test fun `resultBands from intent card used when key matches`() {
        val customBands = mapOf("success" to "Custom success narrative")
        val e = engine(rng = fixedRng(15))
        val result = e.executeRound(intent(bands = customBands))
        assertEquals("Custom success narrative", result.narrative)
    }

    @Test fun `intents list exposed from engine`() {
        val i = intent(statBonus = 3)
        val e = engine(intents = listOf(i))
        assertEquals(1, e.intents.size)
        assertEquals(3, e.intents.first().statBonus)
    }

    // ── RollResult fields ─────────────────────────────────────────────────

    @Test fun `RollResult carries raw roll, modifier, and total`() {
        val result = engine(rng = fixedRng(12)).executeRound(intent(statBonus = 2))
        assertEquals(12, result.roll)
        assertEquals(2, result.modifier)
        assertEquals(14, result.total)
    }

    // ── PlayerWon transitions ─────────────────────────────────────────────

    @Test fun `playerWon is null while game is in progress`() {
        val e = engine(rng = fixedRng(15)) // SUCCESS, opp resolve -1/round
        e.executeRound(intent()) // opp: 2, game in progress
        assertNull(e.getState().playerWon) // not yet complete
    }

    @Test fun `playerName and opponentName accessible`() {
        val e = CombatEngine("Elara", "Shadow Lord", "escalation", listOf(intent()))
        assertEquals("Elara", e.playerName)
        assertEquals("Shadow Lord", e.opponentName)
    }
}
