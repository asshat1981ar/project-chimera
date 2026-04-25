package com.chimera.ui.theme

import androidx.compose.ui.graphics.Color

enum class SceneAtmosphere {
    FOREST,
    CAVE,
    DUNGEON,
    CAMP,
    WORLD_MAP,
    DIALOGUE;

    companion object
}

data class AtmospherePalette(
    val background: Color,
    val surface: Color,
    val elevated: Color,
    val accent: Color,
    val onBackground: Color,
    val onSurface: Color,
    val outline: Color,
    val danger: Color,
    val success: Color,
    val overlayVignette: Color,
    val grainIntensity: Float,
    val vignetteAlpha: Float = 0.4f,
    val vignetteStyle: String = ""
)

object AtmosphereTokens {
    val Forest = AtmospherePalette(
        background = Color(0xFF0B1711),
        surface = Color(0xFF13231A),
        elevated = Color(0xFF1A3024),
        accent = Color(0xFF7FA46A),
        onBackground = Color(0xFFE4ECD8),
        onSurface = Color(0xFFD6E1C7),
        outline = Color(0xFF4E634B),
        danger = Color(0xFFB94A42),
        success = Color(0xFF78A85F),
        overlayVignette = Color(0xFF1A3A1A),
        grainIntensity = 0.10f,
        vignetteAlpha = 0.30f,
        vignetteStyle = "Verdant Manuscript"
    )

    val Cave = AtmospherePalette(
        background = Color(0xFF0B1014),
        surface = Color(0xFF141B21),
        elevated = Color(0xFF1F2930),
        accent = Color(0xFF7A98A5),
        onBackground = Color(0xFFDCE7EA),
        onSurface = Color(0xFFC9D7DC),
        outline = Color(0xFF4D6068),
        danger = Color(0xFFC2574B),
        success = Color(0xFF69A983),
        overlayVignette = Color(0xFF1A1A1A),
        grainIntensity = 0.18f,
        vignetteAlpha = 0.45f,
        vignetteStyle = "Iron Script"
    )

    val Dungeon = AtmospherePalette(
        background = AshBlack,
        surface = Iron,
        elevated = IronElevated,
        accent = Oxblood,
        onBackground = Vellum,
        onSurface = Vellum,
        outline = FadedBone,
        danger = BloodRed,
        success = HealGreen,
        overlayVignette = Color(0xFF1A0A0A),
        grainIntensity = 0.22f,
        vignetteAlpha = 0.55f,
        vignetteStyle = "Blood Inquisition"
    )

    val Camp = AtmospherePalette(
        background = Color(0xFF17100B),
        surface = Color(0xFF241A12),
        elevated = Color(0xFF302319),
        accent = AgedGold,
        onBackground = Color(0xFFF0E2C8),
        onSurface = Color(0xFFE5D2B2),
        outline = Color(0xFF806A47),
        danger = Color(0xFFC25A3E),
        success = Color(0xFF83A85D),
        overlayVignette = Color(0xFF2A1A0A),
        grainIntensity = 0.14f,
        vignetteAlpha = 0.35f,
        vignetteStyle = "Candlelit Chronicle"
    )

    val WorldMap = AtmospherePalette(
        background = Color(0xFF0D1620),
        surface = Color(0xFF172331),
        elevated = Color(0xFF203247),
        accent = ManaBlue,
        onBackground = Color(0xFFE0E9F1),
        onSurface = Color(0xFFD0DCE8),
        outline = Color(0xFF566C84),
        danger = Color(0xFFB95B4D),
        success = Color(0xFF68A97D),
        overlayVignette = Color(0xFF1A1A2A),
        grainIntensity = 0.08f,
        vignetteAlpha = 0.25f,
        vignetteStyle = "Cartographer's Folio"
    )

    val Dialogue = AtmospherePalette(
        background = Color(0xFF121016),
        surface = Color(0xFF1E1A24),
        elevated = Color(0xFF2A2431),
        accent = Color(0xFFB28B5F),
        onBackground = Color(0xFFECE1D3),
        onSurface = Color(0xFFE2D3C2),
        outline = Color(0xFF756477),
        danger = Color(0xFFBE504A),
        success = Color(0xFF79A76D),
        overlayVignette = Color(0xFF2A1A0A),
        grainIntensity = 0.12f,
        vignetteAlpha = 0.30f,
        vignetteStyle = "Scribe's Record"
    )

    fun paletteFor(atmosphere: SceneAtmosphere): AtmospherePalette = when (atmosphere) {
        SceneAtmosphere.FOREST -> Forest
        SceneAtmosphere.CAVE -> Cave
        SceneAtmosphere.DUNGEON -> Dungeon
        SceneAtmosphere.CAMP -> Camp
        SceneAtmosphere.WORLD_MAP -> WorldMap
        SceneAtmosphere.DIALOGUE -> Dialogue
    }
}

fun SceneAtmosphere.Companion.fromRoute(route: String?): SceneAtmosphere = when {
    route == "map" -> SceneAtmosphere.WORLD_MAP
    route == "camp" || route == "inventory" || route == "crafting" -> SceneAtmosphere.CAMP
    route?.startsWith("dialogue") == true -> SceneAtmosphere.DIALOGUE
    route?.startsWith("duel") == true -> SceneAtmosphere.DUNGEON
    route == "journal" || route == "party" || route == "settings" -> SceneAtmosphere.DIALOGUE
    else -> SceneAtmosphere.FOREST
}
