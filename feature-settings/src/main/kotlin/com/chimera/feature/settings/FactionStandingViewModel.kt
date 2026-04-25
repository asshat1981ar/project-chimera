package com.chimera.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.entity.FactionStateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FactionStandingUiState(
    val factions: List<FactionStateEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FactionStandingViewModel @Inject constructor(
    private val factionStateDao: FactionStateDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    val uiState: StateFlow<FactionStandingUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(FactionStandingUiState(isLoading = false))
            factionStateDao.observeAll(slotId)
                .map { factions ->
                    FactionStandingUiState(
                        factions = factions.sortedByDescending { it.influence },
                        isLoading = false
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FactionStandingUiState())

    fun refresh() {
        viewModelScope.launch {
            // Trigger a refresh if needed in the future
            // For now, the Flow handles automatic updates
        }
    }
}
