package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.*

/**
 * A single inventory slot — either filled with an item name or empty.
 */
@Composable
fun InventorySlot(
    itemName: String?,
    modifier: Modifier = Modifier,
    slotSize: Dp = InventoryGridDefaults.slotSize,
    filledColor: Color = ParchmentLight,
    emptyColor: Color = InventoryGridDefaults.emptySlotColor,
    borderColor: Color = InventoryGridDefaults.slotBorderColor,
    itemStyle: TextStyle = InventoryGridDefaults.itemStyle
) {
    val isFilled = itemName != null
    Surface(
        modifier = modifier.size(slotSize),
        shape = RoundedCornerShape(ChimeraCorners.small),
        color = if (isFilled) filledColor else emptyColor,
        border = BorderStroke(1.dp, if (isFilled) borderColor else borderColor.copy(alpha = 0.3f))
    ) {
        if (isFilled) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = itemName!!,
                    style = itemStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(ChimeraSpacing.tiny)
                )
            }
        }
    }
}

data class InventoryItem(
    val id: String,
    val name: String,
    val type: String = "misc"
)

/**
 * A grid layout for inventory items using ManuscriptCard-styled slots.
 */
@Composable
fun InventoryGrid(
    items: List<InventoryItem?>,
    modifier: Modifier = Modifier,
    columns: Int = InventoryGridDefaults.columns,
    slotSize: Dp = InventoryGridDefaults.slotSize,
    filledColor: Color = ParchmentLight,
    emptyColor: Color = InventoryGridDefaults.emptySlotColor,
    borderColor: Color = InventoryGridDefaults.slotBorderColor,
    onItemClick: ((InventoryItem) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ChimeraSpacing.tiny),
        verticalArrangement = Arrangement.spacedBy(ChimeraSpacing.tiny)
    ) {
        items(items) { item ->
            InventorySlot(
                itemName = item?.name,
                slotSize = slotSize,
                filledColor = filledColor,
                emptyColor = emptyColor,
                borderColor = borderColor
            )
        }
    }
}

object InventoryGridDefaults {
    val columns = 4
    val slotSize = 72.dp
    val slotBorderColor = Oxblood
    val emptySlotColor = Iron
    val itemStyle = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = Vellum
    )
}