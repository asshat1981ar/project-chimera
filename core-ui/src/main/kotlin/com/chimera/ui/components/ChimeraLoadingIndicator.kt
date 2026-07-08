package com.chimera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.EmberGold

/**
 * Themed loading indicator with an optional label. Use for full-screen or
 * section loading states.
 */
@Composable
fun ChimeraLoadingIndicator(
    modifier: Modifier = Modifier,
    label: String? = null,
    contentDescription: String = "Loading"
) {
    Column(
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = EmberGold,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        if (label != null) {
            Spacer(modifier = Modifier.height(ChimeraSpacing.medium))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
