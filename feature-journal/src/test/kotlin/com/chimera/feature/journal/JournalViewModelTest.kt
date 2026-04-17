package com.chimera.feature.journal

import com.chimera.data.GameSessionManager
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.domain.usecase.SaveJournalEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
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
    private val gameSessionManager: GameSessionManager = mock()
    private val saveJournalEntryUseCase: SaveJournalEntryUseCase = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(gameSessionManager.activeSlotId).thenReturn(MutableStateFlow(null))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = JournalViewModel(
        journalEntryDao = journalEntryDao,
        vowDao = vowDao,
        gameSessionManager = gameSessionManager,
        saveJournalEntryUseCase = saveJournalEntryUseCase
    )

    private fun fakeEntry(category: String = "story") = JournalEntryEntity(
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
}
