package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraCorners
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

/**
 * GothicButton: Primary action button with oxblood background,
 * gold-leaf text, and gold border.
 */
@Composable
fun GothicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(ChimeraCorners.small),
        colors = ButtonDefaults.buttonColors(
            containerColor = Oxblood,
            contentColor = AgedGold,
            disabledContainerColor = Iron.copy(alpha = 0.55f),
            disabledContentColor = FadedBone.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            focusedElevation = 3.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content
    )
}

/**
 * GothicOutlinedButton: Secondary action with outlined border,
 * no fill, gold text.
 */
@Composable
fun GothicOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(ChimeraCorners.small),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AgedGold,
            disabledContentColor = FadedBone.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, if (enabled) AgedGold.copy(alpha = 0.5f) else FadedBone.copy(alpha = 0.3f)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content
    )
}