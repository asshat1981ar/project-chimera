package com.chimera.feature.journal

import com.chimera.data.GameSessionManager
import com.chimera.data.repository.QuestRepository
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.VowDao
import com.chimera.domain.usecase.SaveJournalEntryUseCase
import com.chimera.model.JournalEntry
import com.chimera.model.QuestWithObjectives
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val journalEntryDao: JournalEntryDao = mock()
    private val vowDao: VowDao = mock()
    private val questRepository: QuestRepository = mock()
    private val gameSessionManager: GameSessionManager = mock()
    private val saveJournalEntryUseCase: SaveJournalEntryUseCase = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
        whenever(questRepository.observeQuestsWithObjectives(org.mockito.kotlin.any())).thenReturn(flowOf(emptyList()))
        whenever(vowDao.observeAll(org.mockito.kotlin.any())).thenReturn(flowOf(emptyList()))
        whenever(journalEntryDao.observeUnreadCount(org.mockito.kotlin.any())).thenReturn(flowOf(0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = JournalViewModel(
        journalEntryDao = journalEntryDao,
        vowDao = vowDao,
        questRepository = questRepository,
        gameSessionManager = gameSessionManager,
        saveJournalEntryUseCase = saveJournalEntryUseCase
    )

    private fun fakeEntry(category: String = "story") = JournalEntry(
        saveSlotId = 1L,
        title = "Test",
        body = "Content",
        category = category,
        createdAt = 0L
    )

    @Test
    fun initialState_isNotNull() {
        val viewModel = buildViewModel()
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun saveEntry_invokesUseCase() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val entry = fakeEntry()

        viewModel.saveEntry(entry)
        advanceUntilIdle()

        verify(saveJournalEntryUseCase).invoke(entry)
    }

    @Test
    fun saveEntry_passesEntryToUseCase() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val entry = fakeEntry()

        viewModel.saveEntry(entry)
        advanceUntilIdle()

        verify(saveJournalEntryUseCase).invoke(entry)
    }

    @Test
    fun saveEntry_withStoryCategory() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val entry = fakeEntry(category = "story")

        viewModel.saveEntry(entry)
        advanceUntilIdle()

        verify(saveJournalEntryUseCase).invoke(entry)
    }

    @Test
    fun saveEntry_withVowCategory() = runTest(testDispatcher) {
        val viewModel = buildViewModel()
        val entry = fakeEntry(category = "vow")

        viewModel.saveEntry(entry)
        advanceUntilIdle()

        verify(saveJournalEntryUseCase).invoke(entry)
    }

    @Test
    fun questsTab_showsEmptyQuests_whenNoQuestsActive() = runTest(testDispatcher) {
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(1L))
        whenever(journalEntryDao.observeAll(1L)).thenReturn(flowOf(emptyList()))
        whenever(questRepository.observeQuestsWithObjectives(1L)).thenReturn(flowOf(emptyList<QuestWithObjectives>()))

        val viewModel = buildViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.selectTab(JournalTab.QUESTS)
        advanceUntilIdle()

        assertEquals(JournalTab.QUESTS, viewModel.uiState.value.selectedTab)
        assertTrue(viewModel.uiState.value.quests.isEmpty())
        assertTrue(viewModel.uiState.value.entries.isEmpty())
    }
}
