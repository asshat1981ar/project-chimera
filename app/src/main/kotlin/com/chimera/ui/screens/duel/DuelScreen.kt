package com.chimera.ui.screens.duel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun DuelScreen(
    onDuelComplete: () -> Unit,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDuelComplete) {
                Icon(Icons.Default.ArrowBack, "Leave duel", tint = FadedBone)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Ritual Duel",
                style = MaterialTheme.typography.headlineMedium,
                color = EmberGold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resource bars
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Player omens
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your Omens", style = MaterialTheme.typography.titleSmall)
                    Text("${uiState.playerOmens}/4", style = MaterialTheme.typography.labelMedium, color = EmberGold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = uiState.playerOmens / 4f,
                    modifier = Modifier.fillMaxWidth(),
                    color = EmberGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Opponent resolve
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${uiState.opponentName}'s Resolve", style = MaterialTheme.typography.titleSmall)
                    Text("${uiState.opponentResolve}/3", style = MaterialTheme.typography.labelMedium, color = HollowCrimson)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = uiState.opponentResolve / 3f,
                    modifier = Modifier.fillMaxWidth(),
                    color = HollowCrimson,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Last result narrative
        uiState.lastResult?.let { result ->
            AnimatedVisibility(visible = true, enter = slideInVertically() + fadeIn()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (result.outcome) {
                            DuelEngine.RoundOutcome.WIN -> VoidGreen.copy(alpha = 0.1f)
                            DuelEngine.RoundOutcome.LOSE -> HollowCrimson.copy(alpha = 0.1f)
                            DuelEngine.RoundOutcome.DRAW -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    border = BorderStroke(
                        1.dp, when (result.outcome) {
                            DuelEngine.RoundOutcome.WIN -> VoidGreen.copy(alpha = 0.4f)
                            DuelEngine.RoundOutcome.LOSE -> HollowCrimson.copy(alpha = 0.4f)
                            DuelEngine.RoundOutcome.DRAW -> FadedBone.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Round ${result.round}",
                            style = MaterialTheme.typography.labelMedium,
                            color = FadedBone
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            result.narrative,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${result.playerStance.label} vs ${result.opponentStance.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FadedBone
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stance selection or result
        if (uiState.isComplete) {
            // Duel outcome
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    2.dp,
                    if (uiState.playerWon == true) EmberGold else HollowCrimson
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (uiState.playerWon == true) "Victory" else "Defeat",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (uiState.playerWon == true) EmberGold else HollowCrimson
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (uiState.playerWon == true) {
                            "The ritual acknowledges your strength. ${uiState.opponentName} yields."
                        } else {
                            "The ritual has spoken. ${uiState.opponentName} prevails."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = FadedBone
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDuelComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = HollowCrimson)
                    ) {
                        Text("Continue")
                    }
                }
            }
        } else {
            // Stance buttons
            Text(
                "Choose your stance:",
                style = MaterialTheme.typography.titleMedium,
                color = EmberGold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DuelEngine.Stance.values().forEach { stance ->
                    OutlinedButton(
                        onClick = { viewModel.selectStance(stance) },
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, EmberGold.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stance.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Beats ${stance.beats}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FadedBone
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Combat log
        if (uiState.log.isNotEmpty()) {
            Text("Combat Log", style = MaterialTheme.typography.titleSmall, color = FadedBone)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.log.reversed()) { entry ->
                    Text(
                        "R${entry.round}: ${entry.playerStance.label} vs ${entry.opponentStance.label} -- ${entry.outcome.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (entry.outcome) {
                            DuelEngine.RoundOutcome.WIN -> VoidGreen
                            DuelEngine.RoundOutcome.LOSE -> HollowCrimson
                            DuelEngine.RoundOutcome.DRAW -> FadedBone
                        }
                    )
                }
            }
        }
    }
}
