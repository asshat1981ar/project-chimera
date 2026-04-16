package com.chimera.feature.camp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun CraftingScreen(
    onBack: () -> Unit,
    viewModel: CraftingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = FadedBone)
            }
            Text("Crafting", style = MaterialTheme.typography.headlineMedium, color = EmberGold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Craft result banner
        uiState.craftResult?.let { result ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (result.startsWith("Crafted")) VoidGreen.copy(alpha = 0.15f)
                    else HollowCrimson.copy(alpha = 0.15f)
                ),
                modifier = Modifier.fillMaxWidth().clickable { viewModel.clearResult() }
            ) {
                Text(result, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Recipe list
        if (uiState.recipes.isEmpty()) {
            Text("No recipes discovered yet.", style = MaterialTheme.typography.bodyMedium, color = DimAsh)
            Text("Explore scenes and talk to NPCs to learn recipes.", style = MaterialTheme.typography.bodySmall, color = DimAsh)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.recipes, key = { it.id }) { recipe ->
                    val isSelected = uiState.selectedRecipe?.id == recipe.id
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.selectRecipe(recipe) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (isSelected) EmberGold.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(recipe.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    recipe.resultRarity.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (recipe.resultRarity) {
                                        "legendary" -> EmberGold
                                        "rare" -> VoidGreen
                                        else -> FadedBone
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(recipe.description, style = MaterialTheme.typography.bodySmall, color = FadedBone)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Creates: ${recipe.resultName}", style = MaterialTheme.typography.labelMedium, color = EmberGold)
                        }
                    }
                }

                // Craft button
                if (uiState.selectedRecipe != null) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.craft() },
                            enabled = uiState.canCraft,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HollowCrimson)
                        ) {
                            Text(if (uiState.canCraft) "Craft" else "Missing Materials")
                        }
                    }
                }
            }
        }
    }
}
