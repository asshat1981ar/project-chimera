package com.chimera.feature.camp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.VoidGreen

@Composable
fun InventoryScreen(
    onBack: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FadedBone)
            }
            Text(
                "Inventory",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberGold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "${uiState.filteredItems.size} items",
                style = MaterialTheme.typography.labelMedium,
                color = FadedBone
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(InventoryCategory.values()) { category ->
                val isSelected = uiState.selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmberGold.copy(alpha = 0.2f),
                        selectedLabelColor = EmberGold
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = FadedBone.copy(alpha = 0.3f),
                        selectedBorderColor = EmberGold.copy(alpha = 0.6f),
                        borderWidth = 1.dp,
                        selectedBorderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Item list
        if (uiState.filteredItems.isEmpty()) {
            Text(
                text = if (uiState.selectedCategory == InventoryCategory.ALL)
                    "Your inventory is empty."
                else
                    "No ${uiState.selectedCategory.label.lowercase()} in inventory.",
                style = MaterialTheme.typography.bodyMedium,
                color = DimAsh,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.filteredItems, key = { it.id }) { item ->
                    InventoryItemCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: com.chimera.database.entity.InventoryItemEntity
) {
    val rarityColor = when (item.rarity) {
        "legendary" -> EmberGold
        "rare"      -> VoidGreen
        "uncommon"  -> FadedBone
        else        -> DimAsh
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "×${item.quantity}",
                        style = MaterialTheme.typography.labelMedium,
                        color = FadedBone
                    )
                    Text(
                        item.rarity.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = rarityColor
                    )
                }
            }
            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = FadedBone
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.category.replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = DimAsh
            )
        }
    }
}
