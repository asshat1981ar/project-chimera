package com.chimera.feature.dialogue

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.ai.AudioProvider
import com.chimera.ai.DialogueOrchestrator
import com.chimera.ai.PortraitGenerationService
import com.chimera.data.GameSessionManager
import com.chimera.data.SceneLoader
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.VowEntity
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
import com.chimera.data.ChimeraPreferences
import com.chimera.domain.usecase.ChapterProgressionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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
    val npcId: String = "",
    val npcName: String = "",
    val npcMood: String = "neutral",
    val npcDisposition: Float = 0f,
    val npcArchetype: String? = null,
    val npcPortraitResName: String? = null,
    val transcript: List<DialogueLine> = emptyList(),
    val quickIntents: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isFallbackMode: Boolean = false,
    val isSceneComplete: Boolean = false,
    val triggerDuelWith: String? = null,
    val relationshipBanner: RelationshipBanner? = null,
    val isSpeaking: Boolean = false,   // true while TTS is playing an NPC line
    val isCinematic: Boolean = false,   // true for narration-only cinematic scenes
    val cinematicIndex: Int = 0,        // current line index in cinematic sequence
    val autoAdvanceTimerMs: Long = 0L   // delay before auto-advancing (0 = disabled)
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
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
    private val orchestrator: DialogueOrchestrator,
    private val gameSessionManager: GameSessionManager,
    private val sceneLoader: SceneLoader,
    private val dialogueTurnDao: DialogueTurnDao,
    private val sceneInstanceDao: SceneInstanceDao,
    private val memoryShardDao: MemoryShardDao,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val journalEntryDao: JournalEntryDao,
    private val saveSlotDao: SaveSlotDao,
    private val vowDao: VowDao,
    private val audioProvider: AudioProvider,
    private val portraitGenerationService: PortraitGenerationService,
    private val preferences: ChimeraPreferences,
    private val chapterProgressionUseCase: ChapterProgressionUseCase
) : ViewModel() {

    private val sceneId: String = savedStateHandle["sceneId"] ?: ""
    private val _uiState = MutableStateFlow(DialogueUiState(sceneId = sceneId))
    val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()

    private val turnResults = mutableListOf<DialogueTurnResult>()
    private var recentMemories = mutableListOf<MemoryShard>()
    private var sceneInstanceId: Long = 0
    private var cachedCharState: CharacterState? = null
    private val portraitRequests = mutableSetOf<String>()

    companion object {
        private const val MAX_TURN_HISTORY = 20
    }

    /** Speaks an NPC line if voiceEnabled in settings. Sets isSpeaking flag around TTS call. */
    private fun speakNpcLine(text: String, npcId: String) {
        viewModelScope.launch {
            val voiceOn = preferences.settings
                .map { it.voiceEnabled }
                .first()
            if (!voiceOn) return@launch
            _uiState.update { it.copy(isSpeaking = true) }
            audioProvider.speak(text, npcId)
            _uiState.update { it.copy(isSpeaking = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioProvider.stop()
        audioProvider.release()
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
                val portraitResName = characterDao.getById(contract.npcId)?.portraitResName
                recentMemories = memoryShardDao.getTopMemories(slotId, contract.npcId, 5)
                    .map { it.toModel() }
                    .toMutableList()

                // Handle cinematic scenes differently - no AI orchestration needed
                if (contract.isCinematic && contract.cinematicLines.isNotEmpty()) {
                    // Load first cinematic line immediately
                    val firstLine = contract.cinematicLines.first()
                    _uiState.value = _uiState.value.copy(
                        transcript = listOf(
                            DialogueLine(
                                speakerId = firstLine.speaker,
                                speakerName = firstLine.speakerName,
                                text = firstLine.text,
                                emotion = firstLine.emotion
                            )
                        ),
                        npcId = contract.npcId,
                        npcMood = firstLine.emotion,
                        npcDisposition = charState.dispositionToPlayer,
                        npcArchetype = charState.activeArchetype,
                        quickIntents = emptyList(), // No intents in cinematic mode
                        isFallbackMode = false,
                        npcPortraitResName = portraitResName,
                        isCinematic = true,
                        cinematicIndex = 0,
                        autoAdvanceTimerMs = contract.autoAdvanceDelayMs,
                        isLoading = false
                    )
                    queueEncounterPortraitGeneration(slotId, firstLine.emotion, charState)
                } else {
                    // Standard AI-driven dialogue scene
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
                        npcId = contract.npcId,
                        npcMood = result.emotion,
                        npcDisposition = charState.dispositionToPlayer,
                        npcArchetype = charState.activeArchetype,
                        quickIntents = intents,
                        isFallbackMode = orchestrator.isFallbackActive,
                        npcPortraitResName = portraitResName,
                        isLoading = false
                    )
                    queueEncounterPortraitGeneration(slotId, result.emotion, charState)
                }
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

                var latestCharState = charState
                if (result.relationshipDelta != 0f) {
                    characterStateDao.adjustDisposition(contract.npcId, result.relationshipDelta)
                    cachedCharState = null
                    latestCharState = loadCharacterState(slotId)
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
                    RelationshipBanner(
                        npcName = contract.npcName,
                        delta = result.relationshipDelta,
                        newDisposition = latestCharState.dispositionToPlayer
                    )
                } else null

                _uiState.value = _uiState.value.copy(
                    transcript = _uiState.value.transcript + playerLine + npcLine,
                    npcMood = result.emotion,
                    npcDisposition = latestCharState.dispositionToPlayer,
                    quickIntents = intents,
                    isFallbackMode = orchestrator.isFallbackActive,
                    isSceneComplete = isEnding,
                    triggerDuelWith = if (triggerDuel) contract.npcId else null,
                    relationshipBanner = banner,
                    isLoading = false
                )
                queueEncounterPortraitGeneration(slotId, result.emotion, latestCharState)

                // Speak NPC line aloud if voice is enabled (fire-and-forget, stops on scene complete)
                if (!isEnding) speakNpcLine(result.npcLine, contract.npcId)

                if (isEnding) {
                    sceneInstanceDao.completeScene(
                        id = sceneInstanceId,
                        turnCount = turnResults.size,
                        usedFallback = orchestrator.isFallbackActive
                    )
                    generateJournalEntry(slotId)
                    generateVows(slotId)
                    chapterProgressionUseCase()
                    // Persist accumulated playtime
                    val sessionSeconds = gameSessionManager.getSessionPlaytimeSeconds()
                    val slot = saveSlotDao.getById(slotId)
                    if (slot != null) {
                        saveSlotDao.upsert(
                            slot.copy(
                                playtimeSeconds = slot.playtimeSeconds + sessionSeconds,
                                lastPlayedAt = System.currentTimeMillis()
                            )
                        )
                    }
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

    /** Advance to the next cinematic line, or complete the scene if at the end. */
    fun advanceCinematic() {
        if (!_uiState.value.isCinematic) return

        val currentIndex = _uiState.value.cinematicIndex
        val lines = contract.cinematicLines

        if (currentIndex >= lines.size - 1) {
            // End of cinematic - mark complete and trigger chapter progression
            completeCinematicScene()
        } else {
            // Advance to next line
            val nextIndex = currentIndex + 1
            val nextLine = lines[nextIndex]
            _uiState.value = _uiState.value.copy(
                transcript = _uiState.value.transcript + DialogueLine(
                    speakerId = nextLine.speaker,
                    speakerName = nextLine.speakerName,
                    text = nextLine.text,
                    emotion = nextLine.emotion
                ),
                cinematicIndex = nextIndex,
                npcMood = nextLine.emotion
            )
            // Speak the line if voice is enabled
            speakNpcLine(nextLine.text, nextLine.speaker)
        }
    }

    /** Mark the cinematic scene as complete and trigger chapter progression. */
    private fun completeCinematicScene() {
        viewModelScope.launch {
            try {
                val slotId = gameSessionManager.activeSlotId.value ?: return@launch

                // Mark scene as complete in database
                sceneInstanceDao.completeScene(
                    id = sceneInstanceId,
                    turnCount = contract.cinematicLines.size,
                    usedFallback = false
                )

                // Persist the cinematic lines as dialogue turns
                contract.cinematicLines.forEach { line ->
                    dialogueTurnDao.insert(
                        DialogueTurnEntity(
                            saveSlotId = slotId,
                            sceneId = sceneId,
                            speakerId = line.speaker,
                            lineText = line.text,
                            emotionJson = "{\"primary\":\"${line.emotion}\"}"
                        )
                    )
                }

                // Mark the bridge tag complete via chapter progression use case
                contract.onCompleteTag?.let { tag ->
                    chapterProgressionUseCase.markCinematicComplete(sceneId)
                }

                // Trigger chapter progression to update state
                chapterProgressionUseCase()

                _uiState.value = _uiState.value.copy(isSceneComplete = true)
            } catch (e: Exception) {
                Log.e("DialogueVM", "Failed to complete cinematic scene", e)
            }
        }
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

        // Story entry (always)
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

        // Companion entry if recruitment happened
        if (turnResults.any { it.flags.contains("recruit_companion") }) {
            journalEntryDao.insert(
                JournalEntryEntity(
                    saveSlotId = slotId,
                    title = "${contract.npcName} Joins",
                    body = "${contract.npcName} has agreed to join your cause.",
                    category = "companion",
                    characterId = contract.npcId
                )
            )
        }

        // Companion disposition entry if relationship changed significantly
        val totalDelta = turnResults.sumOf { it.relationshipDelta.toDouble() }.toFloat()
        if (kotlin.math.abs(totalDelta) >= 0.1f) {
            val direction = if (totalDelta > 0) "warmed to" else "grown colder toward"
            journalEntryDao.insert(
                JournalEntryEntity(
                    saveSlotId = slotId,
                    title = "${contract.npcName}'s Regard",
                    body = "${contract.npcName} has $direction you after your exchange at ${contract.setting}.",
                    category = "companion",
                    characterId = contract.npcId
                )
            )
        }
    }

    private suspend fun generateVows(slotId: Long) {
        val allFlags = turnResults.flatMap { it.flags }.toSet()
        val disposition = cachedCharState?.dispositionToPlayer ?: 0f

        // Authored vow triggers based on scene + flags + disposition
        val vows = mutableListOf<VowEntity>()

        if (contract.npcId == "warden" && disposition > 0.3f && allFlags.contains("scene_ending")) {
            vows.add(VowEntity(
                saveSlotId = slotId,
                description = "Protect the Hollow from those who would exploit it",
                swornTo = "warden",
                sceneIdOrigin = sceneId
            ))
        }
        if (contract.npcId == "aria" && allFlags.contains("scene_ending")) {
            vows.add(VowEntity(
                saveSlotId = slotId,
                description = "Find the source of the corruption in the deep ruins",
                swornTo = "aria",
                sceneIdOrigin = sceneId
            ))
        }
        if (contract.sceneId == "elena_recruitment" && allFlags.contains("recruit_companion")) {
            vows.add(VowEntity(
                saveSlotId = slotId,
                description = "Return what was taken from Elena's hidden cache",
                swornTo = "elena",
                sceneIdOrigin = sceneId
            ))
        }

        vows.forEach { vowDao.insert(it) }
    }

    private fun queueEncounterPortraitGeneration(
        slotId: Long,
        mood: String,
        charState: CharacterState
    ) {
        val signature = portraitSignature(contract.npcId, mood, charState)
        if (!portraitRequests.add(signature)) return

        viewModelScope.launch {
            try {
                val generatedPath = generateEncounterPortrait(slotId, mood, charState, signature)
                if (!generatedPath.isNullOrBlank() && _uiState.value.npcId == contract.npcId) {
                    _uiState.update { it.copy(npcPortraitResName = generatedPath) }
                }
            } catch (e: Exception) {
                Log.w("DialogueVM", "Portrait generation failed for ${contract.npcId}: ${e.message}")
            } finally {
                portraitRequests.remove(signature)
            }
        }
    }

    private suspend fun generateEncounterPortrait(
        slotId: Long,
        mood: String,
        charState: CharacterState,
        signature: String
    ): String? = withContext(Dispatchers.IO) {
        val character = characterDao.getById(contract.npcId) ?: return@withContext null
        if (character.saveSlotId != slotId || character.isPlayerCharacter) return@withContext null

        val existingPath = character.portraitResName
        if (!existingPath.isNullOrBlank() && existingPath.contains(signature)) {
            val existingFile = File(existingPath.removePrefix("file://"))
            if (existingFile.exists()) return@withContext existingPath
        }

        val status = portraitStatus(charState)
        val bytes = portraitGenerationService.generatePortrait(
            npcName = character.name,
            npcRole = character.role,
            npcTitle = character.title,
            identityKey = character.id,
            mood = mood,
            status = status,
            disposition = charState.dispositionToPlayer,
            archetype = charState.activeArchetype,
            healthFraction = charState.healthFraction
        ) ?: return@withContext null

        val portraitDir = File(appContext.filesDir, "portraits").apply { mkdirs() }
        val file = File(portraitDir, "npc_${signature}.jpg")
        file.writeBytes(bytes)

        characterDao.updatePortraitResName(character.id, file.absolutePath)
        file.absolutePath
    }

    private fun portraitSignature(
        npcId: String,
        mood: String,
        charState: CharacterState
    ): String {
        val dispositionBand = when {
            charState.dispositionToPlayer > 0.35f -> "trusted"
            charState.dispositionToPlayer < -0.35f -> "hostile"
            else -> "neutral"
        }
        return listOf(
            npcId,
            sanitizePortraitPart(mood),
            sanitizePortraitPart(portraitStatus(charState)),
            dispositionBand,
            sanitizePortraitPart(charState.activeArchetype ?: "none")
        ).joinToString("_")
    }

    private fun portraitStatus(charState: CharacterState): String = when {
        charState.healthFraction < 0.35f -> "wounded"
        charState.healthFraction < 0.7f -> "fatigued"
        else -> "steady"
    }

    private fun sanitizePortraitPart(value: String): String =
        value.lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "unknown" }

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
