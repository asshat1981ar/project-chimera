package com.chimera.feature.party

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chimera.domain.usecase.RelationshipDynamics
import com.chimera.ui.theme.EmberGold

/**
 * Displays active relationship archetype as a badge.
 * Shows stability index as color: Green = stable, Yellow = unstable, Red = critical.
 */
@Composable
fun ArchetypeBadge(
    dynamics: RelationshipDynamics,
    modifier: Modifier = Modifier
) {
    val archetype = dynamics.activeArchetype ?: return

    val stableThreshold = 0.7f
    val unstableThreshold = 0.4f
    val cardPadding = 8.dp
    val cardVerticalPadding = 4.dp

    val badgeColor = when {
        dynamics.stabilityIndex > stableThreshold -> com.chimera.ui.theme.VoidGreen
        dynamics.stabilityIndex > unstableThreshold -> EmberGold
        else -> com.chimera.ui.theme.HollowCrimson
    }

    Card(
        modifier = modifier.padding(vertical = cardVerticalPadding),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
    ) {
        Text(
            text = archetype,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = cardPadding, vertical = cardVerticalPadding)
        )
    }
}
