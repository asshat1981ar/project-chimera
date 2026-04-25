package com.chimera.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.chimera.ui.components.GothicBottomNav
import com.chimera.ui.components.GothicNavItem

@Composable
fun ChimeraBottomBar(
    destinations: Array<TopLevelDestination> = TopLevelDestination.values(),
    currentDestination: NavDestination?,
    onNavigate: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = destinations.map { destination ->
        GothicNavItem(
            label = destination.label,
            selectedIcon = {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label
                )
            },
            unselectedIcon = {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label
                )
            }
        )
    }

    val selectedIndex = destinations.indexOfFirst { destination ->
        currentDestination?.hierarchy?.any {
            it.route == destination.route
        } == true
    }

    GothicBottomNav(
        items = navItems,
        selectedIndex = selectedIndex,
        onItemSelected = { index ->
            onNavigate(destinations[index])
        },
        modifier = modifier
    )
}
