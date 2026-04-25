package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.*

/**
 * A parchment-styled card with oxblood border and optional illuminated initial.
 * Use for narrative content, inventory items, character info, etc.
 */
@Composable
fun ManuscriptCard(
    modifier: Modifier = Modifier,
    illuminatedText: String = "",
    fillColor: Color = ParchmentLight,
    borderColor: Color = Oxblood,
    borderWidth: Dp = ManuscriptCardDefaults.borderWidth,
    cornerRadius: Dp = ManuscriptCardDefaults.cornerRadius,
    elevation: Dp = ManuscriptCardDefaults.elevation,
    contentPadding: Dp = ManuscriptCardDefaults.contentPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = fillColor,
            contentColor = Vellum
        ),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            if (illuminatedText.isNotEmpty()) {
                IlluminatedInitial(text = illuminatedText)
                Spacer(modifier = Modifier.height(ChimeraSpacing.small))
            }
            content()
        }
    }
}

object ManuscriptCardDefaults {
    val borderWidth = 2.dp
    val cornerRadius = ChimeraCorners.medium
    val elevation = ChimeraElevation.low
    val contentPadding = ChimeraSpacing.regular
}