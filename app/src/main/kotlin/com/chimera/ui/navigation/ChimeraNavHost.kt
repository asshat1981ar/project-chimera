package com.chimera.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.chimera.ui.screens.camp.CampScreen
import com.chimera.ui.screens.dialogue.DialogueSceneScreen
import com.chimera.ui.screens.home.HomeScreen
import com.chimera.ui.screens.journal.JournalScreen
import com.chimera.ui.screens.map.MapScreen
import com.chimera.ui.screens.party.PartyScreen
import com.chimera.ui.screens.saveslot.SaveSlotSelectScreen
import com.chimera.ui.screens.settings.SettingsScreen
import com.chimera.ui.screens.splash.SplashScreen

@Composable
fun ChimeraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ChimeraRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(ChimeraRoutes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(ChimeraRoutes.SAVE_SLOT_SELECT) {
                        popUpTo(ChimeraRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(ChimeraRoutes.SAVE_SLOT_SELECT) {
            SaveSlotSelectScreen(
                onSlotSelected = { slotId ->
                    navController.navigate(ChimeraRoutes.HOME) {
                        popUpTo(ChimeraRoutes.SAVE_SLOT_SELECT) { inclusive = true }
                    }
                }
            )
        }

        // Game navigation graph
        navigation(
            startDestination = ChimeraRoutes.HOME,
            route = ChimeraRoutes.GAME_GRAPH
        ) {
            composable(ChimeraRoutes.HOME) {
                HomeScreen(
                    onEnterScene = { sceneId ->
                        navController.navigate(ChimeraRoutes.dialogue(sceneId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(ChimeraRoutes.SETTINGS)
                    }
                )
            }

            composable(ChimeraRoutes.MAP) {
                MapScreen(
                    onEnterScene = { sceneId ->
                        navController.navigate(ChimeraRoutes.dialogue(sceneId))
                    }
                )
            }

            composable(ChimeraRoutes.CAMP) {
                CampScreen()
            }

            composable(ChimeraRoutes.JOURNAL) {
                JournalScreen()
            }

            composable(ChimeraRoutes.PARTY) {
                PartyScreen()
            }

            composable(ChimeraRoutes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = ChimeraRoutes.DIALOGUE,
                arguments = listOf(
                    navArgument("sceneId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sceneId = backStackEntry.arguments?.getString("sceneId") ?: return@composable
                DialogueSceneScreen(
                    sceneId = sceneId,
                    onSceneComplete = { navController.popBackStack() }
                )
            }
        }
    }
}
