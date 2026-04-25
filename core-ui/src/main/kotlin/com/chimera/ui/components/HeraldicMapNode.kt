package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.*

enum class MapNodeState {
    DISCOVERED,
    CURRENT,
    UNDISCOVERED,
    LOCKED
}

/**
 * A heraldic map node showing a location on the world map.
 * Discovered = oxblood with gold border, undiscovered = iron,
 * current = AgedGold highlight.
 */
@Composable
fun HeraldicMapNode(
    label: String,
    state: MapNodeState,
    modifier: Modifier = Modifier,
    nodeSize: Dp = HeraldicMapNodeDefaults.nodeSize,
    discoveredColor: Color = HeraldicMapNodeDefaults.discoveredColor,
    undiscoveredColor: Color = HeraldicMapNodeDefaults.undiscoveredColor,
    currentColor: Color = HeraldicMapNodeDefaults.currentColor,
    borderColor: Color = HeraldicMapNodeDefaults.borderColor,
    labelStyle: TextStyle = HeraldicMapNodeDefaults.labelStyle
) {
    val fillColor = when (state) {
        MapNodeState.DISCOVERED -> discoveredColor
        MapNodeState.CURRENT -> currentColor
        MapNodeState.UNDISCOVERED -> undiscoveredColor
        MapNodeState.LOCKED -> undiscoveredColor.copy(alpha = 0.5f)
    }

    val borderWidth = when (state) {
        MapNodeState.CURRENT -> 2.dp
        MapNodeState.DISCOVERED -> 1.5.dp
        else -> 1.dp
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(nodeSize),
            shape = CircleShape,
            color = fillColor,
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (state == MapNodeState.CURRENT || state == MapNodeState.DISCOVERED) {
                    Text(
                        text = label.take(1).uppercase(),
                        style = TextStyle(
                            fontFamily = CinzelDecorative,
                            fontWeight = FontWeight.Bold,
                            fontSize = (nodeSize.value * 0.4f).sp,
                            color = if (state == MapNodeState.CURRENT) Iron else Vellum
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(ChimeraSpacing.micro))
        Text(
            text = label,
            style = labelStyle,
            color = if (state == MapNodeState.LOCKED || state == MapNodeState.UNDISCOVERED) DimAsh else Vellum
        )
    }
}

object HeraldicMapNodeDefaults {
    val nodeSize = 48.dp
    val discoveredColor = Oxblood
    val undiscoveredColor = Iron
    val currentColor = AgedGold
    val borderColor = AgedGold
    val labelStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = Vellum
    )
}