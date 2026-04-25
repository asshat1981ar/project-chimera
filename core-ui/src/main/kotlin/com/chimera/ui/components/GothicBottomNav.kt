package com.chimera.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.*

/**
 * A Gothic-themed bottom navigation bar with oxblood/iron styling.
 * Use as the primary app navigation bar.
 */
@Composable
fun GothicBottomNav(
    items: List<GothicNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = GothicBottomNavDefaults.containerColor,
    selectedColor: Color = GothicBottomNavDefaults.selectedColor,
    unselectedColor: Color = GothicBottomNavDefaults.unselectedColor,
    indicatorColor: Color = GothicBottomNavDefaults.indicatorColor
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        tonalElevation = ChimeraElevation.medium
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    if (index == selectedIndex) {
                        BadgedBox(badge = { /* no badge content */ }) {
                            item.selectedIcon()
                        }
                    } else {
                        item.unselectedIcon()
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFamily = Cinzel,
                            fontSize = 10.sp
                        )
                    )
                },
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = indicatorColor
                )
            )
        }
    }
}

data class GothicNavItem(
    val label: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

object GothicBottomNavDefaults {
    val containerColor = Iron
    val selectedColor = AgedGold
    val unselectedColor = FadedBone
    val indicatorColor = Oxblood
}