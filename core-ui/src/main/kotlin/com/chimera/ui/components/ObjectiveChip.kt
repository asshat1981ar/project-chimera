package com.chimera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chimera.model.ActiveObjectiveSummary
import com.chimera.model.ObjectivePrimaryAction
import com.chimera.ui.theme.*

@Composable
fun ObjectiveChip(
    summary: ActiveObjectiveSummary,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor = when (summary.primaryAction) {
        ObjectivePrimaryAction.OPEN_MAP -> VerdigrisBright
        ObjectivePrimaryAction.VIEW_JOURNAL -> AgedGoldBright
        ObjectivePrimaryAction.CONTINUE_SCENE -> OxbloodLight
        ObjectivePrimaryAction.NONE -> DimAsh
    }

    ManuscriptCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPrimaryAction),
        fillColor = IronElevated,
        borderColor = AgedGoldMuted,
        borderWidth = 1.dp,
        contentPadding = 12.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Vellum,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = summary.storyContext,
                    style = MaterialTheme.typography.bodySmall,
                    color = FadedBone,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
