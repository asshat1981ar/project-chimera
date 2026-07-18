package com.chimera.feature.camp

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.ai.PortraitGenerationService
import com.chimera.ai.loadoutHash
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterEquipmentDao
import com.chimera.database.dao.InventoryDao
import com.chimera.database.entity.CharacterEquipmentEntity
import com.chimera.database.entity.InventoryItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class InventoryCategory(val label: String, val dbKey: String?) {
    ALL("All", null),
    ARTIFACT("Artifacts", "artifact"),
    CONSUMABLE("Consumables", "consumable"),
    KEY_ITEM("Key Items", "key_item"),
    MATERIAL("Materials", "material")
}

data class InventoryUiState(
    val items: List<InventoryItemEntity> = emptyList(),
    val selectedCategory: InventoryCategory = InventoryCategory.ALL,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val playerCharacterId: String? = null,
    val equippedItemIds: Set<String> = emptySet()
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val characterDao: CharacterDao,
    private val characterEquipmentDao: CharacterEquipmentDao,
    private val portraitService: PortraitGenerationService,
    private val gameSessionManager: GameSessionManager,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(InventoryCategory.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InventoryUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(InventoryUiState(isLoading = false))
            characterDao.observePlayerCharacter(slotId).flatMapLatest { playerCharacter ->
                val equippedFlow = playerCharacter?.let { characterEquipmentDao.observeByCharacter(it.id) }
                    ?: flowOf(emptyList())
                combine(
                    inventoryDao.observeAll(slotId),
                    inventoryDao.observeItemCount(slotId),
                    _selectedCategory,
                    equippedFlow
                ) { allItems, totalCount, category, equipped ->
                    val filtered = when (category) {
                        InventoryCategory.ALL -> allItems
                        else -> allItems.filter { it.category == category.dbKey }
                    }
                    InventoryUiState(
                        items = filtered,
                        selectedCategory = category,
                        totalCount = totalCount,
                        isLoading = false,
                        playerCharacterId = playerCharacter?.id,
                        equippedItemIds = equipped.map { it.itemId }.toSet()
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState())

    fun selectCategory(category: InventoryCategory) {
        _selectedCategory.value = category
    }

    /** Equips [item] into its equipSlot for the player, replacing anything already there. */
    fun equipItem(item: InventoryItemEntity) {
        val equipSlot = item.equipSlot ?: return
        val slotId = gameSessionManager.activeSlotId.value ?: return
        val playerCharacterId = uiState.value.playerCharacterId ?: return
        viewModelScope.launch {
            characterEquipmentDao.upsert(
                CharacterEquipmentEntity(
                    saveSlotId = slotId,
                    characterId = playerCharacterId,
                    equipSlot = equipSlot,
                    itemId = item.itemId
                )
            )
            refreshPortraitForLoadout(playerCharacterId)
        }
    }

    fun unequipItem(equipSlot: String) {
        val playerCharacterId = uiState.value.playerCharacterId ?: return
        viewModelScope.launch {
            characterEquipmentDao.unequip(playerCharacterId, equipSlot)
            refreshPortraitForLoadout(playerCharacterId)
        }
    }

    /**
     * Regenerates the player's portrait to reflect their current loadout — on-demand
     * only, never eager. Cached forever per distinct loadout by [loadoutHash], so a
     * previously-worn combination costs zero further HF calls. Unequipping back to
     * "base" (no gear) is always free: it re-points portraitResName at the original
     * portrait NpcPortraitSyncWorker already generated, no HF call needed.
     */
    private suspend fun refreshPortraitForLoadout(characterId: String) {
        val character = characterDao.getById(characterId) ?: return
        val equipped = characterEquipmentDao.getByCharacter(characterId)
        val hash = loadoutHash(equipped.map { it.itemId })

        val portraitDir = File(appContext.filesDir, "portraits").apply { mkdirs() }
        val file = if (hash == "base") {
            File(portraitDir, "npc_$characterId.jpg")
        } else {
            File(portraitDir, "npc_${characterId}_$hash.jpg")
        }

        if (file.exists()) {
            if (character.portraitResName != file.absolutePath) {
                characterDao.updatePortraitResName(characterId, file.absolutePath)
            }
            return
        }

        val equipmentDescriptor = equipped.firstNotNullOfOrNull { eq ->
            inventoryDao.getByItemId(character.saveSlotId, eq.itemId)?.name
        }

        try {
            val bytes = portraitService.generatePortrait(
                npcName = character.name,
                npcRole = character.role,
                npcTitle = character.title,
                equipmentDescriptor = equipmentDescriptor
            )
            if (bytes != null) {
                file.writeBytes(bytes)
                characterDao.updatePortraitResName(characterId, file.absolutePath)
            }
        } catch (e: Exception) {
            Log.e("InventoryViewModel", "Failed to regenerate portrait for loadout: ${e.message}")
        }
    }
}
