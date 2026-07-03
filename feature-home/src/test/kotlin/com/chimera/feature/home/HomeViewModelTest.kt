package com.chimera.feature.home

import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.data.repository.DialogueRepository
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.VowDao
import com.chimera.domain.usecase.ObserveActiveObjectiveSummariesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
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
}
