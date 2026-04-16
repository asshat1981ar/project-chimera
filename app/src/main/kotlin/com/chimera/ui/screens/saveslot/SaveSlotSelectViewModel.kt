package com.chimera.ui.screens.saveslot

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.chimera.data.ChimeraPreferences
import com.chimera.data.CraftingRecipeSeeder
import com.chimera.data.FactionSeeder
import com.chimera.data.GameSessionManager
import com.chimera.data.MultiActNpcSeeder
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.network.CloudSaveRepository
import com.chimera.network.CloudSaveRequest
import com.chimera.network.SaveDataSnapshot
import com.chimera.network.SnapshotCharacter
import com.chimera.network.SnapshotCharacterState
import com.chimera.network.SnapshotFactionStanding
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.database.mapper.toModel
import com.chimera.model.SaveSlot
import com.chimera.workers.NpcPortraitSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaveSlotSelectViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saveSlotDao: SaveSlotDao,
    private val characterDao: CharacterDao,
    private val characterStateDao: CharacterStateDao,
    private val factionStateDao: FactionStateDao,
    private val sceneInstanceDao: SceneInstanceDao,
    private val multiActNpcSeeder: MultiActNpcSeeder,
    private val craftingRecipeSeeder: CraftingRecipeSeeder,
    private val factionSeeder: FactionSeeder,
    private val gameSessionManager: GameSessionManager,
    private val cloudSaveRepository: CloudSaveRepository,
    private val preferences: ChimeraPreferences
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /** True while downloading a newer cloud save — disables slot tap during restore. */
    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    val saveSlots: StateFlow<List<SaveSlot>> = saveSlotDao.observeAll()
        .map { entities -> entities.map { it.toModel() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNewGame(slotIndex: Int, playerName: String, onCreated: (Long) -> Unit) {
        if (playerName.isBlank()) return
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val existing = saveSlotDao.getByIndex(slotIndex)
                val slotId = saveSlotDao.upsert(
                    SaveSlotEntity(
                        id = existing?.id ?: 0,
                        slotIndex = slotIndex,
                        playerName = playerName.trim(),
                        chapterTag = "prologue",
                        playtimeSeconds = 0,
                        lastPlayedAt = now,
                        createdAt = now,
                        isEmpty = false
                    )
                )
                characterDao.upsert(
                    CharacterEntity(
                        id = "player_${slotId}",
                        saveSlotId = slotId,
                        name = playerName.trim(),
                        role = "PROTAGONIST",
                        isPlayerCharacter = true
                    )
                )
                // Seed all data for the new slot: NPCs (all acts), crafting recipes, factions
                multiActNpcSeeder.seedNpcsForSlot(slotId)
                craftingRecipeSeeder.seedRecipesForSlot()
                factionSeeder.seedFactionsForSlot(slotId)
                gameSessionManager.setActiveSlot(slotId)

                // Fire-and-forget cloud sync with full snapshot
                viewModelScope.launch {
                    val saveDataJson = buildSnapshot(slotId, "prologue", 0L)
                    cloudSaveRepository.uploadSave(
                        CloudSaveRequest(
                            slotId          = slotId,
                            playerName      = playerName.trim(),
                            chapterTag      = "prologue",
                            playtimeSeconds = 0,
                            saveDataJson    = saveDataJson
                        )
                    )
                }

                onCreated(slotId)

                // Schedule background portrait generation — requires network, runs once per slot
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "${NpcPortraitSyncWorker.WORK_NAME}_$slotId",
                    ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<NpcPortraitSyncWorker>()
                        .setInputData(workDataOf(NpcPortraitSyncWorker.KEY_SLOT_ID to slotId))
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                )
            } catch (e: Exception) {
                Log.e("SaveSlotVM", "Failed to create new game", e)
                _error.value = "Failed to create save"
            }
        }
    }

    fun selectSlot(slotId: Long, onSelected: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val slot = saveSlotDao.getById(slotId) ?: return@launch
                if (slot.isEmpty) return@launch

                // Stage 1: Check — is cloud sync enabled?
                val cloudEnabled = preferences.settings.first().cloudSyncEnabled
                if (cloudEnabled) {
                    // Stage 2: Branch — fetch cloud save and compare timestamps
                    _isRestoring.value = true
                    val cloudResult = cloudSaveRepository.downloadSave(slotId)
                    val cloudSave = cloudResult.getOrNull()

                    if (cloudSave != null && cloudSave.updatedAt > slot.lastPlayedAt) {
                        // Stage 3: Apply — cloud is newer, update local Room entities
                        Log.d("SaveSlotVM", "Cloud save newer for slot $slotId, restoring")
                        saveSlotDao.upsert(
                            slot.copy(
                                playerName      = cloudSave.playerName,
                                chapterTag      = cloudSave.chapterTag,
                                playtimeSeconds = cloudSave.playtimeSeconds,
                                lastPlayedAt    = cloudSave.updatedAt
                            )
                        )
                        // Stage 4: Verify — Room is now up to date; proceed
                    } else if (cloudSave != null && cloudSave.updatedAt < slot.lastPlayedAt) {
                        // Local is newer — push full snapshot to cloud
                        viewModelScope.launch {
                            val saveDataJson = buildSnapshot(slotId, slot.chapterTag, slot.playtimeSeconds)
                            cloudSaveRepository.uploadSave(
                                CloudSaveRequest(
                                    slotId          = slotId,
                                    playerName      = slot.playerName,
                                    chapterTag      = slot.chapterTag,
                                    playtimeSeconds = slot.playtimeSeconds,
                                    saveDataJson    = saveDataJson
                                )
                            )
                        }
                    }
                    _isRestoring.value = false
                }

                saveSlotDao.upsert(slot.copy(lastPlayedAt = System.currentTimeMillis()))
                gameSessionManager.setActiveSlot(slotId)
                onSelected(slotId)

            } catch (e: Exception) {
                _isRestoring.value = false
                Log.e("SaveSlotVM", "Failed to select slot", e)
                _error.value = "Failed to load save"
            }
        }
    }

    fun deleteSave(slotId: Long) {
        viewModelScope.launch {
            try {
                val slot = saveSlotDao.getById(slotId) ?: return@launch
                saveSlotDao.upsert(
                    slot.copy(
                        playerName      = "",
                        chapterTag      = "prologue",
                        playtimeSeconds = 0,
                        isEmpty         = true
                    )
                )
                characterDao.deleteBySlot(slotId)

                // Delete from cloud (best-effort)
                viewModelScope.launch { cloudSaveRepository.deleteSave(slotId) }

            } catch (e: Exception) {
                Log.e("SaveSlotVM", "Failed to delete save", e)
                _error.value = "Failed to delete save"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Builds a [SaveDataSnapshot] from live Room data for [slotId].
     * Called before every cloud upload so the cloud save always has a full snapshot.
     * PromptForge SEQUENCE: load characters → load states → load completed scenes → load factions → serialize
     */
    private suspend fun buildSnapshot(slotId: Long, chapterTag: String, playtimeSeconds: Long): String {
        return try {
            val characters = characterDao.getBySlot(slotId)
            val states = characterStateDao.getBySlot(slotId)
            val completedScenes = sceneInstanceDao.getBySlot(slotId)
                .filter { it.status == "completed" }
                .map { it.sceneId }
            val factions = factionStateDao.getBySlot(slotId)

            val snapshot = SaveDataSnapshot(
                chapterTag      = chapterTag,
                playtimeSeconds = playtimeSeconds,
                characters      = characters.map { c ->
                    SnapshotCharacter(
                        id          = c.id,
                        name        = c.name,
                        title       = c.title,
                        role        = c.role,
                        isPlayer    = c.isPlayerCharacter,
                        portraitUrl = c.portraitResName
                    )
                },
                characterStates = states.map { s ->
                    SnapshotCharacterState(
                        characterId        = s.characterId,
                        disposition        = s.dispositionToPlayer,
                        activeArchetype    = s.activeArchetype,
                        lastInteractionEpoch = s.lastInteractionEpoch
                    )
                },
                completedScenes  = completedScenes,
                factionStandings = factions.map { f ->
                    SnapshotFactionStanding(
                        factionId      = f.factionId,
                        playerStanding = f.playerStanding,
                        worldInfluence = f.influence
                    )
                }
            )
            kotlinx.serialization.json.Json.encodeToString(SaveDataSnapshot.serializer(), snapshot)
        } catch (e: Exception) {
            Log.w("SaveSlotVM", "buildSnapshot failed, using empty JSON: ${e.message}")
            "{}"
        }
    }
}
