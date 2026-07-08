package com.chimera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone

/**
 * Centered empty-state placeholder with an icon, title, and optional body.
 */
@Composable
fun ChimeraEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    body: String? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(ChimeraSpacing.xxl),
                tint = FadedBone
            )
            Spacer(modifier = Modifier.height(ChimeraSpacing.regular))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (body != null) {
            Spacer(modifier = Modifier.height(ChimeraSpacing.small))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = FadedBone,
                textAlign = TextAlign.Center
            )
        }
    }
}
