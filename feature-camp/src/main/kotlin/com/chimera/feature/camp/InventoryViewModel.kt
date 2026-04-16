package com.chimera.feature.camp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chimera.data.GameSessionManager
import com.chimera.database.dao.InventoryDao
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
import javax.inject.Inject

enum class InventoryCategory(val label: String, val key: String?) {
    ALL("All", null),
    MATERIAL("Materials", "material"),
    ARTIFACT("Artifacts", "artifact"),
    CONSUMABLE("Consumables", "consumable"),
    KEY_ITEM("Key Items", "key_item")
}

data class InventoryUiState(
    val items: List<InventoryItemEntity> = emptyList(),
    val selectedCategory: InventoryCategory = InventoryCategory.ALL,
    val isLoading: Boolean = true
) {
    val filteredItems: List<InventoryItemEntity>
        get() = if (selectedCategory.key == null) items
                else items.filter { it.category == selectedCategory.key }
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryDao: InventoryDao,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(InventoryCategory.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InventoryUiState> = gameSessionManager.activeSlotId
        .flatMapLatest { slotId ->
            if (slotId == null) return@flatMapLatest flowOf(InventoryUiState(isLoading = false))
            combine(
                inventoryDao.observeAll(slotId),
                _selectedCategory
            ) { items, category ->
                InventoryUiState(
                    items = items,
                    selectedCategory = category,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState())

    fun selectCategory(category: InventoryCategory) {
        _selectedCategory.value = category
    }
}
