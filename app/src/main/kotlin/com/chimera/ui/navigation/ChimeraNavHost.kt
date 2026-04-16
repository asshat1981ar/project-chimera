package com.chimera.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.chimera.feature.camp.CampScreen
import com.chimera.feature.camp.InventoryScreen
import com.chimera.feature.dialogue.DialogueSceneScreen
import com.chimera.ui.screens.duel.DuelScreen
import com.chimera.feature.home.HomeScreen
import com.chimera.feature.journal.JournalScreen
import com.chimera.feature.map.MapScreen
import com.chimera.feature.party.PartyScreen
import androidx.compose.runtime.collectAsState
import com.chimera.data.ChimeraPreferences
import com.chimera.ui.screens.onboarding.OnboardingScreen
import com.chimera.ui.screens.saveslot.SaveSlotSelectScreen
import com.chimera.feature.settings.SettingsScreen
import com.chimera.ui.screens.splash.SplashScreen

@Composable
fun ChimeraNavHost(
    navController: NavHostController,
    preferences: ChimeraPreferences,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ChimeraRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(ChimeraRoutes.SPLASH) {
            val settings by preferences.settings.collectAsState(
                initial = com.chimera.data.AppSettings()
            )
            SplashScreen(
                onFinished = {
                    val dest = if (settings.tutorialComplete) {
                        ChimeraRoutes.SAVE_SLOT_SELECT
                    } else {
                        ChimeraRoutes.ONBOARDING
                    }
                    navController.navigate(dest) {
                        popUpTo(ChimeraRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(ChimeraRoutes.ONBOARDING) {
            OnboardingScreen(
                preferences = preferences,
                onComplete = {
                    navController.navigate(ChimeraRoutes.SAVE_SLOT_SELECT) {
                        popUpTo(ChimeraRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(ChimeraRoutes.SAVE_SLOT_SELECT) {
            SaveSlotSelectScreen(
                onSlotSelected = { slotId ->
                    navController.navigate(ChimeraRoutes.GAME_GRAPH) {
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
                CampScreen(
                    onNavigateToInventory = {
                        navController.navigate(ChimeraRoutes.INVENTORY)
                    }
                )
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
                    onSceneComplete = { navController.popBackStack() },
                    onTriggerDuel = { opponentId ->
                        navController.navigate(ChimeraRoutes.duel(opponentId))
                    }
                )
            }

            composable(
                route = ChimeraRoutes.DUEL,
                arguments = listOf(
                    navArgument("opponentId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                DuelScreen(
                    onDuelComplete = { navController.popBackStack() }
                )
            }

            composable(ChimeraRoutes.INVENTORY) {
                InventoryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
