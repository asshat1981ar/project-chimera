package com.chimera.ui.screens.duel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.entity.JournalEntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuelUiState(
    val playerName: String = "You",
    val opponentName: String = "Opponent",
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
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val journalEntryDao: JournalEntryDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val opponentId: String = savedStateHandle["opponentId"] ?: "warden"
    private var engine: DuelEngine? = null

    private val _uiState = MutableStateFlow(DuelUiState())
    val uiState: StateFlow<DuelUiState> = _uiState.asStateFlow()

    init {
        initializeDuel()
    }

    private fun initializeDuel() {
        viewModelScope.launch {
            val character = characterDao.getById(opponentId)
            val charState = characterStateDao.getByCharacterId(opponentId)
            val opponentName = character?.let { "${it.name}${it.title?.let { t -> " $t" } ?: ""}" }
                ?: "Unknown Opponent"

            // Derive modifier from disposition: positive disposition = slight advantage
            val disposition = charState?.dispositionToPlayer ?: 0f
            val modifier = (disposition * 0.2f).coerceIn(-0.3f, 0.3f)

            engine = DuelEngine(
                playerName = "You",
                opponentName = opponentName,
                opponentResolve = 3,
                playerModifier = modifier
            )

            _uiState.value = DuelUiState(
                opponentName = opponentName,
                playerOmens = engine!!.getState().playerOmens,
                opponentResolve = engine!!.getState().opponentResolve
            )
        }
    }

    fun selectStance(stance: DuelEngine.Stance) {
        val eng = engine ?: return
        if (_uiState.value.isComplete) return

        val result = eng.executeRound(stance)
        val state = eng.getState()

        _uiState.value = DuelUiState(
            opponentName = _uiState.value.opponentName,
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
            val opponentName = _uiState.value.opponentName
            journalEntryDao.insert(
                JournalEntryEntity(
                    saveSlotId = slotId,
                    title = "Ritual Duel: $opponentName",
                    body = "The ritual duel with $opponentName concluded after ${state.round} rounds. " +
                        "You were $outcome. ${state.log.lastOrNull()?.narrative ?: ""}",
                    category = "story",
                    characterId = opponentId
                )
            )

            // Duel outcome affects disposition
            val delta = if (state.playerWon == true) 0.1f else -0.05f
            characterStateDao.adjustDisposition(opponentId, delta)
        }
    }
}
