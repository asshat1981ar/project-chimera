package com.chimera.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Animated banner showing relationship disposition change after dialogue.
 */
@Composable
fun BondShiftBanner(
    npcName: String,
    delta: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = if (delta > 0) VoidGreen.copy(alpha = 0.2f) else HollowCrimson.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "$npcName: ${if (delta > 0) "+" else ""}${String.format("%.0f", delta * 100)}%",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = if (delta > 0) VoidGreen else HollowCrimson,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * NPC portrait header with avatar circle, name, title, and mood.
 */
@Composable
fun PortraitHeader(
    name: String,
    title: String? = null,
    mood: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(48.dp)
        ) {
            Text(
                text = name.first().toString(),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                color = EmberGold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.titleSmall)
            if (title != null) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = FadedBone)
            }
            if (mood != null) {
                Text(mood, style = MaterialTheme.typography.labelSmall, color = DimAsh)
            }
        }
    }
}

/**
 * Empty state card with themed message for screens with no content yet.
 */
@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = FadedBone,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = DimAsh,
                textAlign = TextAlign.Center
            )
        }
    }
}
