package com.chimera.ui.screens.camp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.VowEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CompanionCardData(
    val character: CharacterEntity,
    val state: CharacterStateEntity?
)

data class CampUiState(
    val day: Int = 1,
    val morale: Float = 0.5f,
    val companions: List<CompanionCardData> = emptyList(),
    val activeVows: List<VowEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CampViewModel @Inject constructor(
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val vowDao: VowDao,
    gameSessionManager: GameSessionManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CampUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(CampUiState(isLoading = false))
            combine(
                characterDao.observeCompanions(slotId),
                characterStateDao.observeBySlot(slotId),
                vowDao.observeActive(slotId)
            ) { companions, states, vows ->
                val stateMap = states.associateBy { it.characterId }
                val companionCards = companions.map { char ->
                    CompanionCardData(char, stateMap[char.id])
                }
                val avgDisposition = states
                    .filter { it.characterId != "player" }
                    .map { it.dispositionToPlayer }
                    .takeIf { it.isNotEmpty() }
                    ?.average()?.toFloat() ?: 0.5f
                val morale = ((avgDisposition + 1f) / 2f).coerceIn(0f, 1f)

                CampUiState(
                    day = 1, // TODO: derive from scene count
                    morale = morale,
                    companions = companionCards,
                    activeVows = vows,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CampUiState())
}
