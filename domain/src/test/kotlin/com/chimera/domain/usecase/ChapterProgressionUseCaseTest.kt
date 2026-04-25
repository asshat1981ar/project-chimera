package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.DialogueRepository
import com.chimera.data.repository.SaveRepository
import com.chimera.model.SaveSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [ChapterProgressionUseCase].
 *
 * Tests cover chapter progression logic, repository interactions,
 * and edge cases for slot validation and state persistence.
 */
class ChapterProgressionUseCaseTest {

    private val dialogueRepository: DialogueRepository = mock()
    private val saveRepository: SaveRepository = mock()
    private val gameSessionManager: GameSessionManager = mock()

    private val testSlotId = 1L

    private fun useCase() = ChapterProgressionUseCase(
        dialogueRepository,
        saveRepository,
        gameSessionManager
    )

    private fun setupActiveSlot() {
        whenever(gameSessionManager.activeSlotId)
            .thenReturn(MutableStateFlow(testSlotId))
    }

    // ── Test 1: Chapter progression ───────────────────────────────────────────

    @Test
    fun `execute_advancesChapter() - progresses from prologue to act1 when mid-game scenes completed`() = runTest {
        // Given: Player has completed act1 mid-game scenes
        setupActiveSlot()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("outer_ruins_1", "watchtower_1"))
        whenever(saveRepository.getSlot(testSlotId))
            .thenReturn(SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "prologue"))

        // When: Chapter progression is evaluated
        val useCase = useCase()
        val newTag = useCase()

        // Then: Chapter advances to act1
        assertEquals("act1", newTag)
    }

    // ── Test 2: Repository method called ──────────────────────────────────────

    @Test
    fun `execute_callsUpdateChapterTag() - persists chapter tag when it changes`() = runTest {
        // Given: Player progresses to act2
        setupActiveSlot()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("hollow_approach", "ashen_gate"))
        whenever(saveRepository.getSlot(testSlotId))
            .thenReturn(SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "prologue"))

        // When: Chapter progression is evaluated
        val useCase = useCase()
        useCase()

        // Then: updateChapterTag is called with new tag
        verify(saveRepository).updateChapterTag(testSlotId, "act2")
    }

    // ── Test 3: Correct chapter returned ──────────────────────────────────────

    @Test
    fun `execute_returnsNewChapter() - returns correct chapter based on scene completion hierarchy`() = runTest {
        // Given: Player has completed act3 climax scenes (highest priority)
        setupActiveSlot()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("hollow_approach", "act2_climax", "act3_climax"))
        whenever(saveRepository.getSlot(testSlotId))
            .thenReturn(SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "act1"))

        // When: Chapter progression is evaluated
        val useCase = useCase()
        val newTag = useCase()

        // Then: Highest priority chapter (act3) is returned
        assertEquals("act3", newTag)
    }

    // ── Test 4: No progression past final chapter ─────────────────────────────

    @Test
    fun `execute_finalChapter_maintainsAct3() - maintains act3 as final chapter without further progression`() = runTest {
        // Given: Player is already at final chapter (act3)
        setupActiveSlot()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("act3_climax", "heart_of_tide"))
        whenever(saveRepository.getSlot(testSlotId))
            .thenReturn(SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "act3"))

        // When: Chapter progression is evaluated
        val useCase = useCase()
        val newTag = useCase()

        // Then: Chapter remains at act3 (no progression beyond final)
        assertEquals("act3", newTag)
    }

    // ── Test 5: Invalid slot handling ─────────────────────────────────────────

    @Test
    fun `execute_invalidSlot_returnsPrologue() - returns prologue when no active slot`() = runTest {
        // Given: No active slot (null activeSlotId)
        whenever(gameSessionManager.activeSlotId)
            .thenReturn(MutableStateFlow(null))

        // When: Chapter progression is evaluated
        val useCase = useCase()
        val newTag = useCase()

        // Then: Returns default prologue without repository calls
        assertEquals("prologue", newTag)
    }

    // ── Test 6: State persistence verified ────────────────────────────────────

    @Test
    fun `execute_persistsState() - does not persist when chapter tag unchanged`() = runTest {
        // Given: Player's chapter tag already matches their progress
        setupActiveSlot()
        whenever(dialogueRepository.getCompletedSceneIds(testSlotId))
            .thenReturn(setOf("outer_ruins_1"))
        whenever(saveRepository.getSlot(testSlotId))
            .thenReturn(SaveSlot(id = testSlotId, slotIndex = 0, playerName = "Test", chapterTag = "act1"))

        // When: Chapter progression is evaluated
        val useCase = useCase()
        useCase()

        // Then: updateChapterTag is NOT called (no change needed)
        verify(saveRepository, org.mockito.kotlin.never())
            .updateChapterTag(any(), any())
    }
}
