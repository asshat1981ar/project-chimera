package com.chimera.feature.settings

import com.chimera.data.GameSessionManager
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.entity.FactionStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FactionStandingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val factionStateDao: FactionStateDao = mock()
    private val gameSessionManager: GameSessionManager = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = FactionStandingViewModel(
        factionStateDao = factionStateDao,
        gameSessionManager = gameSessionManager
    )

    @Test
    fun initialState_isLoading() {
        val viewModel = buildViewModel()
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun uiState_emptyList_whenNoSlotId() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.factions.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun uiState_loadsFactions_whenSlotIdActive() = runTest(testDispatcher) {
        val slotIdFlow = MutableStateFlow(1L)
        whenever(gameSessionManager.activeSlotId).thenReturn(slotIdFlow)

        val testFactions = listOf(
            FactionStateEntity(
                id = 1,
                saveSlotId = 1L,
                factionId = "faction_a",
                factionName = "Test Faction A",
                influence = 0.8f,
                playerStanding = 0.5f
            ),
            FactionStateEntity(
                id = 2,
                saveSlotId = 1L,
                factionId = "faction_b",
                factionName = "Test Faction B",
                influence = 0.3f,
                playerStanding = -0.2f
            )
        )
        whenever(factionStateDao.observeAll(1L)).thenReturn(flowOf(testFactions))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.factions.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun uiState_sortsFactionsByInfluenceDescending() = runTest(testDispatcher) {
        val slotIdFlow = MutableStateFlow(1L)
        whenever(gameSessionManager.activeSlotId).thenReturn(slotIdFlow)

        val testFactions = listOf(
            FactionStateEntity(
                id = 1,
                saveSlotId = 1L,
                factionId = "low",
                factionName = "Low Influence",
                influence = 0.2f,
                playerStanding = 0.0f
            ),
            FactionStateEntity(
                id = 2,
                saveSlotId = 1L,
                factionId = "high",
                factionName = "High Influence",
                influence = 0.9f,
                playerStanding = 0.0f
            )
        )
        whenever(factionStateDao.observeAll(1L)).thenReturn(flowOf(testFactions))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("high", viewModel.uiState.value.factions.first().factionId)
    }

    @Test
    fun refresh_doesNotThrow() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(true)
    }
}
