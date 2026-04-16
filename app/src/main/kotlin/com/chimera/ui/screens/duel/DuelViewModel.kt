package com.chimera.ui.screens.duel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.core.engine.CombatEngine
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.entity.JournalEntryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

enum class CombatPhase { INTENT, RESOLVING, COMPLETE }

data class DuelUiState(
    val opponentName: String = "",
    val opponentArchetype: String = "",
    val phase: CombatPhase = CombatPhase.INTENT,
    val availableIntents: List<CombatEngine.IntentCard> = emptyList(),
    val rollCount: Int = 0,
    val playerResolve: Int = CombatEngine.MAX_RESOLVE,
    val opponentResolve: Int = CombatEngine.MAX_RESOLVE,
    val lastResult: CombatEngine.RollResult? = null,
    val log: List<CombatEngine.RollResult> = emptyList(),
    val isComplete: Boolean = false,
    val playerWon: Boolean? = null,
    val isLoading: Boolean = true
)

@Serializable
private data class IntentsFile(
    val version: Int = 1,
    val intents: Map<String, List<IntentDto>> = emptyMap()
)

@Serializable
private data class IntentDto(
    val id: String,
    val label: String,
    val description: String,
    @SerialName("statBonus") val statBonus: Int = 0,
    @SerialName("requiresDispositionAbove") val requiresDispositionAbove: Float? = null,
    val resultBands: Map<String, String> = emptyMap()
)

@HiltViewModel
class DuelViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val journalEntryDao: JournalEntryDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val opponentId: String = savedStateHandle["opponentId"] ?: "warden"
    private var engine: CombatEngine? = null

    private val _uiState = MutableStateFlow(DuelUiState())
    val uiState: StateFlow<DuelUiState> = _uiState.asStateFlow()

    private val serializer = Json { ignoreUnknownKeys = true }

    init { initializeCombat() }

    private fun initializeCombat() {
        viewModelScope.launch {
            try {
                val character   = characterDao.getById(opponentId)
                val charState   = characterStateDao.getByCharacterId(opponentId)
                val archetype   = charState?.activeArchetype ?: "default"
                val disposition = charState?.dispositionToPlayer ?: 0f
                val opponentName = character?.let {
                    "${it.name}${it.title?.let { t -> " - $t" } ?: ""}"
                } ?: "Unknown Opponent"

                val intentsFile    = loadIntentsFile()
                val intentDtos     = intentsFile.intents[archetype]
                    ?: intentsFile.intents["default"]
                    ?: emptyList()

                val availableIntents = intentDtos
                    .filter { it.requiresDispositionAbove == null || disposition > it.requiresDispositionAbove }
                    .map { dto ->
                        CombatEngine.IntentCard(
                            id          = dto.id,
                            label       = dto.label,
                            description = dto.description,
                            statBonus   = dto.statBonus,
                            requiresDispositionAbove = dto.requiresDispositionAbove,
                            resultBands = dto.resultBands
                        )
                    }

                engine = CombatEngine(
                    playerName        = "You",
                    opponentName      = opponentName,
                    opponentArchetype = archetype,
                    availableIntents  = availableIntents,
                    opponentModifier  = -(disposition * 0.3f).coerceIn(-0.3f, 0.3f)
                )

                _uiState.value = DuelUiState(
                    opponentName      = opponentName,
                    opponentArchetype = archetype,
                    availableIntents  = availableIntents,
                    phase             = CombatPhase.INTENT,
                    isLoading         = false
                )
            } catch (e: Exception) {
                Log.e("DuelVM", "Failed to initialize combat", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun executeIntent(intent: CombatEngine.IntentCard) {
        val eng = engine ?: return
        if (_uiState.value.phase != CombatPhase.INTENT || _uiState.value.isComplete) return

        val result = eng.executeRound(intent)
        val state  = eng.getState()

        _uiState.value = _uiState.value.copy(
            phase           = CombatPhase.RESOLVING,
            rollCount       = state.rollCount,
            playerResolve   = state.playerResolve,
            opponentResolve = state.opponentResolve,
            lastResult      = result,
            log             = state.log,
            isComplete      = state.isComplete,
            playerWon       = state.playerWon
        )

        if (state.isComplete) recordOutcome(state)
    }

    fun acknowledgeResult() {
        val complete = engine?.getState()?.isComplete ?: return
        _uiState.value = _uiState.value.copy(
            phase = if (complete) CombatPhase.COMPLETE else CombatPhase.INTENT
        )
    }

    private fun recordOutcome(state: CombatEngine.CombatState) {
        viewModelScope.launch {
            val slotId = gameSessionManager.activeSlotId.value ?: return@launch
            val won = state.playerWon == true
            journalEntryDao.insert(
                JournalEntryEntity(
                    saveSlotId  = slotId,
                    title       = "Combat: ${_uiState.value.opponentName}",
                    body        = "You faced ${_uiState.value.opponentName} across ${state.rollCount} exchanges. " +
                        "You were ${if (won) "victorious" else "defeated"}. " +
                        (state.lastResult?.narrative ?: ""),
                    category    = "story",
                    characterId = opponentId
                )
            )
            characterStateDao.adjustDisposition(opponentId, if (won) 0.05f else -0.05f)
        }
    }

    private fun loadIntentsFile(): IntentsFile {
        return try {
            val raw = context.assets.open("combat_intents.json").bufferedReader().use { it.readText() }
            serializer.decodeFromString(IntentsFile.serializer(), raw)
        } catch (e: Exception) {
            Log.w("DuelVM", "Could not load combat_intents.json: ${e.message}")
            IntentsFile()
        }
    }
}
