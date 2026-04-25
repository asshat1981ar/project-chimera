package com.chimera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraCorners
import com.chimera.ui.theme.CinzelDecorative
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood

/**
 * Illuminated initial (drop cap) composable in the style of
 * medieval manuscript decoration. Displays a single large letter
 * with gold color, optional border, and dark background.
 */
@Composable
fun IlluminatedInitial(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    accentColor: Color = AgedGold,
    backgroundColor: Color = Iron,
    borderColor: Color = Oxblood.copy(alpha = 0.6f)
) {
    val initial = extractInitial(text)

    Box(
        modifier = modifier
            .size(size)
            .shadow(2.dp, RoundedCornerShape(ChimeraCorners.small))
            .background(backgroundColor, RoundedCornerShape(ChimeraCorners.small))
            .border(1.dp, borderColor, RoundedCornerShape(ChimeraCorners.small)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontFamily = CinzelDecorative,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.55f).sp,
            color = accentColor
        )
    }
}

internal fun extractInitial(text: String): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return "?"
    val first = trimmed.firstOrNull { it.isLetter() }
    return first?.uppercaseChar()?.toString() ?: "?"
}