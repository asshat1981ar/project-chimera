package com.chimera.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.mapper.toModel
import com.chimera.model.SaveSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val playerName: String = "",
    val chapterTag: String = "prologue",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val saveSlotDao: SaveSlotDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCurrentSave()
    }

    private fun loadCurrentSave() {
        viewModelScope.launch {
            val slotId = gameSessionManager.activeSlotId.value ?: return@launch
            val slot = saveSlotDao.getById(slotId)?.toModel() ?: return@launch
            _uiState.value = HomeUiState(
                playerName = slot.playerName,
                chapterTag = slot.chapterTag,
                isLoading = false
            )
        }
    }
}
