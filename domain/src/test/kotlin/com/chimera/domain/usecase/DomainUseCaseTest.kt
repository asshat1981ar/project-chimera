package com.chimera.domain.usecase

import com.chimera.ai.DialogueOrchestrator
import com.chimera.ai.FakeDialogueProvider
import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.DialogueRepository
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [StartSceneUseCase] and [SubmitDialogueTurnUseCase].
 *
 * These use cases wire together the AI orchestrator, repository layer, and
 * session state. All dependencies are mocked so tests run without Android or
 * Room infrastructure.
 *
 * Add `testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")` to
 * domain/build.gradle.kts if not already present.
 */
class DomainUseCaseTest {

    // ── Shared mocks ─────────────────────────────────────────────────────────

    private val sceneLoader: SceneLoader = mock()
    private val dialogueRepository: DialogueRepository = mock()
    private val characterRepository: CharacterRepository = mock()
    private val gameSessionManager: GameSessionManager = mock()
    private val fakeProvider = FakeDialogueProvider()
    private lateinit var orchestrator: DialogueOrchestrator

    private val testSlotId = 42L
    private val testContract = SceneContract(
        sceneId = "test_scene",
        sceneTitle = "Test Scene",
        npcId = "warden",
        npcName = "The Warden",
        setting = "a test gate",
        maxTurns = 5
    )
    private val testCharState = CharacterState(
        characterId = "warden",
        saveSlotId = testSlotId,
        dispositionToPlayer = 0f
    )

    @Before
    fun setup() {
        orchestrator = DialogueOrchestrator(fakeProvider)
        whenever(gameSessionManager.activeSlotId)
            .thenReturn(MutableStateFlow(testSlotId))
    }

    // ── StartSceneUseCase tests ───────────────────────────────────────────────

    @Test
    fun `StartSceneUseCase returns valid init result`() = runTest {
        whenever(sceneLoader.getScene("test_scene")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(testSlotId, "test_scene", "warden"))
            .thenReturn(99L)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharState)
        whenever(dialogueRepository.getRecentMemories(testSlotId, "warden", 5))
            .thenReturn(emptyList())

        val useCase = StartSceneUseCase(
            sceneLoader, dialogueRepository, characterRepository, orchestrator
        )
        val result = useCase(testSlotId, "test_scene")

        assertEquals("test_scene", result.contract.sceneId)
        assertEquals("The Warden", result.contract.npcName)
        assertTrue(result.openingTurn.npcLine.isNotBlank())
        assertTrue(result.intents.isNotEmpty())
        assertEquals(99L, result.sceneInstanceId)
        assertTrue(result.isFallback) // no primary provider → fallback active
    }

    @Test
    fun `StartSceneUseCase creates fallback contract for unknown scene`() = runTest {
        whenever(sceneLoader.getScene("unknown_xyz")).thenReturn(null)
        whenever(dialogueRepository.createSceneInstance(any(), any(), any())).thenReturn(1L)
        whenever(characterRepository.getCharacterState(any())).thenReturn(null)
        whenever(dialogueRepository.getRecentMemories(any(), any(), any())).thenReturn(emptyList())

        val useCase = StartSceneUseCase(
            sceneLoader, dialogueRepository, characterRepository, orchestrator
        )
        val result = useCase(testSlotId, "unknown_xyz")

        assertEquals("unknown_xyz", result.contract.sceneId)
        assertEquals("Stranger", result.contract.npcName)
        assertNotNull(result.openingTurn)
    }

    @Test
    fun `StartSceneUseCase persists opening turn to repository`() = runTest {
        whenever(sceneLoader.getScene("test_scene")).thenReturn(testContract)
        whenever(dialogueRepository.createSceneInstance(any(), any(), any())).thenReturn(1L)
        whenever(characterRepository.getCharacterState(any())).thenReturn(testCharState)
        whenever(dialogueRepository.getRecentMemories(any(), any(), any())).thenReturn(emptyList())

        val useCase = StartSceneUseCase(
            sceneLoader, dialogueRepository, characterRepository, orchestrator
        )
        useCase(testSlotId, "test_scene")

        // Opening turn must be persisted
        verify(dialogueRepository).persistTurn(
            slotId = testSlotId,
            sceneId = "test_scene",
            speakerId = "warden",
            text = any(),
            emotion = any()
        )
    }

    // ── SubmitDialogueTurnUseCase tests ───────────────────────────────────────

    @Test
    fun `SubmitDialogueTurnUseCase returns turn outcome with npc line`() = runTest {
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharState)

        val useCase = SubmitDialogueTurnUseCase(
            orchestrator, dialogueRepository, characterRepository
        )
        val outcome = useCase(
            slotId = testSlotId,
            sceneId = "test_scene",
            contract = testContract,
            playerInput = PlayerInput("Hello"),
            characterState = testCharState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        assertTrue(outcome.result.npcLine.isNotBlank())
        assertTrue(outcome.isFallback)
    }

    @Test
    fun `SubmitDialogueTurnUseCase persists both player and npc turns`() = runTest {
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharState)

        val useCase = SubmitDialogueTurnUseCase(
            orchestrator, dialogueRepository, characterRepository
        )
        useCase(
            slotId = testSlotId,
            sceneId = "test_scene",
            contract = testContract,
            playerInput = PlayerInput("What do you guard?"),
            characterState = testCharState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Player turn persisted first, then NPC turn
        verify(dialogueRepository).persistTurn(
            slotId = testSlotId, sceneId = "test_scene",
            speakerId = "player", text = "What do you guard?", emotion = ""
        )
        verify(dialogueRepository).persistTurn(
            slotId = testSlotId, sceneId = "test_scene",
            speakerId = "warden", text = any(), emotion = any()
        )
    }

    @Test
    fun `SubmitDialogueTurnUseCase adjusts disposition on non-zero delta`() = runTest {
        // The FakeDialogueProvider produces a positive delta for kind input
        whenever(characterRepository.getCharacterState("warden"))
            .thenReturn(testCharState.copy(dispositionToPlayer = 0.4f))

        val useCase = SubmitDialogueTurnUseCase(
            orchestrator, dialogueRepository, characterRepository
        )
        val outcome = useCase(
            slotId = testSlotId,
            sceneId = "test_scene",
            contract = testContract,
            playerInput = PlayerInput("Thank you, I trust you, friend"),
            characterState = testCharState.copy(dispositionToPlayer = 0.4f),
            recentMemories = emptyList(),
            turnHistory = listOf(DialogueTurnResult("opening"))
        )

        // Delta is non-zero for kind input → adjustDisposition must be called
        if (outcome.result.relationshipDelta != 0f) {
            verify(characterRepository).adjustDisposition("warden", outcome.result.relationshipDelta)
        }
    }

    @Test
    fun `SubmitDialogueTurnUseCase inserts memory shards when present`() = runTest {
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharState)

        val useCase = SubmitDialogueTurnUseCase(
            orchestrator, dialogueRepository, characterRepository
        )
        val outcome = useCase(
            slotId = testSlotId,
            sceneId = "test_scene",
            contract = testContract,
            playerInput = PlayerInput("I will destroy you"),
            characterState = testCharState,
            recentMemories = emptyList(),
            turnHistory = listOf(DialogueTurnResult("opening"))
        )

        // Verify memory shard insertion is called if candidates are non-empty
        if (outcome.result.memoryCandidates.isNotEmpty()) {
            verify(dialogueRepository).insertMemoryShards(any())
        }
    }

    @Test
    fun `SubmitDialogueTurnUseCase promotes companion on recruit flag`() = runTest {
        // Create a provider that emits a recruit_companion flag
        val recruitProvider = object : FakeDialogueProvider() {
            override suspend fun generateTurn(
                contract: com.chimera.model.SceneContract,
                playerInput: com.chimera.model.PlayerInput,
                characterState: com.chimera.model.CharacterState,
                recentMemories: List<com.chimera.model.MemoryShard>,
                turnHistory: List<com.chimera.model.DialogueTurnResult>
            ) = DialogueTurnResult(
                npcLine = "You've earned my trust. I'll join you.",
                emotion = "warm",
                relationshipDelta = 0.15f,
                flags = listOf("recruit_companion")
            )
        }
        val recruitOrchestrator = DialogueOrchestrator(recruitProvider)
        whenever(characterRepository.getCharacterState("warden")).thenReturn(testCharState)

        val useCase = SubmitDialogueTurnUseCase(
            recruitOrchestrator, dialogueRepository, characterRepository
        )
        useCase(
            slotId = testSlotId,
            sceneId = "test_scene",
            contract = testContract,
            playerInput = PlayerInput("Join me"),
            characterState = testCharState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        verify(characterRepository).promoteToCompanion("warden")
    }

    // ── ChapterProgressionUseCase tests ──────────────────────────────────────

    @Test
    fun `ChapterProgressionUseCase returns prologue for no completed scenes`() = runTest {
        val saveRepo: com.chimera.data.repository.SaveRepository = mock()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId)).thenReturn(emptySet())
        whenever(saveRepo.getSlot(testSlotId)).thenReturn(
            com.chimera.model.SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test")
        )

        val useCase = ChapterProgressionUseCase(dialogueRepository, saveRepo, gameSessionManager)
        val tag = useCase()

        assertEquals("prologue", tag)
    }

    @Test
    fun `ChapterProgressionUseCase returns act1 after mid-game scenes`() = runTest {
        val saveRepo: com.chimera.data.repository.SaveRepository = mock()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("outer_ruins_1", "watchtower_1"))
        whenever(saveRepo.getSlot(testSlotId)).thenReturn(
            com.chimera.model.SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "prologue")
        )

        val useCase = ChapterProgressionUseCase(dialogueRepository, saveRepo, gameSessionManager)
        val tag = useCase()

        assertEquals("act1", tag)
    }

    @Test
    fun `ChapterProgressionUseCase returns act2 after ashen_gate`() = runTest {
        val saveRepo: com.chimera.data.repository.SaveRepository = mock()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("hollow_approach", "ashen_gate"))
        whenever(saveRepo.getSlot(testSlotId)).thenReturn(
            com.chimera.model.SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "act1")
        )

        val useCase = ChapterProgressionUseCase(dialogueRepository, saveRepo, gameSessionManager)
        val tag = useCase()

        assertEquals("act2", tag)
    }

    @Test
    fun `ChapterProgressionUseCase returns act3 after act2 climax`() = runTest {
        val saveRepo: com.chimera.data.repository.SaveRepository = mock()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("hollow_approach", "ashen_gate", "act2_climax"))
        whenever(saveRepo.getSlot(testSlotId)).thenReturn(
            com.chimera.model.SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "act2")
        )

        val useCase = ChapterProgressionUseCase(dialogueRepository, saveRepo, gameSessionManager)
        val tag = useCase()

        assertEquals("act3", tag)
    }
}
