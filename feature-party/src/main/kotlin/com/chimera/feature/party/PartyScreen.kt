package com.chimera.feature.party

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun PartyScreen(
    viewModel: PartyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Party",
            style = MaterialTheme.typography.headlineMedium,
            color = EmberGold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )

        if (uiState.members.isEmpty() && !uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No companions yet.", style = MaterialTheme.typography.bodyLarge, color = DimAsh)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Seek allies in your journeys through the Hollow.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DimAsh,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Companion detail panel (if selected)
            uiState.selectedMember?.let { member ->
                CompanionDetail(
                    member = member,
                    onClose = { viewModel.clearSelection() }
                )
            }

            // Companion list
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.members, key = { it.character.id }) { member ->
                    CompanionCard(
                        member = member,
                        isSelected = uiState.selectedMember?.character?.id == member.character.id,
                        onClick = { viewModel.selectMember(member.character.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun CompanionCard(
    member: PartyMember,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val disposition = member.state?.dispositionToPlayer ?: 0f
    val moodColor = when {
        disposition > 0.2f -> VoidGreen
        disposition > -0.2f -> FadedBone
        else -> HollowCrimson
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (isSelected) EmberGold.copy(alpha = 0.6f) else moodColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(52.dp)
            ) {
                Text(
                    text = member.character.name.first().toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 10.dp),
                    textAlign = TextAlign.Center,
                    color = EmberGold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.character.name, style = MaterialTheme.typography.titleSmall)
                member.character.title?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = FadedBone)
                }
                Text(
                    member.character.role.replaceFirstChar { it.uppercase() }.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = moodColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = ((disposition + 1f) / 2f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = moodColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompanionDetail(
    member: PartyMember,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(member.character.name, style = MaterialTheme.typography.headlineSmall, color = EmberGold)
                    member.character.title?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = FadedBone)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "Close", tint = FadedBone)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val disposition = member.state?.dispositionToPlayer ?: 0f
            val healthFraction = member.state?.healthFraction ?: 1f

            // Stats
            StatBar("Disposition", disposition, -1f..1f,
                color = when { disposition > 0.2f -> VoidGreen; disposition > -0.2f -> FadedBone; else -> HollowCrimson })
            StatBar("Health", healthFraction, 0f..1f, color = VoidGreen)

            member.state?.activeArchetype?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Active Archetype: $it", style = MaterialTheme.typography.bodySmall, color = FadedBone)
            }

            if (member.recentMemories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Recent Memories", style = MaterialTheme.typography.titleSmall, color = EmberGold)
                Spacer(modifier = Modifier.height(6.dp))
                member.recentMemories.take(5).forEach { memory ->
                    Text(
                        "- ${memory.summary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = FadedBone,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBar(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    color: androidx.compose.ui.graphics.Color
) {
    val normalized = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
        LinearProgressIndicator(
            progress = normalized,
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface
        )
        Text(
            "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = FadedBone,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}
