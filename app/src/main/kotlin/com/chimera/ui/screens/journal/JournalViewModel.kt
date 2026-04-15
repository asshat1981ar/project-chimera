package com.chimera.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.VowEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class JournalTab(val label: String, val category: String?) {
    ALL("All", null),
    STORY("Story", "story"),
    RUMORS("Rumors", "rumor"),
    VOWS("Vows", null),
    COMPANIONS("Companions", "companion")
}

data class JournalUiState(
    val selectedTab: JournalTab = JournalTab.ALL,
    val entries: List<JournalEntryEntity> = emptyList(),
    val vows: List<VowEntity> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalEntryDao: JournalEntryDao,
    private val vowDao: VowDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(JournalTab.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<JournalUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(JournalUiState())
            combine(
                _selectedTab,
                journalEntryDao.observeAll(slotId),
                vowDao.observeAll(slotId),
                journalEntryDao.observeUnreadCount(slotId)
            ) { tab, allEntries, vows, unread ->
                val filtered = when {
                    tab == JournalTab.ALL -> allEntries
                    tab == JournalTab.VOWS -> emptyList()
                    tab.category != null -> allEntries.filter { it.category == tab.category }
                    else -> allEntries
                }
                JournalUiState(
                    selectedTab = tab,
                    entries = filtered,
                    vows = vows,
                    unreadCount = unread
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JournalUiState())

    fun selectTab(tab: JournalTab) {
        _selectedTab.value = tab
    }

    fun markRead(entryId: Long) {
        viewModelScope.launch {
            journalEntryDao.markRead(entryId)
        }
    }
}
