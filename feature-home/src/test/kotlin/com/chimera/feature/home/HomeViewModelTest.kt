package com.chimera.feature.home

import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.data.repository.DialogueRepository
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.VowDao
import com.chimera.domain.usecase.ObserveActiveObjectiveSummariesUseCase
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.database.mapper.toModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val saveSlotDao: SaveSlotDao = mock()
    private val dialogueRepository: DialogueRepository = mock()
    private val vowDao: VowDao = mock()
    private val sceneLoader: SceneLoader = mock()
    private val observeActiveObjectiveSummariesUseCase: ObserveActiveObjectiveSummariesUseCase = mock()
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

    private fun buildViewModel() = HomeViewModel(
        saveSlotDao = saveSlotDao,
        dialogueRepository = dialogueRepository,
        vowDao = vowDao,
        sceneLoader = sceneLoader,
        observeActiveObjectiveSummariesUseCase = observeActiveObjectiveSummariesUseCase,
        gameSessionManager = gameSessionManager
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun initialState_hasNoActiveObjectives() {
        val viewModel = buildViewModel()
        assertTrue(viewModel.uiState.value.activeObjectives.isEmpty())
    }

    @Test
    fun continueSceneId_defaultsToPrologue_whenNoProgress() = runTest(testDispatcher) {
        val slotId = 1L
        val slot = SaveSlotEntity(id = slotId, slotIndex = 0, playerName = "Test", isEmpty = false)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(saveSlotDao.observeAll()).thenReturn(flowOf(listOf(slot)))
        whenever(vowDao.observeActive(slotId)).thenReturn(flowOf(emptyList()))
        whenever(observeActiveObjectiveSummariesUseCase(slotId)).thenReturn(flowOf(emptyList()))
        whenever(dialogueRepository.getLastIncompleteSceneId(slotId)).thenReturn(null)
        whenever(dialogueRepository.getCompletedSceneIds(slotId)).thenReturn(emptySet())

        val viewModel = buildViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals("prologue_scene_1", viewModel.uiState.value.continueSceneId)
    }

    @Test
    fun continueSceneId_isNull_whenNoActiveSceneAndProgressExists() = runTest(testDispatcher) {
        val slotId = 1L
        val slot = SaveSlotEntity(id = slotId, slotIndex = 0, playerName = "Test", isEmpty = false)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(saveSlotDao.observeAll()).thenReturn(flowOf(listOf(slot)))
        whenever(vowDao.observeActive(slotId)).thenReturn(flowOf(emptyList()))
        whenever(observeActiveObjectiveSummariesUseCase(slotId)).thenReturn(flowOf(emptyList()))
        whenever(dialogueRepository.getLastIncompleteSceneId(slotId)).thenReturn(null)
        whenever(dialogueRepository.getCompletedSceneIds(slotId)).thenReturn(setOf("prologue_scene_1"))

        val viewModel = buildViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.continueSceneId)
    }
}
