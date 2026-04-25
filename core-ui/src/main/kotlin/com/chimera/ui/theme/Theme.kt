package com.chimera.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun ChimeraTheme(
    content: @Composable () -> Unit
) {
    AtmosphereTheme(
        atmosphere = SceneAtmosphere.DUNGEON,
        content = content
    )
}
