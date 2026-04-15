package com.chimera.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.mapper.toModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val playerName: String = "",
    val chapterTag: String = "prologue",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    saveSlotDao: SaveSlotDao,
    gameSessionManager: GameSessionManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) {
                flowOf(HomeUiState(isLoading = false))
            } else {
                saveSlotDao.observeAll().map { slots ->
                    val slot = slots.find { it.id == slotId }?.toModel()
                    if (slot != null) {
                        HomeUiState(
                            playerName = slot.playerName,
                            chapterTag = slot.chapterTag,
                            isLoading = false
                        )
                    } else {
                        HomeUiState(isLoading = false)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
