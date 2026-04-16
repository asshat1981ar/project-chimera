package com.chimera.domain.usecase

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.SaveRepository
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.entity.SceneInstanceEntity
import com.chimera.model.SaveSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DomainUseCaseTest {

    // ── ChapterProgressionUseCase ──────────────────────────────────────────────

    private lateinit var saveRepository: SaveRepository
    private lateinit var sceneInstanceDao: SceneInstanceDao
    private lateinit var gameSessionManager: GameSessionManager
    private lateinit var chapterProgressionUseCase: ChapterProgressionUseCase

    @Before
    fun setUp() {
        saveRepository = mock()
        sceneInstanceDao = mock()
        gameSessionManager = mock()
        chapterProgressionUseCase = ChapterProgressionUseCase(
            saveRepository, sceneInstanceDao, gameSessionManager
        )
    }

    private fun slot(tag: String) = SaveSlot(
        id = 1L, slotIndex = 0, playerName = "Tester",
        chapterTag = tag, playtimeSeconds = 0,
        lastPlayedAt = 0L, createdAt = 0L, isEmpty = false
    )

    private fun completedScene(id: String) = SceneInstanceEntity(
        id = 0L, saveSlotId = 1L, sceneId = id,
        npcId = "npc", status = "completed",
        startedAt = 0L, completedAt = 0L, turnCount = 1, usedFallback = false
    )

    @Test
    fun `chapter progression - no active slot does nothing`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
        chapterProgressionUseCase()
        verify(saveRepository, never()).updateChapterTag(any(), any())
    }

    @Test
    fun `chapter progression - prologue advances to act1 on act1 entry scene`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(1L))
        whenever(saveRepository.getSlot(1L)).thenReturn(slot("prologue"))
        whenever(sceneInstanceDao.getBySlot(1L)).thenReturn(
            listOf(completedScene(ChapterProgressionUseCase.ACT1_ENTRY_SCENE))
        )
        chapterProgressionUseCase()
        verify(saveRepository).updateChapterTag(1L, "act1")
    }

    @Test
    fun `chapter progression - prologue stays if entry scene not completed`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(1L))
        whenever(saveRepository.getSlot(1L)).thenReturn(slot("prologue"))
        whenever(sceneInstanceDao.getBySlot(1L)).thenReturn(emptyList())
        chapterProgressionUseCase()
        verify(saveRepository, never()).updateChapterTag(any(), any())
    }

    @Test
    fun `chapter progression - act1 advances to act2 on act2 entry scene`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(1L))
        whenever(saveRepository.getSlot(1L)).thenReturn(slot("act1"))
        whenever(sceneInstanceDao.getBySlot(1L)).thenReturn(
            listOf(completedScene(ChapterProgressionUseCase.ACT2_ENTRY_SCENE))
        )
        chapterProgressionUseCase()
        verify(saveRepository).updateChapterTag(1L, "act2")
    }

    @Test
    fun `chapter progression - act2 advances to act3 on act3 entry scene`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(1L))
        whenever(saveRepository.getSlot(1L)).thenReturn(slot("act2"))
        whenever(sceneInstanceDao.getBySlot(1L)).thenReturn(
            listOf(completedScene(ChapterProgressionUseCase.ACT3_ENTRY_SCENE))
        )
        chapterProgressionUseCase()
        verify(saveRepository).updateChapterTag(1L, "act3")
    }

    @Test
    fun `chapter progression - act3 never advances further`() = runTest {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(1L))
        whenever(saveRepository.getSlot(1L)).thenReturn(slot("act3"))
        whenever(sceneInstanceDao.getBySlot(1L)).thenReturn(emptyList())
        chapterProgressionUseCase()
        verify(saveRepository, never()).updateChapterTag(any(), any())
    }

    // ── ApplyRelationshipDeltaUseCase ──────────────────────────────────────────

    private lateinit var applyRelationshipDeltaUseCase: ApplyRelationshipDeltaUseCase
    private lateinit var characterRepository: com.chimera.data.repository.CharacterRepository
    private lateinit var journalEntryDao: com.chimera.database.dao.JournalEntryDao

    @Before
    fun setUpApplyRelationshipDelta() {
        characterRepository = mock()
        journalEntryDao = mock()
        applyRelationshipDeltaUseCase = ApplyRelationshipDeltaUseCase(characterRepository, journalEntryDao)
    }

    @Test
    fun `apply relationship delta - small delta skips journal entry`() = runTest {
        applyRelationshipDeltaUseCase(1L, "npc_1", "Aria", 0.05f)
        verify(characterRepository).adjustDisposition("npc_1", 0.05f)
        verify(journalEntryDao, never()).insert(any())
    }

    @Test
    fun `apply relationship delta - large positive delta creates journal entry`() = runTest {
        applyRelationshipDeltaUseCase(1L, "npc_1", "Aria", 0.2f)
        verify(journalEntryDao).insert(any())
    }

    @Test
    fun `apply relationship delta - large negative delta creates journal entry`() = runTest {
        applyRelationshipDeltaUseCase(1L, "npc_1", "Aria", -0.15f)
        verify(journalEntryDao).insert(any())
    }

    // ── ResolveCampNightUseCase ────────────────────────────────────────────────

    private lateinit var resolveCampNightUseCase: ResolveCampNightUseCase

    @Before
    fun setUpCampNight() {
        val nightEventProvider: com.chimera.data.NightEventProvider = mock()
        val campRepository: com.chimera.data.repository.CampRepository = mock()
        val rumorService: com.chimera.data.RumorService = mock()
        resolveCampNightUseCase = ResolveCampNightUseCase(nightEventProvider, campRepository, rumorService)
    }

    @Test
    fun `resolve camp night - outcome matches choice outcome text`() = runTest {
        val choice = com.chimera.data.NightEventChoice(
            text = "Stand watch",
            outcome = "The night passes without incident.",
            moraleDelta = 0.05f
        )
        val event = com.chimera.data.NightEvent(
            id = "test_event",
            title = "Rustling in the Dark",
            narrative = "Something moves beyond the firelight.",
            choices = listOf(choice)
        )
        val rumorService: com.chimera.data.RumorService = mock()
        val campRepository: com.chimera.data.repository.CampRepository = mock()
        val nightEventProvider: com.chimera.data.NightEventProvider = mock()
        val useCase = ResolveCampNightUseCase(nightEventProvider, campRepository, rumorService)
        val result = useCase.resolve(1L, event, choice)
        assertEquals("The night passes without incident.", result.outcome)
        assertEquals(0.05f, result.moraleChange, 0.001f)
    }

    @Test
    fun `resolve camp night - negative morale choice returns negative delta`() = runTest {
        val choice = com.chimera.data.NightEventChoice(
            text = "Argue over rations",
            outcome = "Tension rises in camp.",
            moraleDelta = -0.1f
        )
        val event = com.chimera.data.NightEvent(
            id = "argument",
            title = "Argument",
            narrative = "Voices raise.",
            choices = listOf(choice)
        )
        val useCase = ResolveCampNightUseCase(
            mock(), mock(), mock()
        )
        val result = useCase.resolve(1L, event, choice)
        assertEquals(-0.1f, result.moraleChange, 0.001f)
    }
}
