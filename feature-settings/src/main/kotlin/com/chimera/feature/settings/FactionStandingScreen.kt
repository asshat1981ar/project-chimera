package com.chimera.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.chimera.feature.party.FactionStandingRow
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactionStandingScreen(
    onBack: () -> Unit = {},
    viewModel: FactionStandingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = FadedBone)
            }
            Text(
                "Faction Standing",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Description
        Text(
            "Your reputation with each faction affects trade, dialogue options, and quest availability.",
            style = MaterialTheme.typography.bodyMedium,
            color = DimAsh
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Standing reference card
        StandingReferenceCard()

        Spacer(modifier = Modifier.height(16.dp))

        // Faction list
        if (uiState.isLoading) {
            Text(
                "Loading factions...",
                style = MaterialTheme.typography.bodyLarge,
                color = FadedBone,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (uiState.factions.isEmpty()) {
            Text(
                "No faction data available.",
                style = MaterialTheme.typography.bodyLarge,
                color = FadedBone,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            uiState.factions.forEach { faction ->
                FactionStandingRow(
                    faction = faction,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StandingReferenceCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = EmberGold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "Standing Tiers",
                    style = MaterialTheme.typography.titleSmall,
                    color = EmberGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StandingTierRow(label = "Revered", range = "> +0.7", color = EmberGold)
            StandingTierRow(label = "Honoured", range = "+0.4 to +0.7", color = EmberGold)
            StandingTierRow(label = "Friendly", range = "+0.1 to +0.4", color = EmberGold)
            StandingTierRow(label = "Neutral", range = "-0.1 to +0.1", color = FadedBone)
            StandingTierRow(label = "Wary", range = "-0.4 to -0.1", color = FadedBone)
            StandingTierRow(label = "Hostile", range = "-0.7 to -0.4", color = MaterialTheme.colorScheme.error)
            StandingTierRow(label = "Enemy", range = "< -0.7", color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun StandingTierRow(label: String, range: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        Text(range, style = MaterialTheme.typography.labelSmall, color = DimAsh)
    }
}
