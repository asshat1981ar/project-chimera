package com.chimera.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chimera.data.ChimeraPreferences
import com.chimera.ui.navigation.ChimeraBottomBar
import com.chimera.ui.navigation.ChimeraNavHost
import com.chimera.ui.navigation.ChimeraRoutes
import com.chimera.ui.navigation.TopLevelDestination
import com.chimera.ui.theme.ChimeraTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferences: ChimeraPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChimeraTheme {
                ChimeraApp(preferences)
            }
        }
    }
}

@Composable
fun ChimeraApp(preferences: ChimeraPreferences) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val topLevelRoutes = TopLevelDestination.values().map { it.route }.toSet()
    val showBottomBar = currentDestination?.route in topLevelRoutes

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    ChimeraBottomBar(
                        currentDestination = currentDestination,
                        onNavigate = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            ChimeraNavHost(
                navController = navController,
                preferences = preferences,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
