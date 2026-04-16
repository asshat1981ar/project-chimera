package com.chimera.feature.camp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.database.entity.InventoryItemEntity
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Inventory browsing screen, accessible from [CampScreen] via a nav action.
 *
 * Shows all items grouped by category (All / Artifacts / Consumables /
 * Key Items / Materials). Each item card shows name, description, quantity
 * and a rarity badge colour-coded to the game's vocabulary:
 *   legendary → [EmberGold]
 *   rare       → [VoidGreen]
 *   uncommon   → [FadedBone]
 *   common     → [DimAsh]
 */
@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = FadedBone)
            }
            Text(
                "Inventory",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberGold,
                modifier = Modifier.weight(1f)
            )
            if (!uiState.isLoading) {
                Text(
                    "${uiState.totalCount} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = FadedBone
                )
            }
        }

        // ── Category tabs ────────────────────────────────────────────────────
        ScrollableTabRow(
            selectedTabIndex = InventoryCategory.values().indexOf(uiState.selectedCategory),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmberGold,
            edgePadding = 16.dp
        ) {
            InventoryCategory.values().forEach { category ->
                Tab(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    text = {
                        Text(
                            category.label,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selectedContentColor = EmberGold,
                    unselectedContentColor = FadedBone
                )
            }
        }

        // ── Content ──────────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmberGold)
                }
            }
            uiState.items.isEmpty() -> {
                EmptyInventory(category = uiState.selectedCategory)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        InventoryItemCard(item = item)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Item card ────────────────────────────────────────────────────────────────

@Composable
private fun InventoryItemCard(item: InventoryItemEntity) {
    val rarityColor = when (item.rarity) {
        "legendary" -> EmberGold
        "rare"      -> VoidGreen
        "uncommon"  -> FadedBone
        else        -> DimAsh
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Rarity badge
                Text(
                    item.rarity.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = rarityColor
                )
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = FadedBone
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = DimAsh
                )
                if (item.quantity > 1) {
                    Text(
                        "×${item.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmberGold
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyInventory(category: InventoryCategory) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (category) {
                InventoryCategory.ALL       -> "Your pack is empty."
                InventoryCategory.ARTIFACT  -> "No artifacts yet."
                InventoryCategory.CONSUMABLE -> "No consumables."
                InventoryCategory.KEY_ITEM  -> "No key items."
                InventoryCategory.MATERIAL  -> "No materials."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = DimAsh,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Explore scenes and forge relationships to acquire items.",
            style = MaterialTheme.typography.bodyMedium,
            color = DimAsh,
            textAlign = TextAlign.Center
        )
    }
}
