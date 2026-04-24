package com.chimera.feature.party

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Renders a sparkline showing disposition trend over time.
 * Green = positive trend, Red = negative, Gray = stable.
 */
@Composable
fun RelationshipTrendGraph(
    snapshots: List<DispositionSnapshot>,
    modifier: Modifier = Modifier
) {
    if (snapshots.isEmpty()) return

    val trendColor = when {
        snapshots.last().disposition > 0.2f -> VoidGreen
        snapshots.last().disposition > -0.2f -> EmberGold
        else -> HollowCrimson
    }

    Canvas(
        modifier = modifier
            .width(120.dp)
            .height(40.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 4.dp.toPx()

        val graphWidth = width - 2 * padding
        val graphHeight = height - 2 * padding

        if (snapshots.size == 1) {
            // Single point - draw dot
            drawCircle(
                color = trendColor,
                radius = 4.dp.toPx(),
                center = Offset(padding + graphWidth / 2, padding + graphHeight / 2)
            )
        } else {
            // Draw line connecting points
            val points = snapshots.mapIndexed { index, snapshot ->
                val x = padding + (index.toFloat() / (snapshots.size - 1)) * graphWidth
                val y = padding + (0.5f - (snapshot.disposition + 1f) / 4f) * graphHeight
                Offset(x, y)
            }

            for (i in 0 until points.size - 1) {
                drawLine(
                    color = trendColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun RelationshipTrendGraphPreview() {
    val previewData = listOf(
        DispositionSnapshot(disposition = -0.3f),
        DispositionSnapshot(disposition = -0.1f),
        DispositionSnapshot(disposition = 0.2f),
        DispositionSnapshot(disposition = 0.1f),
        DispositionSnapshot(disposition = 0.4f)
    )
    RelationshipTrendGraph(snapshots = previewData)
}
