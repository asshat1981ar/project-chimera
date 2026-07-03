package com.chimera.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.data.repository.QuestRepository
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.VowEntity
import com.chimera.domain.usecase.SaveJournalEntryUseCase
import com.chimera.model.JournalEntry
import com.chimera.model.QuestWithObjectives
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class JournalTab(val label: String, val category: String?) {
    ALL("All", null),
    STORY("Story", "story"),
    RUMORS("Rumors", "rumor"),
    QUESTS("Quests", null),
    VOWS("Vows", null),
    COMPANIONS("Companions", "companion")
}

data class JournalUiState(
    val selectedTab: JournalTab = JournalTab.ALL,
    val entries: List<JournalEntryEntity> = emptyList(),
    val vows: List<VowEntity> = emptyList(),
    val quests: List<QuestWithObjectives> = emptyList(),
    val unreadCount: Int = 0,
    val searchQuery: String = ""
)

/** Escapes FTS5 special characters in a query string. */
private fun escapeFtsQuery(raw: String): String =
    "\"${raw.replace("\"", "\"\"")}\"*"   // phrase prefix match; safe for all inputs

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val vowDao: VowDao,
    private val questRepository: QuestRepository,
    private val gameSessionManager: GameSessionManager,
    private val saveJournalEntryUseCase: SaveJournalEntryUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(JournalTab.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _errorMessage = MutableStateFlow<String?>(null)

    /** Exposed for the search bar composable. */
    val searchQuery: StateFlow<String> = _searchQuery

    val errorMessage: StateFlow<String?> = _errorMessage

    val uiState: StateFlow<JournalUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(JournalUiState())

            // Debounce search 300 ms to avoid FTS churn on every keypress
            val debouncedQuery = _searchQuery.debounce(300L)

            combine(
                _selectedTab,
                debouncedQuery,
                vowDao.observeAll(slotId),
                questRepository.observeQuestsWithObjectives(slotId),
                journalEntryDao.observeUnreadCount(slotId)
            ) { tab, rawQuery, vows, quests, unread ->
                val query = rawQuery.trim()
                val entriesFlow = when {
                    // No search — use existing category flows
                    query.isBlank() && tab == JournalTab.ALL ->
                        journalEntryDao.observeAll(slotId)
                    query.isBlank() && tab.category != null ->
                        journalEntryDao.observeByCategory(slotId, tab.category)
                    query.isBlank() ->
                        journalEntryDao.observeAll(slotId)
                    // FTS search with optional category scope
                    tab.category != null ->
                        journalEntryDao.searchEntriesByCategory(slotId, tab.category, escapeFtsQuery(query))
                    else ->
                        journalEntryDao.searchEntries(slotId, escapeFtsQuery(query))
                }
                entriesFlow.map { entries ->
                    val filtered = if (tab == JournalTab.VOWS || tab == JournalTab.QUESTS) emptyList() else entries
                    JournalUiState(
                        selectedTab = tab,
                        entries = filtered,
                        vows = vows,
                        quests = quests,
                        unreadCount = unread,
                        searchQuery = query
                    )
                }
            }.flatMapLatest { it }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JournalUiState())

    fun selectTab(tab: JournalTab) {
        _selectedTab.value = tab
        _searchQuery.value = ""   // clear search on tab switch
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun markRead(entryId: Long) {
        viewModelScope.launch {
            journalEntryDao.markRead(entryId)
        }
    }

    fun saveEntry(entry: JournalEntry) {
        viewModelScope.launch {
            try {
                saveJournalEntryUseCase(entry)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save entry"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
