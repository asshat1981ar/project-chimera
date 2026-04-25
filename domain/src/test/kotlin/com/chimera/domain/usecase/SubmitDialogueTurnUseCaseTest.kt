package com.chimera.domain.usecase

import com.chimera.ai.DialogueOrchestrator
import com.chimera.data.repository.CharacterRepository
import com.chimera.data.repository.DialogueRepository
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SubmitDialogueTurnUseCaseTest {

    private val orchestrator: DialogueOrchestrator = mock()
    private val dialogueRepository: DialogueRepository = mock()
    private val characterRepository: CharacterRepository = mock()

    private fun useCase() = SubmitDialogueTurnUseCase(orchestrator, dialogueRepository, characterRepository)

    private fun fakeContract(
        sceneId: String = "scene_1",
        npcId: String = "elena",
        npcName: String = "Elena"
    ) = SceneContract(
        sceneId = sceneId,
        sceneTitle = "Test Scene",
        npcId = npcId,
        npcName = npcName,
        setting = "Test setting",
        stakes = "Test stakes"
    )

    private fun fakePlayerInput(text: String = "Hello") = PlayerInput(text = text)

    private fun fakeCharacterState(
        characterId: String = "elena",
        disposition: Float = 0.5f
    ) = CharacterState(
        characterId = characterId,
        saveSlotId = 1L,
        dispositionToPlayer = disposition
    )

    private fun fakeMemoryShard(summary: String = "Test memory") = MemoryShard(
        id = 1L,
        saveSlotId = 1L,
        sceneId = "scene_1",
        characterId = "elena",
        summary = summary
    )

    private fun fakeTurnResult(
        npcLine: String = "Hello, traveler!",
        emotion: String = "neutral",
        relationshipDelta: Float = 0.1f,
        flags: List<String> = emptyList(),
        memoryCandidates: List<String> = emptyList()
    ) = DialogueTurnResult(
        npcLine = npcLine,
        emotion = emotion,
        relationshipDelta = relationshipDelta,
        flags = flags,
        memoryCandidates = memoryCandidates
    )

    @Test
    fun executeSubmitsTurn() = runTest {
        // Given
        val contract = fakeContract()
        val playerInput = fakePlayerInput("Hello")
        val characterState = fakeCharacterState()
        val turnResult = fakeTurnResult(npcLine = "Greetings!")
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(turnResult)
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // When
        val result = useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        verify(dialogueRepository).persistTurn(1L, "scene_1", "player", "Hello", "")
        verify(dialogueRepository).persistTurn(1L, "scene_1", "elena", "Greetings!", "neutral")
        assertNotNull(result)
    }

    @Test
    fun executeReturnsDialogueResponse() = runTest {
        // Given
        val contract = fakeContract()
        val playerInput = fakePlayerInput("What brings you here?")
        val characterState = fakeCharacterState()
        val expectedResponse = fakeTurnResult(
            npcLine = "I seek the ancient artifact",
            emotion = "curious",
            relationshipDelta = 0.15f
        )
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(expectedResponse)
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // When
        val result = useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        assertEquals("I seek the ancient artifact", result.result.npcLine)
        assertEquals("curious", result.result.emotion)
        assertEquals(0.15f, result.result.relationshipDelta, 0.001f)
    }

    @Test
    fun executeEmptyInputThrowsException() = runTest {
        // Given
        val contract = fakeContract()
        val emptyInput = fakePlayerInput(text = "")
        val characterState = fakeCharacterState()

        // When/Then
        try {
            useCase()(
                slotId = 1L,
                sceneId = "scene_1",
                contract = contract,
                playerInput = emptyInput,
                characterState = characterState,
                recentMemories = emptyList(),
                turnHistory = emptyList()
            )
            // Should not reach here
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("blank", ignoreCase = true) == true || e.message?.contains("empty", ignoreCase = true) == true)
        }
    }

    @Test
    fun executeInvalidSceneIdThrowsException() = runTest {
        // Given
        val invalidContract = fakeContract(sceneId = "")
        val playerInput = fakePlayerInput("Hello")
        val characterState = fakeCharacterState()

        // When/Then
        try {
            useCase()(
                slotId = 1L,
                sceneId = "",
                contract = invalidContract,
                playerInput = playerInput,
                characterState = characterState,
                recentMemories = emptyList(),
                turnHistory = emptyList()
            )
            // Should not reach here
            throw AssertionError("Expected IllegalArgumentException was not thrown")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("scene", ignoreCase = true) == true || e.message?.contains("blank", ignoreCase = true) == true)
        }
    }

    @Test
    fun executeUpdatesTurnCount() = runTest {
        // Given
        val contract = fakeContract()
        val playerInput = fakePlayerInput("Let us continue")
        val characterState = fakeCharacterState()
        val turnResult = fakeTurnResult()
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(turnResult)
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // When
        useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then - verify persistTurn was called twice (player + NPC)
        verify(dialogueRepository, org.mockito.kotlin.times(2)).persistTurn(
            any(), any(), any(), any(), any()
        )
    }

    @Test
    fun executeHandlesProviderError() = runTest {
        // Given
        val contract = fakeContract()
        val playerInput = fakePlayerInput("Hello")
        val characterState = fakeCharacterState()
        val fallbackResult = fakeTurnResult(npcLine = "Fallback response")
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(fallbackResult)
        whenever(orchestrator.isFallbackActive).thenReturn(true)

        // When
        val result = useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        assertTrue(result.isFallback)
        assertEquals("Fallback response", result.result.npcLine)
    }

    @Test
    fun executePersistsMemoryCandidates() = runTest {
        // Given
        val contract = fakeContract()
        val playerInput = fakePlayerInput("Tell me about the shrine")
        val characterState = fakeCharacterState()
        val turnResult = fakeTurnResult(
            memoryCandidates = listOf("Player asked about shrine", "Elena explained shrine history")
        )
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(turnResult)
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // When
        useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        val captor = argumentCaptor<List<MemoryShard>>()
        verify(dialogueRepository).insertMemoryShards(captor.capture())
        val captured = captor.firstValue
        assertEquals(2, captured.size)
        assertEquals("Player asked about shrine", captured[0].summary)
    }

    @Test
    fun executeAdjustsDispositionOnRelationshipDelta() = runTest {
        // Given
        val contract = fakeContract(npcId = "thorne")
        val playerInput = fakePlayerInput("I trust you")
        val characterState = fakeCharacterState(characterId = "thorne", disposition = 0.3f)
        val turnResult = fakeTurnResult(relationshipDelta = 0.2f)
        val updatedState = fakeCharacterState(characterId = "thorne", disposition = 0.5f)
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(turnResult)
        whenever(orchestrator.isFallbackActive).thenReturn(false)
        whenever(characterRepository.adjustDisposition("thorne", 0.2f)).thenReturn(Unit)
        whenever(characterRepository.getCharacterState("thorne")).thenReturn(updatedState)

        // When
        val result = useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        verify(characterRepository).adjustDisposition("thorne", 0.2f)
        assertEquals(0.5f, result.updatedDisposition ?: 0f, 0.001f)
    }

    @Test
    fun executeHandlesRecruitCompanionFlag() = runTest {
        // Given
        val contract = fakeContract(npcId = "vessa")
        val playerInput = fakePlayerInput("Join us")
        val characterState = fakeCharacterState(characterId = "vessa")
        val turnResult = fakeTurnResult(flags = listOf("recruit_companion"))
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(turnResult)
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // When
        useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        verify(characterRepository).promoteToCompanion("vessa")
    }

    @Test
    fun executeNoRelationshipDeltaDoesNotAdjustDisposition() = runTest {
        // Given
        val contract = fakeContract()
        val playerInput = fakePlayerInput("Neutral statement")
        val characterState = fakeCharacterState()
        val turnResult = fakeTurnResult(relationshipDelta = 0f)
        whenever(orchestrator.generateTurn(any(), any(), any(), any(), any())).thenReturn(turnResult)
        whenever(orchestrator.isFallbackActive).thenReturn(false)

        // When
        val result = useCase()(
            slotId = 1L,
            sceneId = "scene_1",
            contract = contract,
            playerInput = playerInput,
            characterState = characterState,
            recentMemories = emptyList(),
            turnHistory = emptyList()
        )

        // Then
        verify(characterRepository, never()).adjustDisposition(any(), any())
        assertNull(result.updatedDisposition)
    }
}
