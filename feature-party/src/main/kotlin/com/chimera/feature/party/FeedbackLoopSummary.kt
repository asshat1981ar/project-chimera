package com.chimera.feature.party

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chimera.domain.usecase.RelationshipDynamics
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.VoidGreen
import com.chimera.ui.theme.HollowCrimson

/**
 * Displays feedback loop indicators as text with trend icons.
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
        Spacer(modifier = Modifier.height(4.dp))
        dynamics.feedbackLoops.forEach { loop: String ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (icon, color) = when {
                    loop.contains("↑") -> Icons.Filled.TrendingUp to HollowCrimson
                    loop.contains("↓") -> Icons.Filled.TrendingDown to VoidGreen
                    else -> null to DimAsh
                }

                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = color
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
