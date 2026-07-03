package com.chimera.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Actionable intent for an objective rune.
 *
 * Kept as a UI-layer enum so [core-ui] stays independent of domain models;
 * feature screens map from domain [ObjectivePrimaryAction] to this value.
 */
enum class ObjectiveHudAction {
    NONE,
    OPEN_MAP,
    VIEW_JOURNAL,
    CONTINUE_SCENE
}

/**
 * Small rune/icon that communicates what tapping an objective will do.
 */
@Composable
fun ObjectiveStatusRune(
    action: ObjectiveHudAction,
    modifier: Modifier = Modifier,
    tint: Color = FadedBone
) {
    val imageVector = when (action) {
        ObjectiveHudAction.OPEN_MAP -> Icons.Default.Explore
        ObjectiveHudAction.VIEW_JOURNAL -> Icons.Default.Book
        ObjectiveHudAction.CONTINUE_SCENE -> Icons.Default.AutoStories
        ObjectiveHudAction.NONE -> Icons.Default.HelpOutline
    }

    val actionTint = when (action) {
        ObjectiveHudAction.OPEN_MAP -> VoidGreen
        ObjectiveHudAction.VIEW_JOURNAL -> EmberGold
        ObjectiveHudAction.CONTINUE_SCENE -> HollowCrimson
        ObjectiveHudAction.NONE -> tint
    }

    Icon(
        imageVector = imageVector,
        contentDescription = action.name.lowercase().replace("_", " "),
        modifier = modifier.size(24.dp),
        tint = actionTint
    )
}
