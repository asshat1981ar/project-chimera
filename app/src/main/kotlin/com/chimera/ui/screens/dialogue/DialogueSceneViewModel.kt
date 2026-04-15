package com.chimera.ui.screens.dialogue

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.ai.DialogueOrchestrator
import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.DialogueTurnDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.entity.DialogueTurnEntity
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.database.entity.SceneInstanceEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.CharacterState
import com.chimera.model.DialogueTurnResult
import com.chimera.model.MemoryShard
import com.chimera.model.PlayerInput
import com.chimera.model.SceneContract
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DialogueLine(
    val speakerId: String,
    val speakerName: String,
    val text: String,
    val emotion: String = "neutral",
    val isPlayer: Boolean = false
)

data class DialogueUiState(
    val sceneId: String = "",
    val sceneTitle: String = "",
    val npcName: String = "",
    val npcMood: String = "neutral",
    val transcript: List<DialogueLine> = emptyList(),
    val quickIntents: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isFallbackMode: Boolean = false,
    val isSceneComplete: Boolean = false,
    val triggerDuelWith: String? = null,
    val relationshipBanner: RelationshipBanner? = null
)

data class RelationshipBanner(
    val npcName: String,
    val delta: Float,
    val newDisposition: Float
) {
    companion object {
        const val SIGNIFICANCE_THRESHOLD = 0.05f
    }
}

@HiltViewModel
class DialogueSceneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orchestrator: DialogueOrchestrator,
    private val gameSessionManager: GameSessionManager,
    private val sceneLoader: SceneLoader,
    private val dialogueTurnDao: DialogueTurnDao,
    private val sceneInstanceDao: SceneInstanceDao,
    private val memoryShardDao: MemoryShardDao,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val journalEntryDao: JournalEntryDao
) : ViewModel() {

    private val sceneId: String = savedStateHandle["sceneId"] ?: ""
    private val _uiState = MutableStateFlow(DialogueUiState(sceneId = sceneId))
    val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()

    private val turnResults = mutableListOf<DialogueTurnResult>()
    private var recentMemories = mutableListOf<MemoryShard>()
    private var sceneInstanceId: Long = 0
    private var cachedCharState: CharacterState? = null

    companion object {
        private const val MAX_TURN_HISTORY = 20
    }

    private val contract: SceneContract = sceneLoader.getScene(sceneId) ?: SceneContract(
        sceneId = sceneId,
        sceneTitle = "Unknown Scene",
        npcId = "unknown",
        npcName = "Stranger",
        setting = "an unfamiliar place"
    )

    init {
        initializeScene()
    }

    private fun initializeScene() {
        viewModelScope.launch {
            try {
                val slotId = gameSessionManager.activeSlotId.value ?: return@launch
                _uiState.value = _uiState.value.copy(
                    sceneTitle = contract.sceneTitle,
                    npcName = contract.npcName,
                    isLoading = true
                )

                sceneInstanceId = sceneInstanceDao.insert(
                    SceneInstanceEntity(
                        saveSlotId = slotId,
                        sceneId = sceneId,
                        npcId = contract.npcId
                    )
                )

                val charState = loadCharacterState(slotId)
                recentMemories = memoryShardDao.getTopMemories(slotId, contract.npcId, 5)
                    .map { it.toModel() }
                    .toMutableList()

                val openingInput = PlayerInput(text = "[Scene begins]", isQuickIntent = true)
                val result = orchestrator.generateTurn(
                    contract, openingInput, charState, recentMemories, turnResults
                )
                turnResults.add(result)

                persistTurn(contract.npcId, result.npcLine, result.emotion)

                val intents = orchestrator.generateIntents(contract, charState, turnResults)

                _uiState.value = _uiState.value.copy(
                    transcript = listOf(
                        DialogueLine(
                            speakerId = contract.npcId,
                            speakerName = contract.npcName,
                            text = result.npcLine,
                            emotion = result.emotion
                        )
                    ),
                    npcMood = result.emotion,
                    quickIntents = intents,
                    isFallbackMode = orchestrator.isFallbackActive,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("DialogueVM", "Failed to initialize scene", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun selectIntent(intent: String) {
        submitPlayerInput(PlayerInput(text = intent, isQuickIntent = true))
    }

    fun submitTypedInput(text: String) {
        if (text.isBlank()) return
        submitPlayerInput(PlayerInput(text = text.trim()))
    }

    private fun submitPlayerInput(input: PlayerInput) {
        if (_uiState.value.isLoading || _uiState.value.isSceneComplete) return

        viewModelScope.launch {
            try {
                val slotId = gameSessionManager.activeSlotId.value ?: return@launch
                _uiState.value = _uiState.value.copy(isLoading = true, quickIntents = emptyList())

                val playerLine = DialogueLine(
                    speakerId = "player",
                    speakerName = "You",
                    text = input.text,
                    isPlayer = true
                )
                persistTurn("player", input.text, "")

                val charState = loadCharacterState(slotId)

                val result = orchestrator.generateTurn(
                    contract, input, charState, recentMemories, turnResults
                )
                addTurnResult(result)

                persistTurn(contract.npcId, result.npcLine, result.emotion)

                // Batch insert memory candidates
                if (result.memoryCandidates.isNotEmpty()) {
                    val shards = result.memoryCandidates.map { summary ->
                        MemoryShardEntity(
                            saveSlotId = slotId,
                            sceneId = sceneId,
                            characterId = contract.npcId,
                            summary = summary,
                            importanceScore = 0.6f
                        )
                    }
                    memoryShardDao.insertAll(shards)
                    recentMemories.addAll(shards.map { it.toModel() })
                }

                if (result.relationshipDelta != 0f) {
                    characterStateDao.adjustDisposition(contract.npcId, result.relationshipDelta)
                    cachedCharState = null
                }

                // Companion recruitment: promote NPC role to COMPANION
                if (result.flags.contains("recruit_companion")) {
                    val existing = characterDao.getById(contract.npcId)
                    if (existing != null && existing.role != "COMPANION") {
                        characterDao.upsert(existing.copy(role = "COMPANION"))
                    }
                }

                val npcLine = DialogueLine(
                    speakerId = contract.npcId,
                    speakerName = contract.npcName,
                    text = result.npcLine,
                    emotion = result.emotion
                )

                val isEnding = result.flags.contains("scene_ending") ||
                    turnResults.size >= contract.maxTurns
                val triggerDuel = result.flags.contains("trigger_duel")

                val intents = if (isEnding) {
                    listOf("Farewell.", "[Leave]")
                } else {
                    orchestrator.generateIntents(contract, charState, turnResults)
                }

                val banner = if (kotlin.math.abs(result.relationshipDelta) >= RelationshipBanner.SIGNIFICANCE_THRESHOLD) {
                    val updated = loadCharacterState(slotId)
                    RelationshipBanner(
                        npcName = contract.npcName,
                        delta = result.relationshipDelta,
                        newDisposition = updated.dispositionToPlayer
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    transcript = _uiState.value.transcript + playerLine + npcLine,
                    npcMood = result.emotion,
                    quickIntents = intents,
                    isFallbackMode = orchestrator.isFallbackActive,
                    isSceneComplete = isEnding,
                    triggerDuelWith = if (triggerDuel) contract.npcId else null,
                    relationshipBanner = banner,
                    isLoading = false
                )

                if (isEnding) {
                    sceneInstanceDao.completeScene(
                        id = sceneInstanceId,
                        turnCount = turnResults.size,
                        usedFallback = orchestrator.isFallbackActive
                    )
                    generateJournalEntry(slotId)
                }
            } catch (e: Exception) {
                Log.e("DialogueVM", "Failed to process turn", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun dismissRelationshipBanner() {
        _uiState.value = _uiState.value.copy(relationshipBanner = null)
    }

    private suspend fun loadCharacterState(slotId: Long): CharacterState {
        cachedCharState?.let { return it }
        val state = characterStateDao.getByCharacterId(contract.npcId)?.toModel()
            ?: CharacterState(characterId = contract.npcId, saveSlotId = slotId)
        cachedCharState = state
        return state
    }

    private fun addTurnResult(result: DialogueTurnResult) {
        turnResults.add(result)
        // Keep only recent history to bound memory
        if (turnResults.size > MAX_TURN_HISTORY) {
            turnResults.removeAt(0)
        }
    }

    private suspend fun generateJournalEntry(slotId: Long) {
        val npcLines = _uiState.value.transcript.filter { !it.isPlayer }
        val summary = if (npcLines.size > 1) {
            "Spoke with ${contract.npcName} at ${contract.setting}. " +
            "The conversation spanned ${turnResults.size} exchanges."
        } else {
            "A brief encounter with ${contract.npcName}."
        }
        journalEntryDao.insert(
            JournalEntryEntity(
                saveSlotId = slotId,
                title = contract.sceneTitle,
                body = summary,
                category = "story",
                sceneId = sceneId,
                characterId = contract.npcId
            )
        )
    }

    private suspend fun persistTurn(speakerId: String, text: String, emotion: String) {
        val slotId = gameSessionManager.activeSlotId.value ?: return
        dialogueTurnDao.insert(
            DialogueTurnEntity(
                saveSlotId = slotId,
                sceneId = sceneId,
                speakerId = speakerId,
                lineText = text,
                emotionJson = "{\"primary\":\"$emotion\"}"
            )
        )
    }
}
