package com.chimera.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ChimeraColorScheme = darkColorScheme(
    primary = HollowCrimson,
    onPrimary = ParchmentWhite,
    primaryContainer = HollowCrimsonLight,
    onPrimaryContainer = ParchmentWhite,
    secondary = EmberGold,
    onSecondary = AshBlack,
    secondaryContainer = EmberGoldMuted,
    onSecondaryContainer = ParchmentWhite,
    tertiary = VoidGreen,
    onTertiary = ParchmentWhite,
    tertiaryContainer = VoidGreenBright,
    onTertiaryContainer = ParchmentWhite,
    background = AshBlack,
    onBackground = ParchmentWhite,
    surface = CharcoalSurface,
    onSurface = ParchmentWhite,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = FadedBone,
    outline = FadedBone,
    outlineVariant = DimAsh,
    error = BloodRed,
    onError = ParchmentWhite
)

private val ChimeraShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun ChimeraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ChimeraColorScheme,
        typography = ChimeraTypography,
        shapes = ChimeraShapes,
        content = content
    )
}
