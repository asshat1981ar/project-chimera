package com.chimera.feature.dialogue

import androidx.lifecycle.SavedStateHandle
import com.chimera.ai.AudioProvider
import com.chimera.ai.DialogueOrchestrator
import com.chimera.core.engine.RelationshipArchetypeEngine
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
import com.chimera.model.CinematicLine
import com.chimera.model.DialogueTurnResult
import com.chimera.model.SceneContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.wheneverBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class DialogueSceneViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

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
    private val archetypeEngine = RelationshipArchetypeEngine()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
        whenever(preferences.settings).thenReturn(flowOf(AppSettings()))
        wheneverBlocking { sceneInstanceDao.insert(org.mockito.kotlin.any()) } doReturn 1L
        wheneverBlocking { sceneInstanceDao.completeScene(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()) } doAnswer { }
        wheneverBlocking { dialogueTurnDao.insert(org.mockito.kotlin.any()) } doReturn 0L
        wheneverBlocking { memoryShardDao.getTopMemories(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()) } doReturn emptyList()
        wheneverBlocking { chapterProgressionUseCase.invoke() } doReturn "prologue"
        wheneverBlocking { chapterProgressionUseCase.getCinematicTransition() } doReturn null
        wheneverBlocking { chapterProgressionUseCase.markCinematicComplete(org.mockito.kotlin.any()) } doAnswer { }
        // sceneLoader.getScene returns null by default; VM handles via elvis
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) = DialogueSceneViewModel(
        savedStateHandle = savedStateHandle,
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
        chapterProgressionUseCase = chapterProgressionUseCase,
        archetypeEngine = archetypeEngine
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun cinematicCompletion_emitsNextSceneEvent() = runTest(testDispatcher) {
        val slotId = 1L
        val cinematicSceneId = "act1_finale"
        val nextSceneId = "act2_finale"
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(sceneLoader.getScene(cinematicSceneId)).thenReturn(null)
        whenever(sceneLoader.getCinematicScene(cinematicSceneId)).thenReturn(
            SceneContract(
                sceneId = cinematicSceneId,
                sceneTitle = "The Hollow Threshold",
                npcId = "narrator",
                npcName = "Narrator",
                isCinematic = true,
                cinematicLines = listOf(
                    CinematicLine(
                        speaker = "narrator",
                        speakerName = "Narrator",
                        text = "The threshold opens."
                    )
                ),
                onCompleteTag = "hollow_approach_complete"
            )
        )
        wheneverBlocking { chapterProgressionUseCase.getCinematicTransition() } doReturn nextSceneId

        val viewModel = buildViewModel(savedStateHandle = SavedStateHandle(mapOf("sceneId" to cinematicSceneId)))
        val emitted = mutableListOf<String?>()
        backgroundScope.launch {
            viewModel.sceneCompleteEvent.collect { emitted.add(it) }
        }

        // Let initializeScene complete before advancing the cinematic.
        advanceUntilIdle()
        assertEquals(cinematicSceneId, viewModel.uiState.value.sceneId)
        assertEquals("The Hollow Threshold", viewModel.uiState.value.sceneTitle)
        assertTrue(viewModel.uiState.value.isCinematic)

        viewModel.advanceCinematic()
        advanceUntilIdle()

        assertTrue(
            "Expected scene to be marked complete",
            viewModel.uiState.value.isSceneComplete
        )
        assertEquals(1, emitted.size)
        assertEquals(nextSceneId, emitted[0])
    }

    @Test
    fun submitTypedInput_feedsTheRelationshipArchetypeEngine() = runTest(testDispatcher) {
        val slotId = 1L
        val sceneId = "elena_market"
        val npcId = "elena"
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(slotId))
        whenever(sceneLoader.getScene(sceneId)).thenReturn(
            SceneContract(
                sceneId = sceneId,
                sceneTitle = "The Market Stall",
                npcId = npcId,
                npcName = "Elena"
            )
        )
        archetypeEngine.initializeArchetype(
            RelationshipArchetypeEngine.ArchetypeType.SHIFTING_THE_BURDEN, npcId, "player"
        )
        wheneverBlocking {
            orchestrator.generateTurn(
                org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any(),
                org.mockito.kotlin.any(), org.mockito.kotlin.any()
            )
        } doReturn DialogueTurnResult(npcLine = "You've let me down again.", relationshipDelta = -0.2f)
        wheneverBlocking {
            orchestrator.generateIntents(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())
        } doReturn listOf("Farewell.")

        val viewModel = buildViewModel(savedStateHandle = SavedStateHandle(mapOf("sceneId" to sceneId)))
        advanceUntilIdle()

        val stabilityBefore = archetypeEngine.getStabilityReport().values.single()
        viewModel.submitTypedInput("You're on your own with the stall from now on.")
        advanceUntilIdle()

        val stabilityAfter = archetypeEngine.getStabilityReport().values.single()
        assertTrue(
            "expected the archetype engine's simulated state to move after a dialogue turn",
            stabilityAfter != stabilityBefore
        )
    }
}
