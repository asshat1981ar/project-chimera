package com.chimera.ui.screens.duel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.data.GameSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuelUiState(
    val playerName: String = "You",
    val opponentName: String = "The Warden",
    val round: Int = 0,
    val playerOmens: Int = 2,
    val opponentResolve: Int = 3,
    val lastResult: DuelEngine.RoundResult? = null,
    val log: List<DuelEngine.RoundResult> = emptyList(),
    val isComplete: Boolean = false,
    val playerWon: Boolean? = null
)

@HiltViewModel
class DuelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val journalEntryDao: JournalEntryDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val opponentId: String = savedStateHandle["opponentId"] ?: "warden"
    private val opponentName: String = "The Warden" // TODO: look up from character DB

    private val engine = DuelEngine(
        playerName = "You",
        opponentName = opponentName,
        opponentResolve = 3,
        playerModifier = 0f // TODO: derive from prior dialogue choices
    )

    private val _uiState = MutableStateFlow(
        DuelUiState(
            opponentName = opponentName,
            playerOmens = engine.getState().playerOmens,
            opponentResolve = engine.getState().opponentResolve
        )
    )
    val uiState: StateFlow<DuelUiState> = _uiState.asStateFlow()

    fun selectStance(stance: DuelEngine.Stance) {
        if (_uiState.value.isComplete) return

        val result = engine.executeRound(stance)
        val state = engine.getState()

        _uiState.value = DuelUiState(
            opponentName = opponentName,
            round = state.round,
            playerOmens = state.playerOmens,
            opponentResolve = state.opponentResolve,
            lastResult = result,
            log = state.log,
            isComplete = state.isComplete,
            playerWon = state.playerWon
        )

        if (state.isComplete) {
            recordOutcome(state)
        }
    }

    private fun recordOutcome(state: DuelEngine.DuelState) {
        viewModelScope.launch {
            val slotId = gameSessionManager.activeSlotId.value ?: return@launch
            val outcome = if (state.playerWon == true) "victorious" else "defeated"
            journalEntryDao.insert(
                JournalEntryEntity(
                    saveSlotId = slotId,
                    title = "Ritual Duel: $opponentName",
                    body = "The ritual duel with $opponentName concluded after ${state.round} rounds. You were $outcome. ${state.log.lastOrNull()?.narrative ?: ""}",
                    category = "story",
                    characterId = opponentId
                )
            )
        }
    }
}
