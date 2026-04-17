package com.chimera.feature.dialogue

import androidx.lifecycle.SavedStateHandle
import com.chimera.ai.AudioProvider
import com.chimera.ai.DialogueOrchestrator
import com.chimera.data.AppSettings
import com.chimera.data.ChimeraPreferences
import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.DialogueTurnDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.dao.VowDao
import com.chimera.domain.usecase.ChapterProgressionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DialogueSceneViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val orchestrator: DialogueOrchestrator = mock()
    private val gameSessionManager: GameSessionManager = mock()
    private val sceneLoader: SceneLoader = mock()
    private val dialogueTurnDao: DialogueTurnDao = mock()
    private val sceneInstanceDao: SceneInstanceDao = mock()
    private val memoryShardDao: MemoryShardDao = mock()
    private val characterDao: CharacterDao = mock()
    private val characterStateDao: CharacterStateDao = mock()
    private val journalEntryDao: JournalEntryDao = mock()
    private val saveSlotDao: SaveSlotDao = mock()
    private val vowDao: VowDao = mock()
    private val audioProvider: AudioProvider = mock()
    private val preferences: ChimeraPreferences = mock()
    private val chapterProgressionUseCase: ChapterProgressionUseCase = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
        whenever(preferences.settings).thenReturn(flowOf(AppSettings()))
        // sceneLoader.getScene returns null by default; VM handles via elvis
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = DialogueSceneViewModel(
        savedStateHandle = SavedStateHandle(),
        orchestrator = orchestrator,
        gameSessionManager = gameSessionManager,
        sceneLoader = sceneLoader,
        dialogueTurnDao = dialogueTurnDao,
        sceneInstanceDao = sceneInstanceDao,
        memoryShardDao = memoryShardDao,
        characterDao = characterDao,
        characterStateDao = characterStateDao,
        journalEntryDao = journalEntryDao,
        saveSlotDao = saveSlotDao,
        vowDao = vowDao,
        audioProvider = audioProvider,
        preferences = preferences,
        chapterProgressionUseCase = chapterProgressionUseCase
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.uiState)
    }
}
