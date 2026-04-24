package com.chimera.feature.camp

import androidx.compose.foundation.BorderStroke
import com.chimera.data.DutyType
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun CampScreen(
    onNavigateToInventory: () -> Unit = {},
    onNavigateToCrafting: () -> Unit = {},
    viewModel: CampViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Camp",
                        style = MaterialTheme.typography.headlineMedium,
                        color = EmberGold
                    )
                    Text(
                        "Day ${uiState.day}",
                        style = MaterialTheme.typography.titleMedium,
                        color = FadedBone
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Inventory + Crafting navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToInventory,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_inventory"),
                        border = BorderStroke(1.dp, FadedBone.copy(alpha = 0.3f))
                    ) {
                        Text("Inventory", color = FadedBone)
                    }
                    OutlinedButton(
                        onClick = onNavigateToCrafting,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_crafting"),
                        border = BorderStroke(1.dp, EmberGold.copy(alpha = 0.3f))
                    ) {
                        Text("Crafting", color = EmberGold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Morale bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Camp Morale: ${(uiState.morale * 100).toInt()}%"
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Camp Morale", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${(uiState.morale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = moraleColor(uiState.morale)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = uiState.morale,
                            modifier = Modifier.fillMaxWidth(),
                            color = moraleColor(uiState.morale),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Companions section
        item {
            Text(
                "Companions",
                style = MaterialTheme.typography.titleMedium,
                color = EmberGold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (uiState.companions.isEmpty()) {
            item {
                Text(
                    "No companions have joined your camp yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DimAsh,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(uiState.companions, key = { it.character.id }) { companion ->
                CompanionCard(data = companion)
            }
        }

        // Duty assignments section
        if (uiState.dutyAssignments.isNotEmpty()) {
            item {
                Text(
                    "Duty Assignments",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberGold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(uiState.dutyAssignments, key = { it.companionId }) { assignment ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(assignment.companionName, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DutyType.values().forEach { duty ->
                                val isSelected = assignment.duty == duty
                                OutlinedButton(
                                    onClick = {
                                        if (isSelected) viewModel.clearDuty(assignment.companionId)
                                        else viewModel.assignDuty(assignment.companionId, duty)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) EmberGold else FadedBone.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_duty_${duty.name.lowercase()}")
                                ) {
                                    Text(
                                        duty.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) EmberGold else FadedBone
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active vows section
        if (uiState.activeVows.isNotEmpty()) {
            item {
                Text(
                    "Unresolved Vows",
                    style = MaterialTheme.typography.titleMedium,
                    color = HollowCrimson,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(uiState.activeVows, key = { it.id }) { vow ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, HollowCrimson.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = vow.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Night event trigger
        item {
            if (uiState.nightEvent == null) {
                Button(
                    onClick = { viewModel.triggerNightEvent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_rest_night"),
                    colors = ButtonDefaults.buttonColors(containerColor = HollowCrimson)
                ) {
                    Text("Rest for the Night")
                }
            }
        }

        // Night event display
        uiState.nightEvent?.let { event ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, EmberGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(event.title, style = MaterialTheme.typography.titleMedium, color = EmberGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(event.narrative, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.nightEventOutcome != null) {
                            Text(
                                uiState.nightEventOutcome!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VoidGreen
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { viewModel.dismissNightEvent() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_dawn_breaks")
                            ) {
                                Text("Dawn Breaks")
                            }
                        } else {
                            event.choices.forEach { choice ->
                                OutlinedButton(
                                    onClick = { viewModel.resolveNightEvent(choice) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .testTag("btn_night_event_choice"),
                                    border = BorderStroke(1.dp, FadedBone.copy(alpha = 0.3f))
                                ) {
                                    Text(choice.text)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom spacer for nav bar
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CompanionCard(data: CompanionCardData) {
    val disposition = data.state?.dispositionToPlayer ?: 0f
    val moodLabel = when {
        disposition > 0.5f -> "Loyal"
        disposition > 0.2f -> "Friendly"
        disposition > -0.2f -> "Neutral"
        disposition > -0.5f -> "Wary"
        else -> "Hostile"
    }
    val moodColor = when {
        disposition > 0.2f -> VoidGreen
        disposition > -0.2f -> FadedBone
        else -> HollowCrimson
    }

    Card(
        modifier = Modifier.testTag("card_companion_${data.character.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, moodColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            com.chimera.ui.components.NpcPortrait(
                npcId = data.character.id,
                npcName = data.character.name,
                disposition = data.state?.dispositionToPlayer ?: 0f,
                archetype = data.state?.activeArchetype,
                portraitResName = data.character.portraitResName,
                size = 48.dp,
                contentDescription = "${data.character.name} portrait"
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(data.character.name, style = MaterialTheme.typography.titleSmall)
                if (data.character.title != null) {
                    Text(
                        data.character.title!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = FadedBone
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(moodLabel, style = MaterialTheme.typography.labelMedium, color = moodColor)
                    Text(
                        "${((disposition + 1f) / 2f * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = FadedBone
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = ((disposition + 1f) / 2f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Disposition: ${((disposition + 1f) / 2f * 100).toInt()}%"
                        },
                    color = moodColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

private fun moraleColor(morale: Float) = when {
    morale > 0.6f -> VoidGreen
    morale > 0.3f -> EmberGold
    else -> HollowCrimson
}
