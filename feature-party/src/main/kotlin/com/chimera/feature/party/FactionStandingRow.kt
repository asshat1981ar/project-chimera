package com.chimera.feature.party

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chimera.database.entity.FactionStateEntity
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Renders a single faction standing row showing:
 * - Faction name + world influence bar (their power in the world)
 * - Player standing bar (the player's reputation with this faction)
 * - Standing label ("Hostile" → "Revered")
 */
@Composable
fun FactionStandingRow(
    faction: FactionStateEntity,
    modifier: Modifier = Modifier
) {
    val standing = faction.playerStanding.coerceIn(-1f, 1f)
    val standingNorm = (standing + 1f) / 2f // 0..1 for progress bar

    val standingColor = when {
        standing > 0.5f  -> VoidGreen
        standing > 0f    -> EmberGold
        standing > -0.5f -> FadedBone
        else             -> HollowCrimson
    }

    val standingLabel = when {
        standing > 0.7f  -> "Revered"
        standing > 0.4f  -> "Honoured"
        standing > 0.1f  -> "Friendly"
        standing > -0.1f -> "Neutral"
        standing > -0.4f -> "Wary"
        standing > -0.7f -> "Hostile"
        else             -> "Enemy"
    }

    Card(
        modifier = modifier.testTag("card_faction_${faction.factionId}").fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, standingColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    faction.factionName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    standingLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = standingColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // World influence
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Influence: ${(faction.influence * 100).toInt()}%"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Influence",
                    style = MaterialTheme.typography.labelSmall,
                    color = DimAsh,
                    modifier = Modifier.width(64.dp)
                )
                LinearProgressIndicator(
                    progress = faction.influence.coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f).height(4.dp),
                    color = FadedBone.copy(alpha = 0.5f),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    "${(faction.influence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = DimAsh,
                    modifier = Modifier.width(36.dp).padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Player standing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Standing: ${(standing * 100).toInt()}%"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Standing",
                    style = MaterialTheme.typography.labelSmall,
                    color = DimAsh,
                    modifier = Modifier.width(64.dp)
                )
                LinearProgressIndicator(
                    progress = standingNorm,
                    modifier = Modifier.weight(1f).height(4.dp),
                    color = standingColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    "${(standing * 100).toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = standingColor,
                    modifier = Modifier.width(36.dp).padding(start = 6.dp)
                )
            }
        }
    }
}
