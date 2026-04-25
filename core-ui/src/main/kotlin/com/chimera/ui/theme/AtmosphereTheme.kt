package com.chimera.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalSceneAtmosphere = staticCompositionLocalOf { SceneAtmosphere.FOREST }

private val AtmosphereShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun AtmosphereTheme(
    atmosphere: SceneAtmosphere,
    content: @Composable () -> Unit
) {
    val palette = AtmosphereTokens.paletteFor(atmosphere)
    val colorScheme = darkColorScheme(
        primary = palette.accent,
        onPrimary = palette.onBackground,
        primaryContainer = palette.elevated,
        onPrimaryContainer = palette.onSurface,
        secondary = palette.success,
        onSecondary = palette.background,
        secondaryContainer = palette.surface,
        onSecondaryContainer = palette.onSurface,
        tertiary = palette.outline,
        onTertiary = palette.onBackground,
        background = palette.background,
        onBackground = palette.onBackground,
        surface = palette.surface,
        onSurface = palette.onSurface,
        surfaceVariant = palette.elevated,
        onSurfaceVariant = palette.onSurface,
        outline = palette.outline,
        outlineVariant = palette.outline.copy(alpha = 0.55f),
        error = palette.danger,
        onError = palette.onBackground
    )

    CompositionLocalProvider(LocalSceneAtmosphere provides atmosphere) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChimeraTypography,
            shapes = AtmosphereShapes,
            content = content
        )
    }
}

@Composable
fun AtmosphereScaffold(
    atmosphere: SceneAtmosphere,
    modifier: Modifier = Modifier,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    AtmosphereTheme(atmosphere = atmosphere) {
        Scaffold(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = bottomBar
        ) { paddingValues ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                color = MaterialTheme.colorScheme.background
            ) {
                content(paddingValues)
            }
        }
    }
}

@Composable
fun AtmosphereSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        content = content
    )
}

@Composable
fun AtmosphereCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(elevation = 3.dp, shape = MaterialTheme.shapes.large, clip = false),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun AtmosphereButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(0.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            focusedElevation = 3.dp,
            hoveredElevation = 3.dp
        ),
        contentPadding = contentPadding
    ) {
        content()
    }
}
