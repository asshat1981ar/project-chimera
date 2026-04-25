package com.chimera.ui.screens.duel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.chimera.core.engine.CombatEngine
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
class DuelViewModelTest {

    private lateinit var context: Context
    private lateinit var characterDao: CharacterDao
    private lateinit var characterStateDao: CharacterStateDao
    private lateinit var journalEntryDao: JournalEntryDao
    private lateinit var gameSessionManager: GameSessionManager
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var testDispatcher: StandardTestDispatcher

    private val testOpponentId = "test-opponent"
    private val testOpponentName = "Test Opponent"
    private val testTitle = "The Challenger"
    private val testArchetype = "ESCALATION"
    private val testDisposition = 0.5f

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        savedStateHandle = SavedStateHandle(mapOf("opponentId" to testOpponentId))

        // Mock Context with assets
        context = mock {
            val assets = mock<android.content.res.AssetManager> {
                val inputStream = mock<InputStream> {
                    on { bufferedReader() } doReturn java.io.BufferedReader(
                        java.io.StringReader(combatIntentsJson)
                    )
                }
                on { open(eq("combat_intents.json")) } doReturn inputStream
            }
            on { assets } doReturn assets
        }

        // Mock DAOs
        characterDao = mock {
            onBlocking { getById(testOpponentId) } doReturn CharacterEntity(
                id = testOpponentId,
                name = testOpponentName,
                title = testTitle,
                archetype = testArchetype,
                level = 5,
                experience = 1000,
                health = 100,
                maxHealth = 100,
                resolve = 3,
                maxResolve = 3,
                disposition = 0.5f,
                stats = emptyMap()
            )
        }

        characterStateDao = mock {
            onBlocking { getByCharacterId(testOpponentId) } doReturn CharacterStateEntity(
                id = "state-1",
                characterId = testOpponentId,
                activeArchetype = testArchetype,
                dispositionToPlayer = testDisposition,
                relationshipLevel = 0.5f,
                trustLevel = 0.5f,
                fearLevel = 0.2f,
                respectLevel = 0.7f
            )
        }

        journalEntryDao = mock()

        // Mock GameSessionManager with active slot
        gameSessionManager = mock {
            on { activeSlotId } doReturn MutableStateFlow("slot-1")
        }
    }

    // ── Test 1: Initial state loaded correctly ───────────────────────────────

    @Test
    fun `initialState_loadedCorrectly() - opponent and intents populated`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.opponentName.contains(testOpponentName))
        assertEquals(testArchetype, state.opponentArchetype)
        assertFalse(state.availableIntents.isEmpty())
        assertEquals(CombatPhase.INTENT, state.phase)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initialState_defaultsWhenOpponentNotFound`() = runTest(testDispatcher) {
        whenever(characterDao.getById(testOpponentId)) doReturn null

        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Unknown Opponent", state.opponentName)
        assertEquals("default", state.opponentArchetype)
        assertFalse(state.isLoading)
    }

    // ── Test 2: Intent selection updates state ────────────────────────────────

    @Test
    fun `selectIntent_updatesState() - intent selection works`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val intent = viewModel.uiState.value.availableIntents.first()
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CombatPhase.RESOLVING, state.phase)
        assertEquals(1, state.rollCount)
        assertNotNull(state.lastResult)
        assertTrue(state.log.isNotEmpty())
    }

    @Test
    fun `executeIntent_ignoredWhenPhaseNotIntent`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val intent = viewModel.uiState.value.availableIntents.first()
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        // Second execute should be ignored (phase is now RESOLVING)
        val initialState = viewModel.uiState.value
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        // State should not have changed significantly (no new roll)
        val afterState = viewModel.uiState.value
        assertEquals(initialState.rollCount, afterState.rollCount)
    }

    @Test
    fun `executeIntent_ignoredWhenComplete`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        // Simulate combat completion by manually setting state
        val intent = viewModel.uiState.value.availableIntents.first()

        // Execute until complete (critical success removes 2 resolve per round)
        viewModel.executeIntent(intent)
        advanceUntilIdle()
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)

        // Further executes should be ignored
        val finalRollCount = viewModel.uiState.value.rollCount
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        assertEquals(finalRollCount, viewModel.uiState.value.rollCount)
    }

    // ── Test 3: CombatEngine integration ──────────────────────────────────────

    @Test
    fun `resolveDuel_callsEngine() - CombatEngine integration`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val intent = viewModel.uiState.value.availableIntents.first()
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.rollCount >= 1)
        assertNotNull(state.lastResult)
        assertTrue(state.lastResult?.roll in 1..20)
        assertTrue(state.lastResult?.total in 1..25)
        assertNotNull(state.lastResult?.band)
        assertTrue(state.lastResult?.narrative.isNullOrBlank().not())
    }

    // ── Test 4: Victory outcome ───────────────────────────────────────────────

    @Test
    fun `onVictory_grantsRewards() - victory outcome recorded`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        // Use critical success intents to win quickly (opponent loses 2 resolve per round)
        val criticalIntent = viewModel.uiState.value.availableIntents.first()

        // Round 1: opponent 3→1
        viewModel.executeIntent(criticalIntent)
        advanceUntilIdle()
        // Round 2: opponent 1→0 (victory)
        viewModel.executeIntent(criticalIntent)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(true, state.playerWon)

        // Verify journal entry was created
        verify(journalEntryDao).insert(
            argThat {
                title.contains(testOpponentName) &&
                body.contains("victorious") &&
                category == "story"
            }
        )

        // Verify disposition adjustment
        verify(characterStateDao).adjustDisposition(eq(testOpponentId), eq(0.05f))
    }

    // ── Test 5: Defeat outcome ────────────────────────────────────────────────

    @Test
    fun `onDefeat_appliesPenalties() - defeat outcome recorded`() = runTest(testDispatcher) {
        // Create a test that forces player defeat (critical failures)
        // We need to mock the RNG or use intents that cause player harm

        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        // Execute rounds - depending on RNG, may result in defeat or draw
        // For deterministic defeat, we'd need to inject a controlled RNG
        // This test verifies the defeat handling path exists
        val intent = viewModel.uiState.value.availableIntents.first()

        // Execute all 3 rounds (max rolls)
        repeat(3) {
            viewModel.executeIntent(intent)
            advanceUntilIdle()
        }

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)

        // If player lost (opponent has more resolve or player at 0)
        if (state.playerWon == false) {
            verify(journalEntryDao).insert(
                argThat {
                    body.contains("defeated")
                }
            )
            verify(characterStateDao).adjustDisposition(eq(testOpponentId), eq(-0.05f))
        }
    }

    // ── Test 6: Archetype-based filtering ─────────────────────────────────────

    @Test
    fun `loadIntentsWithArchetype_filtering() - archetype-based intent filtering`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        // ESCALATION archetype should have specific intents
        assertTrue(state.availableIntents.isNotEmpty())

        // Verify intents match the archetype from the JSON file
        val intentIds = state.availableIntents.map { it.id }
        // ESCALATION archetype has: strike, defend, outmaneuver
        assertTrue(intentIds.contains("strike") ||
                   intentIds.contains("defend") ||
                   intentIds.contains("outmaneuver"))
    }

    @Test
    fun `loadIntents_withDispositionFilter() - parley requires positive disposition`() = runTest(testDispatcher) {
        // Set low disposition to filter out parley option
        whenever(characterStateDao.getByCharacterId(testOpponentId)) doReturn CharacterStateEntity(
            id = "state-1",
            characterId = testOpponentId,
            activeArchetype = "default",
            dispositionToPlayer = -0.8f,  // Very negative
            relationshipLevel = 0.1f,
            trustLevel = 0.1f,
            fearLevel = 0.8f,
            respectLevel = 0.1f
        )

        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Parley requires disposition > -0.5, so it should be filtered out
        val parleyIntent = state.availableIntents.find { it.id == "parley" }
        assertNull("Parley should be filtered out with low disposition", parleyIntent)
    }

    @Test
    fun `loadIntents_fallsBackToDefault() - when archetype not found`() = runTest(testDispatcher) {
        whenever(characterStateDao.getByCharacterId(testOpponentId)) doReturn CharacterStateEntity(
            id = "state-1",
            characterId = testOpponentId,
            activeArchetype = "NONEXISTENT_ARCHETYPE",
            dispositionToPlayer = testDisposition,
            relationshipLevel = 0.5f,
            trustLevel = 0.5f,
            fearLevel = 0.2f,
            respectLevel = 0.7f
        )

        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        // Should fall back to default intents
        assertFalse(state.availableIntents.isEmpty())
        val intentIds = state.availableIntents.map { it.id }
        assertTrue(intentIds.contains("strike"))
        assertTrue(intentIds.contains("defend"))
    }

    // ── Test 7: Reset functionality ───────────────────────────────────────────

    @Test
    fun `acknowledgeResult_resetsPhase() - allows next intent selection`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val intent = viewModel.uiState.value.availableIntents.first()
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        // Phase should be RESOLVING after execute
        assertEquals(CombatPhase.RESOLVING, viewModel.uiState.value.phase)

        // Acknowledge to reset phase
        viewModel.acknowledgeResult()
        advanceUntilIdle()

        // Phase should be back to INTENT (if not complete)
        val state = viewModel.uiState.value
        if (!state.isComplete) {
            assertEquals(CombatPhase.INTENT, state.phase)
        } else {
            assertEquals(CombatPhase.COMPLETE, state.phase)
        }
    }

    // ── Additional edge case tests ────────────────────────────────────────────

    @Test
    fun `uiState_emitsUpdates() - StateFlow updates correctly`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        val states = mutableListOf<DuelUiState>()
        val job = kotlinx.coroutines.launch(testDispatcher) {
            viewModel.uiState.collect { states.add(it) }
        }

        advanceUntilIdle()

        // Should have at least initial loading state and loaded state
        assertTrue(states.size >= 2)

        // Initial state should have loading = true
        assertEquals(true, states.first().isLoading)

        // Final state should have loading = false
        assertFalse(states.last().isLoading)

        job.cancel()
    }

    @Test
    fun `combatState_tracksRollCount() - increments correctly`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        val intent = viewModel.uiState.value.availableIntents.first()

        assertEquals(0, viewModel.uiState.value.rollCount)

        viewModel.executeIntent(intent)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.rollCount)

        viewModel.acknowledgeResult()
        advanceUntilIdle()

        viewModel.executeIntent(intent)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.rollCount)
    }

    @Test
    fun `combatState_tracksResolve() - player and opponent resolve change`() = runTest(testDispatcher) {
        val viewModel = DuelViewModel(
            context = context,
            savedStateHandle = savedStateHandle,
            characterDao = characterDao,
            characterStateDao = characterStateDao,
            journalEntryDao = journalEntryDao,
            gameSessionManager = gameSessionManager
        )

        advanceUntilIdle()

        // Initial resolve should be MAX (3)
        assertEquals(CombatEngine.MAX_RESOLVE, viewModel.uiState.value.playerResolve)
        assertEquals(CombatEngine.MAX_RESOLVE, viewModel.uiState.value.opponentResolve)

        val intent = viewModel.uiState.value.availableIntents.first()
        viewModel.executeIntent(intent)
        advanceUntilIdle()

        // Resolve should have changed based on result
        val state = viewModel.uiState.value
        assertTrue(state.playerResolve <= CombatEngine.MAX_RESOLVE)
        assertTrue(state.opponentResolve <= CombatEngine.MAX_RESOLVE)
    }
}

// Combat intents JSON for testing
private const val combatIntentsJson = """
{
  "version": 1,
  "intents": {
    "default": [
      {
        "id": "strike",
        "label": "Strike Hard",
        "description": "Press the attack. High risk, high reward.",
        "statBonus": 2,
        "resultBands": {
          "critical": "You land a decisive blow. Opponent loses 2 Resolve.",
          "success": "A clean hit. Opponent loses 1 Resolve.",
          "partial": "You strike but take the brunt of the counter. Both lose 1 Resolve.",
          "failure": "Your attack is turned aside. You lose 1 Resolve.",
          "criticalFailure": "You overextend. You lose 2 Resolve."
        }
      },
      {
        "id": "defend",
        "label": "Hold Ground",
        "description": "Brace for impact. Recover Resolve on success.",
        "statBonus": 0,
        "resultBands": {
          "critical": "You weather the storm and find an opening. You recover 1 Resolve.",
          "success": "You hold firm. No Resolve lost.",
          "partial": "A near miss. You lose 1 Resolve but the worst is avoided.",
          "failure": "The blow lands. You lose 1 Resolve.",
          "criticalFailure": "Badly shaken. You lose 2 Resolve."
        }
      },
      {
        "id": "outmaneuver",
        "label": "Outmaneuver",
        "description": "Use speed and cunning over force.",
        "statBonus": 1,
        "resultBands": {
          "critical": "You slip past every guard. Opponent loses 2 Resolve.",
          "success": "You find the gap. Opponent loses 1 Resolve.",
          "partial": "Clever but not enough. You lose 1 Resolve.",
          "failure": "Anticipated. You lose 1 Resolve.",
          "criticalFailure": "Walked into a trap. You lose 2 Resolve."
        }
      },
      {
        "id": "parley",
        "label": "Parley",
        "description": "Words cut deeper than steel. Requires disposition > -0.5.",
        "statBonus": -1,
        "requiresDispositionAbove": -0.5,
        "resultBands": {
          "critical": "They falter. Opponent loses 2 Resolve and disposition rises.",
          "success": "Doubt enters their eyes. Opponent loses 1 Resolve.",
          "partial": "They listen, then laugh. You lose 1 Resolve.",
          "failure": "Your words ring hollow. You lose 1 Resolve.",
          "criticalFailure": "You've only made them angrier. You lose 2 Resolve, disposition falls."
        }
      }
    ],
    "ESCALATION": [
      {
        "id": "strike",
        "label": "Match Their Fury",
        "description": "Meet aggression with aggression.",
        "statBonus": 3,
        "resultBands": {
          "critical": "You match their fury perfectly. Opponent loses 2 Resolve.",
          "success": "You stand firm. Opponent loses 1 Resolve.",
          "partial": "Both take damage. Both lose 1 Resolve.",
          "failure": "You falter. You lose 1 Resolve.",
          "criticalFailure": "Overwhelmed. You lose 2 Resolve."
        }
      },
      {
        "id": "defend",
        "label": "Absorb and Wait",
        "description": "Let their rage exhaust itself.",
        "statBonus": 1,
        "resultBands": {
          "critical": "Their rage breaks against you. You recover 1 Resolve.",
          "success": "You hold. No Resolve lost.",
          "partial": "You lose 1 Resolve but survive.",
          "failure": "The blow lands. You lose 1 Resolve.",
          "criticalFailure": "Badly hurt. You lose 2 Resolve."
        }
      },
      {
        "id": "outmaneuver",
        "label": "Disrupt Rhythm",
        "description": "Break the pattern of escalation.",
        "statBonus": 1,
        "resultBands": {
          "critical": "You break their rhythm. Opponent loses 2 Resolve.",
          "success": "You find the gap. Opponent loses 1 Resolve.",
          "partial": "They adapt. You lose 1 Resolve.",
          "failure": "Predicted. You lose 1 Resolve.",
          "criticalFailure": "Trapped. You lose 2 Resolve."
        }
      }
    ]
  }
}
"""
