package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.*

/**
 * A parchment-styled dialogue bubble with optional illuminated initial,
 * used for NPC dialogue and narrative text.
 */
@Composable
fun IlluminatedDialogueBubble(
    text: String,
    modifier: Modifier = Modifier,
    speakerName: String = "",
    fillColor: Color = ParchmentLight,
    borderColor: Color = Oxblood,
    borderWidth: Dp = IlluminatedDialogueBubbleDefaults.borderWidth,
    cornerRadius: Dp = IlluminatedDialogueBubbleDefaults.cornerRadius,
    contentPadding: Dp = IlluminatedDialogueBubbleDefaults.contentPadding,
    speakerStyle: TextStyle = IlluminatedDialogueBubbleDefaults.speakerStyle,
    bodyStyle: TextStyle = IlluminatedDialogueBubbleDefaults.bodyStyle,
    showIlluminatedInitial: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = fillColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = ChimeraElevation.low,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            if (speakerName.isNotEmpty()) {
                Text(
                    text = speakerName,
                    style = speakerStyle
                )
                Spacer(modifier = Modifier.height(ChimeraSpacing.micro))
            }
            if (showIlluminatedInitial && text.isNotEmpty()) {
                Row {
                    IlluminatedInitial(text = text)
                    Spacer(modifier = Modifier.width(ChimeraSpacing.small))
                    Text(
                        text = text.drop(1),
                        style = bodyStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = text,
                    style = bodyStyle
                )
            }
        }
    }
}

object IlluminatedDialogueBubbleDefaults {
    val borderWidth = 1.5.dp
    val cornerRadius = ChimeraCorners.medium
    val contentPadding = ChimeraSpacing.regular
    val speakerStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = AgedGold
    )
    val bodyStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Vellum
    )
}