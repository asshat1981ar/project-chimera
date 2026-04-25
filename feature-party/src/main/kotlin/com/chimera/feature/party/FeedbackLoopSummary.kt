package com.chimera.feature.party

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chimera.domain.usecase.RelationshipDynamics
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Displays feedback loop indicators as text with trend icons.
 *
 * @param dynamics The relationship dynamics containing feedback loop information.
 * @param modifier Optional modifier for styling the summary.
 */
@Composable
fun FeedbackLoopSummary(
    dynamics: RelationshipDynamics,
    modifier: Modifier = Modifier
) {
    if (dynamics.feedbackLoops.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            "Relationship Dynamics",
            style = MaterialTheme.typography.labelMedium,
            color = DimAsh
        )
        Spacer(modifier = Modifier.height(FeedbackLoopSummaryDefaults.SPACER_HEIGHT))
        dynamics.feedbackLoops.forEach { loop: String ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TrendingUp icon (red) for "↑" items, TrendingDown icon (green) for "↓" items
                val (icon, color) = when {
                    loop.contains("↑") -> Icons.Filled.TrendingUp to VoidGreen
                    loop.contains("↓") -> Icons.Filled.TrendingDown to HollowCrimson
                    else -> null to DimAsh
                }

                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(FeedbackLoopSummaryDefaults.ICON_SIZE),
                        tint = color
                    )
                    Spacer(modifier = Modifier.height(FeedbackLoopSummaryDefaults.ICON_SPACER_HEIGHT))
                }

                Text(
                    text = loop.replace("↑", "").replace("↓", "").trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = DimAsh
                )
            }
        }
    }
}

private object FeedbackLoopSummaryDefaults {
    val SPACER_HEIGHT = 4.dp
    val ICON_SIZE = 12.dp
    val ICON_SPACER_HEIGHT = 2.dp
}

@Preview(showBackground = true)
@Composable
private fun FeedbackLoopSummaryPreview() {
    FeedbackLoopSummary(
        dynamics = RelationshipDynamics(
            activeArchetype = "Escalation",
            stabilityIndex = 0.35f,
            feedbackLoops = listOf("Tension ↑", "Relationship damage ↑", "Retaliation cycle"),
            archetypeDescription = "A cycle of retaliation"
        )
    )
}
