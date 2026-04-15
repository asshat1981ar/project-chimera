package com.chimera.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    HOME("home", Icons.Default.Home, "Home"),
    MAP("map", Icons.Default.Map, "Map"),
    CAMP("camp", Icons.Default.Castle, "Camp"),
    JOURNAL("journal", Icons.Default.MenuBook, "Journal"),
    PARTY("party", Icons.Default.Groups, "Party")
}

object ChimeraRoutes {
    const val SPLASH = "splash"
    const val SAVE_SLOT_SELECT = "save_slot_select"
    const val GAME_GRAPH = "game"
    const val HOME = "home"
    const val MAP = "map"
    const val CAMP = "camp"
    const val JOURNAL = "journal"
    const val PARTY = "party"
    const val SETTINGS = "settings"
    const val DIALOGUE = "dialogue/{sceneId}"
    const val DUEL = "duel/{opponentId}"

    fun dialogue(sceneId: String) = "dialogue/$sceneId"
    fun duel(opponentId: String) = "duel/$opponentId"
}
