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
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Displays active relationship archetype as a badge.
 * Shows stability index as color: Green = stable, Yellow = unstable, Red = critical.
 *
 * @param dynamics The relationship dynamics containing archetype and stability information.
 * @param modifier Optional modifier for styling the badge.
 */
@Composable
fun ArchetypeBadge(
    dynamics: RelationshipDynamics,
    modifier: Modifier = Modifier
) {
    val archetype = dynamics.activeArchetype ?: return

    val cardPadding = ArchetypeBadgeDefaults.CARD_PADDING
    val cardVerticalPadding = ArchetypeBadgeDefaults.CARD_VERTICAL_PADDING

    val badgeColor = when {
        dynamics.stabilityIndex > ArchetypeBadgeDefaults.STABLE_THRESHOLD -> VoidGreen
        dynamics.stabilityIndex > ArchetypeBadgeDefaults.UNSTABLE_THRESHOLD -> EmberGold
        else -> HollowCrimson
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

private object ArchetypeBadgeDefaults {
    const val STABLE_THRESHOLD = 0.7f
    const val UNSTABLE_THRESHOLD = 0.4f
    val CARD_PADDING = 8.dp
    val CARD_VERTICAL_PADDING = 4.dp
}
