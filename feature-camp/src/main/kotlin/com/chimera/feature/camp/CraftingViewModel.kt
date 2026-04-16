package com.chimera.feature.camp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.database.dao.CraftingRecipeDao
import com.chimera.database.dao.InventoryDao
import com.chimera.database.entity.CraftingRecipeEntity
import com.chimera.database.entity.InventoryItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class Ingredient(val itemId: String, val quantity: Int)

data class CraftingUiState(
    val inventory: List<InventoryItemEntity> = emptyList(),
    val recipes: List<CraftingRecipeEntity> = emptyList(),
    val selectedRecipe: CraftingRecipeEntity? = null,
    val canCraft: Boolean = false,
    val craftResult: String? = null
)

@HiltViewModel
class CraftingViewModel @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val craftingRecipeDao: CraftingRecipeDao,
    private val gameSessionManager: com.chimera.data.GameSessionManager
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private val _selectedRecipe = MutableStateFlow<CraftingRecipeEntity?>(null)
    private val _craftResult = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CraftingUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(CraftingUiState())
            combine(
                inventoryDao.observeAll(slotId),
                craftingRecipeDao.observeDiscovered(),
                _selectedRecipe,
                _craftResult
            ) { items, recipes, selected, result ->
                val canCraft = selected != null && checkIngredients(slotId, selected)
                CraftingUiState(
                    inventory = items,
                    recipes = recipes,
                    selectedRecipe = selected,
                    canCraft = canCraft,
                    craftResult = result
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CraftingUiState())

    fun selectRecipe(recipe: CraftingRecipeEntity) {
        _selectedRecipe.value = recipe
        _craftResult.value = null
    }

    fun craft() {
        val recipe = _selectedRecipe.value ?: return
        val slotId = gameSessionManager.activeSlotId.value ?: return

        viewModelScope.launch {
            val ingredients = parseIngredients(recipe.ingredientsJson)
            // Consume ingredients
            for (ing in ingredients) {
                val removed = inventoryDao.removeQuantity(slotId, ing.itemId, ing.quantity)
                if (removed == 0) {
                    _craftResult.value = "Missing materials!"
                    return@launch
                }
                inventoryDao.cleanupEmpty(slotId, ing.itemId)
            }
            // Grant result item
            val existing = inventoryDao.getByItemId(slotId, recipe.resultItemId)
            if (existing != null) {
                inventoryDao.addQuantity(slotId, recipe.resultItemId, 1)
            } else {
                inventoryDao.upsert(
                    InventoryItemEntity(
                        saveSlotId = slotId,
                        itemId = recipe.resultItemId,
                        name = recipe.resultName,
                        category = recipe.resultCategory,
                        quantity = 1,
                        rarity = recipe.resultRarity,
                    )
                )
            }
            _craftResult.value = "Crafted: ${recipe.resultName}!"
            _selectedRecipe.value = null
        }
    }

    fun clearResult() {
        _craftResult.value = null
    }

    private suspend fun checkIngredients(slotId: Long, recipe: CraftingRecipeEntity): Boolean {
        val ingredients = parseIngredients(recipe.ingredientsJson)
        return ingredients.all { ing ->
            val item = inventoryDao.getByItemId(slotId, ing.itemId)
            item != null && item.quantity >= ing.quantity
        }
    }

    private fun parseIngredients(jsonStr: String): List<Ingredient> {
        return try {
            json.decodeFromString<List<Ingredient>>(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
