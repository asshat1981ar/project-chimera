package com.chimera.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson

/**
 * Gothic-themed alert dialog with parchment surface and styled actions.
 */
@Composable
fun ChimeraAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    confirmIsDestructive: Boolean = false,
    confirmEnabled: Boolean = true
) {
    ChimeraAlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        modifier = modifier,
        confirmText = confirmText,
        onConfirm = onConfirm,
        dismissText = dismissText,
        onDismiss = onDismiss,
        confirmIsDestructive = confirmIsDestructive,
        confirmEnabled = confirmEnabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Gothic-themed alert dialog with custom content and styled actions.
 */
@Composable
fun ChimeraAlertDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    confirmIsDestructive: Boolean = false,
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = content,
        confirmButton = {
            GothicButton(
                onClick = onConfirm,
                enabled = confirmEnabled
            ) {
                Text(
                    text = confirmText,
                    color = if (confirmIsDestructive) HollowCrimson else MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = if (dismissText != null && onDismiss != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(dismissText, color = FadedBone)
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
