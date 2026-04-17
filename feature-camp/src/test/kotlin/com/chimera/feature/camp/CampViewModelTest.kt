package com.chimera.feature.camp

import app.cash.turbine.test
import com.chimera.data.DutyType
import com.chimera.data.GameSessionManager
import com.chimera.data.NightEvent
import com.chimera.data.NightEventChoice
import com.chimera.data.NightEventProvider
import com.chimera.data.RumorService
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.VowEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CampViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val characterDao: CharacterDao = mock()
    private val characterStateDao: CharacterStateDao = mock()
    private val vowDao: VowDao = mock()
    private val nightEventProvider: NightEventProvider = mock()
    private val rumorService: RumorService = mock()
    private val gameSessionManager: GameSessionManager = mock()

    // Reusable test data
    private val slotId = 1L
    private val companion1 = CharacterEntity(
        id = "npc_1",
        saveSlotId = slotId,
        name = "Theron",
        role = "COMPANION"
    )
    private val companion2 = CharacterEntity(
        id = "npc_2",
        saveSlotId = slotId,
        name = "Liriel",
        role = "COMPANION"
    )
    private val state1 = CharacterStateEntity(
        characterId = "npc_1",
        saveSlotId = slotId,
        dispositionToPlayer = 0.4f
    )
    private val state2 = CharacterStateEntity(
        characterId = "npc_2",
        saveSlotId = slotId,
        dispositionToPlayer = 0.6f
    )
    private val vow1 = VowEntity(id = 1, saveSlotId = slotId, description = "Protect the village")

    private val testChoice = NightEventChoice(
        text = "Listen quietly",
        moraleDelta = 0.05f,
        outcome = "Morale rises slightly."
    )
    private val testEvent = NightEvent(
        id = "campfire_stories",
        title = "Campfire Stories",
        narrative = "The fire crackles.",
        choices = listOf(testChoice)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): CampViewModel = CampViewModel(
        characterDao = characterDao,
        characterStateDao = characterStateDao,
        vowDao = vowDao,
        nightEventProvider = nightEventProvider,
        rumorService = rumorService,
        gameSessionManager = gameSessionManager
    )

    // ─── Slot null: no active game ───────────────────────────────────────────

    @Test
    fun `uiState emits isLoading false when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `uiState has empty companions when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.companions.isEmpty())
        }
    }

    @Test
    fun `uiState has empty vows when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertTrue(state.activeVows.isEmpty())
        }
    }

    @Test
    fun `uiState nightEvent is null when activeSlotId is null`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertNull(expectMostRecentItem().nightEvent)
        }
    }

    // ─── Slot present: happy-path loading ───────────────────────────────────

    @Test
    fun `uiState emits isLoading false when activeSlotId is set`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertFalse(expectMostRecentItem().isLoading)
        }
    }

    @Test
    fun `uiState companions count matches DAO emission`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(2, expectMostRecentItem().companions.size)
        }
    }

    @Test
    fun `uiState companion names match characters from DAO`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val names = expectMostRecentItem().companions.map { it.character.name }
            assertTrue(names.contains("Theron"))
            assertTrue(names.contains("Liriel"))
        }
    }

    @Test
    fun `uiState companion state is associated correctly`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            val card = expectMostRecentItem().companions.first { it.character.id == "npc_1" }
            assertEquals(state1, card.state)
        }
    }

    @Test
    fun `uiState activeVows matches DAO emission`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            assertEquals(listOf(vow1), expectMostRecentItem().activeVows)
        }
    }

    @Test
    fun `uiState morale is computed from dispositions`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            // avg disposition = (0.4 + 0.6) / 2 = 0.5 → morale = (0.5 + 1) / 2 = 0.75
            val morale = expectMostRecentItem().morale
            assertEquals(0.75f, morale, 0.01f)
        }
    }

    // ─── triggerNightEvent ───────────────────────────────────────────────────

    @Test
    fun `triggerNightEvent delegates to nightEventProvider with current morale`() = runTest {
        stubActiveSlotWithData()
        whenever(nightEventProvider.getRandomEvent(0.75f)).thenReturn(testEvent)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.triggerNightEvent()
            advanceUntilIdle()

            verify(nightEventProvider).getRandomEvent(0.75f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `triggerNightEvent sets nightEvent in uiState`() = runTest {
        stubActiveSlotWithData()
        whenever(nightEventProvider.getRandomEvent(0.75f)).thenReturn(testEvent)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.triggerNightEvent()
            advanceUntilIdle()

            assertNotNull(expectMostRecentItem().nightEvent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `triggerNightEvent clears previous nightEventOutcome`() = runTest {
        stubActiveSlotWithData()
        whenever(nightEventProvider.getRandomEvent(0.75f)).thenReturn(testEvent)

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            // First resolve then trigger again
            vm.resolveNightEvent(testChoice)
            advanceUntilIdle()
            vm.triggerNightEvent()
            advanceUntilIdle()

            assertNull(expectMostRecentItem().nightEventOutcome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── resolveNightEvent ───────────────────────────────────────────────────

    @Test
    fun `resolveNightEvent sets nightEventOutcome to choice outcome`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.resolveNightEvent(testChoice)
            advanceUntilIdle()

            assertEquals(testChoice.outcome, expectMostRecentItem().nightEventOutcome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resolveNightEvent with different choice reflects correct outcome text`() = runTest {
        stubActiveSlotWithData()
        val alternateChoice = NightEventChoice(
            text = "Keep watch",
            moraleDelta = -0.02f,
            outcome = "Night passes uneventfully."
        )

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.resolveNightEvent(alternateChoice)
            advanceUntilIdle()

            assertEquals("Night passes uneventfully.", expectMostRecentItem().nightEventOutcome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── dismissNightEvent ───────────────────────────────────────────────────

    @Test
    fun `dismissNightEvent clears nightEvent from uiState`() = runTest {
        stubActiveSlotWithData()
        whenever(nightEventProvider.getRandomEvent(0.75f)).thenReturn(testEvent)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.triggerNightEvent()
            advanceUntilIdle()
            vm.dismissNightEvent()
            advanceUntilIdle()

            assertNull(expectMostRecentItem().nightEvent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissNightEvent clears nightEventOutcome from uiState`() = runTest {
        stubActiveSlotWithData()
        whenever(nightEventProvider.getRandomEvent(0.75f)).thenReturn(testEvent)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.resolveNightEvent(testChoice)
            advanceUntilIdle()
            vm.dismissNightEvent()
            advanceUntilIdle()

            assertNull(expectMostRecentItem().nightEventOutcome)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissNightEvent calls rumorService advanceDay with active slotId`() = runTest {
        stubActiveSlotWithData()
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.dismissNightEvent()
            advanceUntilIdle()

            verify(rumorService).advanceDay(slotId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── assignDuty / clearDuty ──────────────────────────────────────────────

    @Test
    fun `assignDuty adds duty for companion in dutyAssignments`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.assignDuty("npc_1", DutyType.GUARD)
            advanceUntilIdle()

            val assignments = expectMostRecentItem().dutyAssignments
            val npc1Duty = assignments.first { it.companionId == "npc_1" }.duty
            assertEquals(DutyType.GUARD, npc1Duty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `assignDuty for different companions are independent`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.assignDuty("npc_1", DutyType.GUARD)
            vm.assignDuty("npc_2", DutyType.REST)
            advanceUntilIdle()

            val assignments = expectMostRecentItem().dutyAssignments
            assertEquals(DutyType.GUARD, assignments.first { it.companionId == "npc_1" }.duty)
            assertEquals(DutyType.REST, assignments.first { it.companionId == "npc_2" }.duty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearDuty removes duty for a companion`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.assignDuty("npc_1", DutyType.FORAGE)
            advanceUntilIdle()
            vm.clearDuty("npc_1")
            advanceUntilIdle()

            val assignments = expectMostRecentItem().dutyAssignments
            assertNull(assignments.first { it.companionId == "npc_1" }.duty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clearDuty does not affect duties for other companions`() = runTest {
        stubActiveSlotWithData()

        val vm = buildViewModel()

        vm.uiState.test {
            advanceUntilIdle()
            vm.assignDuty("npc_1", DutyType.GUARD)
            vm.assignDuty("npc_2", DutyType.REST)
            advanceUntilIdle()
            vm.clearDuty("npc_1")
            advanceUntilIdle()

            val assignments = expectMostRecentItem().dutyAssignments
            assertNull(assignments.first { it.companionId == "npc_1" }.duty)
            assertEquals(DutyType.REST, assignments.first { it.companionId == "npc_2" }.duty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun stubActiveSlotWithData() {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(characterDao.observeCompanions(slotId))
            .thenReturn(flowOf(listOf(companion1, companion2)))
        whenever(characterStateDao.observeBySlot(slotId))
            .thenReturn(flowOf(listOf(state1, state2)))
        whenever(vowDao.observeActive(slotId))
            .thenReturn(flowOf(listOf(vow1)))
    }
}
