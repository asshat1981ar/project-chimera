# Chimera Visual Upgrade Phase 1 and 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Phase 1 visual polish system and Phase 2 art integration foundation for Chimera's Android Compose RPG UI without changing simulation behavior.

**Architecture:** Keep visual infrastructure in `core-ui` so feature modules consume shared tokens, icons, overlays, and art helpers without cross-feature dependencies. Preserve `ChimeraTheme` as a compatibility wrapper while introducing `AtmosphereTheme` and `AtmosphereScaffold`. Phase 2 adds manifests, placeholders, and rendering hooks for future paid art assets; it does not require final commissioned PNGs to compile.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Android resources, Coil, Hilt-backed existing screens, JUnit unit tests, existing Gradle Android modules.

---

## Current Repo Facts

- `core-ui` currently owns `Theme.kt`, `ChimeraColors.kt`, `ChimeraTypography.kt`, `SharedComponents.kt`, and `NpcPortrait.kt`.
- `app` wraps content in `ChimeraTheme` from `MainActivity.kt`; navigation is in `ChimeraNavHost.kt`.
- Feature modules use Material 3 directly and import `Icons.Default.*` in several screens.
- Fonts must be added under `core-ui/src/main/res/font/`, not `app/src/main/res/font/`, because `ChimeraTypography.kt` compiles inside the `core-ui` library.
- The checked-in scene JSON has 30 dialogue scenes: 10 Act 1, 10 Act 2, 10 Act 3. Phase 2 maps those 30 scene IDs onto 20 reusable background sets.
- The working tree already has unrelated local changes in `.claude/worktrees/*`, `.codex`, `.serena`, `AGENTS.md`, and `core-data/src/main/kotlin/com/chimera/data/SceneLoader.kt`. Do not overwrite or revert them while implementing this plan.

## File Structure

- Create `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`: scene atmosphere enum, palette tokens, mapping helpers, and pure testable color data.
- Create `core-ui/src/main/kotlin/com/chimera/ui/theme/AtmosphereTheme.kt`: `AtmosphereTheme`, `LocalSceneAtmosphere`, `AtmosphereScaffold`, `AtmosphereSurface`, `AtmosphereCard`, and `AtmosphereButton`.
- Modify `core-ui/src/main/kotlin/com/chimera/ui/theme/Theme.kt`: keep `ChimeraTheme` and delegate to `AtmosphereTheme(SceneAtmosphere.DUNGEON)`.
- Modify `core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt`: replace system font families with embedded Cinzel, Source Sans 3, and JetBrains Mono families; expose `ChimeraTextStyles`.
- Create `core-ui/src/main/res/font/*.ttf`: font resources used by `core-ui`.
- Create `core-ui/src/main/kotlin/com/chimera/ui/icons/ChimeraIcons.kt`: custom 24 dp line-art `ImageVector` icons and small composable helpers such as `DispositionHeart`.
- Create `core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt`: vignette and deterministic grain overlay.
- Create `core-ui/src/main/kotlin/com/chimera/ui/components/ParticleOverlay.kt`: six lightweight Compose particle overlays.
- Create `core-ui/src/main/kotlin/com/chimera/ui/components/ParallaxBackground.kt`: background-layer renderer with placeholder fallback.
- Create `core-ui/src/main/kotlin/com/chimera/ui/assets/SceneVisuals.kt`: scene-to-background, atmosphere, particle, and NPC expression mapping.
- Create `app/src/main/assets/visual/scene_background_manifest.json`: 20 background set manifest mapped to real scene IDs.
- Create `docs/art/visual-upgrade-phase-2.md`: production-ready artist spec for backgrounds, particles, and expression sprite sheets.
- Modify `MainActivity.kt`, `TopLevelDestination.kt`, `ChimeraBottomBar.kt`, and key screens to consume `AtmosphereScaffold`, `ChimeraIcons`, particle overlays, and visual mappings.

## Task 1: Baseline Compile and Visual Surface Inventory

**Files:**
- Read: `core-ui/src/main/kotlin/com/chimera/ui/theme/*.kt`
- Read: `app/src/main/kotlin/com/chimera/ui/MainActivity.kt`
- Read: `app/src/main/kotlin/com/chimera/ui/navigation/*.kt`
- Read: `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`
- Read: `feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt`
- Read: `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneScreen.kt`
- Read: `feature-camp/src/main/kotlin/com/chimera/feature/camp/CampScreen.kt`
- Read: `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt`
- Read: `app/src/main/kotlin/com/chimera/ui/screens/duel/DuelScreen.kt`

- [ ] **Step 1: Create implementation branch or isolated worktree**

Run:
```bash
git worktree add .worktrees/visual-upgrade-phase-1-2 -b visual-upgrade-phase-1-2
cd .worktrees/visual-upgrade-phase-1-2
```

Expected: a clean worktree rooted at `.worktrees/visual-upgrade-phase-1-2`.

- [ ] **Step 2: Record current status before edits**

Run:
```bash
git status --short
```

Expected: no implementation changes in the new worktree. If the main worktree has unrelated changes, leave them untouched.

- [ ] **Step 3: Run baseline build for the affected shared UI module**

Run:
```bash
./gradlew :core-ui:test
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Run baseline app compile**

Run:
```bash
./gradlew assembleMockDebug
```

Expected: `BUILD SUCCESSFUL`. If it fails before this plan's edits, capture the first compile error and fix only if it blocks all visual work.

- [ ] **Step 5: Commit baseline note if a blocker fix was required**

Only if Step 4 required a fix:
```bash
git add path/to/blocking-file.kt
git commit -m "fix: restore baseline mock debug build

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 2: Atmosphere Tokens and Theme Primitives

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`
- Create: `core-ui/src/main/kotlin/com/chimera/ui/theme/AtmosphereTheme.kt`
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/theme/Theme.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt`

- [ ] **Step 1: Write the failing token tests**

Create `core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt`:
```kotlin
package com.chimera.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AtmosphereTest {
    @Test
    fun `all scene atmospheres have distinct palettes`() {
        val palettes = SceneAtmosphere.values().map { AtmosphereTokens.paletteFor(it) }

        assertEquals(SceneAtmosphere.values().size, palettes.map { it.background }.toSet().size)
        assertEquals(SceneAtmosphere.values().size, palettes.map { it.surface }.toSet().size)
        assertTrue(palettes.all { it.overlayVignette in 0f..0.8f })
        assertTrue(palettes.all { it.grainIntensity in 0f..0.1f })
    }

    @Test
    fun `known routes map to expected atmospheres`() {
        assertEquals(SceneAtmosphere.WORLD_MAP, SceneAtmosphere.fromRoute("map"))
        assertEquals(SceneAtmosphere.CAMP, SceneAtmosphere.fromRoute("camp"))
        assertEquals(SceneAtmosphere.DIALOGUE, SceneAtmosphere.fromRoute("dialogue/prologue_scene_1"))
        assertEquals(SceneAtmosphere.DUNGEON, SceneAtmosphere.fromRoute("duel/warden"))
        assertEquals(SceneAtmosphere.FOREST, SceneAtmosphere.fromRoute("home"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.theme.AtmosphereTest"
```

Expected: compile failure because `SceneAtmosphere` and `AtmosphereTokens` do not exist.

- [ ] **Step 3: Implement atmosphere tokens**

Create `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`:
```kotlin
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
    val overlayVignette: Float,
    val grainIntensity: Float
)

object AtmosphereTokens {
    val Forest = AtmospherePalette(
        background = Color(0xFF122414),
        surface = Color(0xFF203820),
        elevated = Color(0xFF2D4A2D),
        accent = Color(0xFF9AC27D),
        onBackground = Color(0xFFE8E1C7),
        onSurface = Color(0xFFF0E8D2),
        outline = Color(0xFF687A52),
        danger = Color(0xFFBC6F5E),
        success = Color(0xFF9AC27D),
        overlayVignette = 0.34f,
        grainIntensity = 0.025f
    )

    val Cave = AtmospherePalette(
        background = Color(0xFF111826),
        surface = Color(0xFF1B2638),
        elevated = Color(0xFF28364C),
        accent = Color(0xFF8FA7C7),
        onBackground = Color(0xFFE2E8F0),
        onSurface = Color(0xFFEAF0F7),
        outline = Color(0xFF52647D),
        danger = Color(0xFFC78484),
        success = Color(0xFF8FC7B1),
        overlayVignette = 0.48f,
        grainIntensity = 0.035f
    )

    val Dungeon = AtmospherePalette(
        background = Color(0xFF261111),
        surface = Color(0xFF371C1A),
        elevated = Color(0xFF4A2D2D),
        accent = Color(0xFFE09A63),
        onBackground = Color(0xFFF0DDC8),
        onSurface = Color(0xFFFFEAD8),
        outline = Color(0xFF835A4D),
        danger = Color(0xFFE56A54),
        success = Color(0xFF9DBF82),
        overlayVignette = 0.58f,
        grainIntensity = 0.045f
    )

    val Camp = AtmospherePalette(
        background = Color(0xFF201811),
        surface = Color(0xFF332518),
        elevated = Color(0xFF4A3320),
        accent = Color(0xFFFFB85C),
        onBackground = Color(0xFFFFE8C7),
        onSurface = Color(0xFFFFEED7),
        outline = Color(0xFF8B6742),
        danger = Color(0xFFD76F57),
        success = Color(0xFFC0C06A),
        overlayVignette = 0.40f,
        grainIntensity = 0.030f
    )

    val WorldMap = AtmospherePalette(
        background = Color(0xFF151A1A),
        surface = Color(0xFF222928),
        elevated = Color(0xFF303937),
        accent = Color(0xFFD7B56D),
        onBackground = Color(0xFFEDE2CB),
        onSurface = Color(0xFFF4E9D2),
        outline = Color(0xFF6D7364),
        danger = Color(0xFFC56E5B),
        success = Color(0xFF8CB381),
        overlayVignette = 0.42f,
        grainIntensity = 0.035f
    )

    val Dialogue = AtmospherePalette(
        background = Color(0xFF151114),
        surface = Color(0xFF241C22),
        elevated = Color(0xFF332630),
        accent = Color(0xFFC9A66B),
        onBackground = Color(0xFFF0E5D3),
        onSurface = Color(0xFFFFEFD8),
        outline = Color(0xFF6F5B58),
        danger = Color(0xFFD36B64),
        success = Color(0xFF92B887),
        overlayVignette = 0.50f,
        grainIntensity = 0.050f
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
    route == null -> SceneAtmosphere.FOREST
    route == "map" -> SceneAtmosphere.WORLD_MAP
    route == "camp" || route == "inventory" || route == "crafting" -> SceneAtmosphere.CAMP
    route.startsWith("dialogue") -> SceneAtmosphere.DIALOGUE
    route.startsWith("duel") -> SceneAtmosphere.DUNGEON
    route == "journal" || route == "party" || route == "settings" -> SceneAtmosphere.DIALOGUE
    else -> SceneAtmosphere.FOREST
}
```

- [ ] **Step 4: Implement `AtmosphereTheme` primitives**

Create `core-ui/src/main/kotlin/com/chimera/ui/theme/AtmosphereTheme.kt`:
```kotlin
package com.chimera.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chimera.ui.components.AtmosphereOverlay

val LocalSceneAtmosphere = staticCompositionLocalOf { SceneAtmosphere.FOREST }

private val AtmosphereShapes = Shapes(
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
        onPrimary = Color(0xFF16110C),
        secondary = palette.success,
        onSecondary = Color(0xFF111511),
        tertiary = palette.danger,
        onTertiary = Color.White,
        background = palette.background,
        onBackground = palette.onBackground,
        surface = palette.surface,
        onSurface = palette.onSurface,
        surfaceVariant = palette.elevated,
        onSurfaceVariant = palette.onSurface,
        outline = palette.outline,
        outlineVariant = palette.outline.copy(alpha = 0.42f),
        error = palette.danger,
        onError = Color.White
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
    overlay: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit
) {
    AtmosphereTheme(atmosphere = atmosphere) {
        val palette = AtmosphereTokens.paletteFor(atmosphere)
        Scaffold(
            modifier = modifier,
            bottomBar = bottomBar,
            containerColor = palette.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                content(padding)
                if (overlay) {
                    AtmosphereOverlay(
                        grainIntensity = palette.grainIntensity,
                        vignetteStrength = palette.overlayVignette,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }
    }
}

@Composable
fun AtmosphereSurface(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = tonalElevation,
        content = content
    )
}

@Composable
fun AtmosphereCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    shape: Shape = MaterialTheme.shapes.large,
    borderAlpha: Float = 0.38f,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.32f),
            spotColor = Color.Black.copy(alpha = 0.50f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha)),
        content = content
    )
}

@Composable
fun AtmosphereButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.shadow(
            elevation = if (enabled) 6.dp else 0.dp,
            shape = MaterialTheme.shapes.medium,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
            spotColor = Color.Black.copy(alpha = 0.45f)
        ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        content = content
    )
}
```

- [ ] **Step 5: Preserve `ChimeraTheme` as a compatibility wrapper**

Replace `core-ui/src/main/kotlin/com/chimera/ui/theme/Theme.kt` with:
```kotlin
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
```

- [ ] **Step 6: Run token tests**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.theme.AtmosphereTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

Run:
```bash
git add core-ui/src/main/kotlin/com/chimera/ui/theme core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt
git commit -m "feat: add atmosphere theme tokens

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 3: Typography and Embedded Fonts

**Files:**
- Create: `core-ui/src/main/res/font/cinzel_variable.ttf`
- Create: `core-ui/src/main/res/font/source_sans_3_variable.ttf`
- Create: `core-ui/src/main/res/font/jetbrains_mono_variable.ttf`
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/TypographyTest.kt`

- [ ] **Step 1: Download font files into `core-ui` resources**

Run:
```bash
mkdir -p core-ui/src/main/res/font
curl -L -o core-ui/src/main/res/font/cinzel_variable.ttf "https://github.com/google/fonts/raw/main/ofl/cinzel/Cinzel%5Bwght%5D.ttf"
curl -L -o core-ui/src/main/res/font/source_sans_3_variable.ttf "https://github.com/google/fonts/raw/main/ofl/sourcesans3/SourceSans3%5Bwght%5D.ttf"
curl -L -o core-ui/src/main/res/font/jetbrains_mono_variable.ttf "https://github.com/google/fonts/raw/main/ofl/jetbrainsmono/JetBrainsMono%5Bwght%5D.ttf"
```

Expected: three `.ttf` files exist under `core-ui/src/main/res/font/`.

- [ ] **Step 2: Write typography tests**

Create `core-ui/src/test/kotlin/com/chimera/ui/theme/TypographyTest.kt`:
```kotlin
package com.chimera.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyTest {
    @Test
    fun `chimera text styles expose required six semantic styles`() {
        assertEquals(6, ChimeraTextStyles.all.size)
        assertTrue(ChimeraTextStyles.display.fontSize.value > ChimeraTextStyles.header.fontSize.value)
        assertTrue(ChimeraTextStyles.header.fontSize.value > ChimeraTextStyles.body.fontSize.value)
        assertEquals(14f, ChimeraTextStyles.mono.fontSize.value)
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.theme.TypographyTest"
```

Expected: compile failure because `ChimeraTextStyles` does not exist.

- [ ] **Step 4: Replace typography implementation**

Replace `core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt` with:
```kotlin
package com.chimera.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import com.chimera.ui.R

val Cinzel = FontFamily(
    Font(R.font.cinzel_variable, FontWeight.Normal),
    Font(R.font.cinzel_variable, FontWeight.SemiBold),
    Font(R.font.cinzel_variable, FontWeight.Bold)
)

val SourceSans3 = FontFamily(
    Font(R.font.source_sans_3_variable, FontWeight.Normal),
    Font(R.font.source_sans_3_variable, FontWeight.Medium),
    Font(R.font.source_sans_3_variable, FontWeight.SemiBold)
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Normal),
    Font(R.font.jetbrains_mono_variable, FontWeight.Medium),
    Font(R.font.jetbrains_mono_variable, FontWeight.Bold)
)

object ChimeraTextStyles {
    val display = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Bold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        color = ParchmentWhite,
        shadow = Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 3f), 6f)
    )
    val header = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = ParchmentWhite,
        shadow = Shadow(Color.Black.copy(alpha = 0.30f), Offset(0f, 2f), 4f)
    )
    val subheader = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = ParchmentWhite
    )
    val body = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = ParchmentWhite
    )
    val caption = TextStyle(
        fontFamily = SourceSans3,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = FadedBone
    )
    val mono = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = FadedBone
    )
    val storyMoment = body.copy(
        fontWeight = FontWeight.SemiBold,
        shadow = Shadow(Color.Black.copy(alpha = 0.55f), Offset(0f, 2f), 5f)
    )
    val all = listOf(display, header, subheader, body, caption, mono)
}

private fun TextStyle.withPlatformDefaults() = copy(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

val ChimeraTypography = Typography(
    displayLarge = ChimeraTextStyles.display.withPlatformDefaults(),
    displayMedium = ChimeraTextStyles.header.copy(fontSize = 30.sp, lineHeight = 36.sp).withPlatformDefaults(),
    displaySmall = ChimeraTextStyles.header.withPlatformDefaults(),
    headlineLarge = ChimeraTextStyles.header.withPlatformDefaults(),
    headlineMedium = ChimeraTextStyles.header.copy(fontSize = 22.sp, lineHeight = 28.sp).withPlatformDefaults(),
    headlineSmall = ChimeraTextStyles.subheader.copy(fontSize = 18.sp, lineHeight = 24.sp).withPlatformDefaults(),
    titleLarge = ChimeraTextStyles.header.copy(fontSize = 20.sp, lineHeight = 26.sp).withPlatformDefaults(),
    titleMedium = ChimeraTextStyles.subheader.withPlatformDefaults(),
    titleSmall = ChimeraTextStyles.subheader.copy(fontSize = 15.sp, lineHeight = 20.sp).withPlatformDefaults(),
    bodyLarge = ChimeraTextStyles.body.withPlatformDefaults(),
    bodyMedium = ChimeraTextStyles.body.copy(fontSize = 14.sp, lineHeight = 20.sp, color = FadedBone).withPlatformDefaults(),
    bodySmall = ChimeraTextStyles.caption.withPlatformDefaults(),
    labelLarge = ChimeraTextStyles.subheader.copy(fontSize = 14.sp, lineHeight = 18.sp).withPlatformDefaults(),
    labelMedium = ChimeraTextStyles.caption.copy(fontSize = 12.sp, lineHeight = 16.sp).withPlatformDefaults(),
    labelSmall = ChimeraTextStyles.caption.copy(fontSize = 11.sp, lineHeight = 14.sp, color = DimAsh).withPlatformDefaults()
)
```

- [ ] **Step 5: Run typography tests**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.theme.TypographyTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Run app compile to verify library resources resolve**

Run:
```bash
./gradlew assembleMockDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

Run:
```bash
git add core-ui/src/main/res/font core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt core-ui/src/test/kotlin/com/chimera/ui/theme/TypographyTest.kt
git commit -m "feat: embed chimera typography system

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 4: Custom Iconography System

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/icons/ChimeraIcons.kt`
- Modify: `app/src/main/kotlin/com/chimera/ui/navigation/TopLevelDestination.kt`
- Modify: selected feature screens that import `androidx.compose.material.icons.*`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/icons/ChimeraIconsTest.kt`

- [ ] **Step 1: Write icon coverage test**

Create `core-ui/src/test/kotlin/com/chimera/ui/icons/ChimeraIconsTest.kt`:
```kotlin
package com.chimera.ui.icons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChimeraIconsTest {
    @Test
    fun `icon set contains at least twenty custom icons`() {
        assertTrue(ChimeraIcons.all.size >= 20)
        assertEquals(ChimeraIcons.all.size, ChimeraIcons.all.map { it.name }.toSet().size)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.icons.ChimeraIconsTest"
```

Expected: compile failure because `ChimeraIcons` does not exist.

- [ ] **Step 3: Implement `ChimeraIcons`**

Create `core-ui/src/main/kotlin/com/chimera/ui/icons/ChimeraIcons.kt` with 24 dp line-art vectors. The file must expose these exact names:
```kotlin
package com.chimera.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object ChimeraIcons {
    val MapNode = lineIcon("MapNode") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(12f, 21f); cubicTo(12f, 21f, 5f, 14f, 5f, 8.5f); cubicTo(5f, 4.7f, 8.1f, 2f, 12f, 2f); cubicTo(15.9f, 2f, 19f, 4.7f, 19f, 8.5f); cubicTo(19f, 14f, 12f, 21f, 12f, 21f)
            moveTo(12f, 11f); arcTo(2.5f, 2.5f, 0f, true, true, 12f, 6f); arcTo(2.5f, 2.5f, 0f, true, true, 12f, 11f)
        }
    }
    val MapNodeLocked = lineIcon("MapNodeLocked") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(7f, 11f); lineTo(17f, 11f); lineTo(17f, 20f); lineTo(7f, 20f); close()
            moveTo(9f, 11f); lineTo(9f, 8f); cubicTo(9f, 5.8f, 10.4f, 4.5f, 12f, 4.5f); cubicTo(13.6f, 4.5f, 15f, 5.8f, 15f, 8f); lineTo(15f, 11f)
        }
    }
    val MapNodeCompleted = lineIcon("MapNodeCompleted") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(4f, 12f); lineTo(9f, 17f); lineTo(20f, 6f)
            moveTo(12f, 21f); cubicTo(12f, 21f, 5f, 14f, 5f, 8.5f); cubicTo(5f, 4.7f, 8.1f, 2f, 12f, 2f); cubicTo(15.9f, 2f, 19f, 4.7f, 19f, 8.5f)
        }
    }
    val DispositionHeart = lineIcon("DispositionHeart") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(12f, 21f); cubicTo(12f, 21f, 4f, 16.5f, 4f, 9.5f); cubicTo(4f, 6f, 6.5f, 4f, 9f, 4f); cubicTo(10.5f, 4f, 11.5f, 4.8f, 12f, 6f); cubicTo(12.5f, 4.8f, 13.5f, 4f, 15f, 4f); cubicTo(17.5f, 4f, 20f, 6f, 20f, 9.5f); cubicTo(20f, 16.5f, 12f, 21f, 12f, 21f)
        }
    }
    val SceneForest = lineIcon("SceneForest") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(4f, 20f); lineTo(20f, 20f); moveTo(8f, 20f); lineTo(8f, 10f); moveTo(16f, 20f); lineTo(16f, 9f)
            moveTo(8f, 4f); lineTo(3f, 12f); lineTo(13f, 12f); close(); moveTo(16f, 3f); lineTo(11f, 12f); lineTo(21f, 12f); close()
        }
    }
    val SceneCave = lineIcon("SceneCave") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(3f, 20f); lineTo(3f, 12f); cubicTo(3f, 6f, 7f, 3f, 12f, 3f); cubicTo(17f, 3f, 21f, 6f, 21f, 12f); lineTo(21f, 20f)
            moveTo(9f, 20f); lineTo(9f, 13f); cubicTo(9f, 11f, 10f, 10f, 12f, 10f); cubicTo(14f, 10f, 15f, 11f, 15f, 13f); lineTo(15f, 20f)
        }
    }
    val SceneDungeon = lineIcon("SceneDungeon") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(5f, 21f); lineTo(5f, 8f); lineTo(12f, 3f); lineTo(19f, 8f); lineTo(19f, 21f)
            moveTo(9f, 21f); lineTo(9f, 14f); cubicTo(9f, 12.3f, 10.3f, 11f, 12f, 11f); cubicTo(13.7f, 11f, 15f, 12.3f, 15f, 14f); lineTo(15f, 21f)
        }
    }
    val SceneCamp = lineIcon("SceneCamp") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(4f, 20f); lineTo(12f, 5f); lineTo(20f, 20f); moveTo(8f, 20f); lineTo(12f, 12f); lineTo(16f, 20f)
            moveTo(7f, 20f); lineTo(17f, 20f)
        }
    }
    val MemoryOrb = lineIcon("MemoryOrb") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(12f, 21f); arcTo(9f, 9f, 0f, true, true, 12f, 3f); arcTo(9f, 9f, 0f, true, true, 12f, 21f)
            moveTo(8f, 12f); cubicTo(10f, 9f, 14f, 15f, 16f, 12f)
        }
    }
    val DialogueBubble = lineIcon("DialogueBubble") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 15f); lineTo(13f, 15f); lineTo(8f, 20f); lineTo(8f, 15f); lineTo(4f, 15f); close()
        }
    }
    val VowSeal = lineIcon("VowSeal") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(12f, 3f); lineTo(19f, 7f); lineTo(19f, 15f); lineTo(12f, 21f); lineTo(5f, 15f); lineTo(5f, 7f); close()
            moveTo(9f, 12f); lineTo(11f, 14f); lineTo(15f, 9f)
        }
    }
    val OmensStrike = lineIcon("OmensStrike") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(5f, 20f); lineTo(19f, 6f); moveTo(15f, 5f); lineTo(20f, 5f); lineTo(20f, 10f); moveTo(7f, 18f); lineTo(4f, 21f)
        }
    }
    val OmensWard = lineIcon("OmensWard") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(12f, 3f); lineTo(19f, 6f); lineTo(18f, 14f); cubicTo(17f, 18f, 14f, 20f, 12f, 21f); cubicTo(10f, 20f, 7f, 18f, 6f, 14f); lineTo(5f, 6f); close()
        }
    }
    val OmensFeint = lineIcon("OmensFeint") {
        path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
            moveTo(4f, 8f); cubicTo(8f, 3f, 16f, 3f, 20f, 8f); moveTo(20f, 8f); lineTo(16f, 8f); moveTo(20f, 8f); lineTo(20f, 4f)
            moveTo(20f, 16f); cubicTo(16f, 21f, 8f, 21f, 4f, 16f); moveTo(4f, 16f); lineTo(8f, 16f); moveTo(4f, 16f); lineTo(4f, 20f)
        }
    }
    val Home = SceneForest
    val Map = MapNode
    val Camp = SceneCamp
    val Journal = MemoryOrb
    val Party = DispositionHeart
    val Settings = lineIcon("Settings") { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(12f, 8f); arcTo(4f, 4f, 0f, true, true, 12f, 16f); arcTo(4f, 4f, 0f, true, true, 12f, 8f); moveTo(12f, 2f); lineTo(12f, 5f); moveTo(12f, 19f); lineTo(12f, 22f); moveTo(2f, 12f); lineTo(5f, 12f); moveTo(19f, 12f); lineTo(22f, 12f) } }
    val Back = lineIcon("Back") { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(15f, 6f); lineTo(9f, 12f); lineTo(15f, 18f) } }
    val Send = lineIcon("Send") { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(3f, 20f); lineTo(21f, 12f); lineTo(3f, 4f); lineTo(7f, 12f); close(); moveTo(7f, 12f); lineTo(21f, 12f) } }
    val Search = lineIcon("Search") { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(11f, 18f); arcTo(7f, 7f, 0f, true, true, 11f, 4f); arcTo(7f, 7f, 0f, true, true, 11f, 18f); moveTo(16f, 16f); lineTo(21f, 21f) } }
    val Clear = lineIcon("Clear") { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f) } }
    val Forge = lineIcon("Forge") { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(5f, 19f); lineTo(19f, 19f); moveTo(7f, 19f); lineTo(9f, 11f); lineTo(15f, 11f); lineTo(17f, 19f); moveTo(8f, 7f); lineTo(16f, 7f); moveTo(12f, 3f); lineTo(12f, 11f) } }

    val all = listOf(
        MapNode, MapNodeLocked, MapNodeCompleted, DispositionHeart, SceneForest,
        SceneCave, SceneDungeon, SceneCamp, MemoryOrb, DialogueBubble, VowSeal,
        OmensStrike, OmensWard, OmensFeint, Home, Map, Camp, Journal, Party,
        Settings, Back, Send, Search, Clear, Forge
    )
}

private fun lineIcon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply(block).build()

@Composable
fun DispositionHeart(
    disposition: Float,
    modifier: Modifier = Modifier,
    contentDescription: String = "Relationship"
) {
    val tint = when {
        disposition > 0.7f -> Color(0xFFFF6B6B)
        disposition > 0.4f -> Color(0xFFFFB84D)
        else -> Color(0xFF6B7B8F)
    }
    Icon(
        imageVector = ChimeraIcons.DispositionHeart,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
            .size(24.dp)
            .drawBehind {
                if (disposition > 0.7f) {
                    drawCircle(color = tint.copy(alpha = 0.26f), radius = size.minDimension)
                }
            }
    )
}
```

- [ ] **Step 4: Replace bottom navigation icons**

Modify `TopLevelDestination.kt` so it imports `com.chimera.ui.icons.ChimeraIcons` and uses:
```kotlin
HOME("home", ChimeraIcons.Home, "Home"),
MAP("map", ChimeraIcons.Map, "Map"),
CAMP("camp", ChimeraIcons.Camp, "Camp"),
JOURNAL("journal", ChimeraIcons.Journal, "Journal"),
PARTY("party", ChimeraIcons.Party, "Party")
```

- [ ] **Step 5: Replace feature system icon imports**

For every `androidx.compose.material.icons.*` usage in app and feature screens, replace with `ChimeraIcons` equivalents:
```kotlin
Icon(ChimeraIcons.Back, contentDescription = "Back")
Icon(ChimeraIcons.Send, contentDescription = "Send dialogue")
Icon(ChimeraIcons.Search, contentDescription = null)
Icon(ChimeraIcons.Clear, contentDescription = "Clear search")
Icon(ChimeraIcons.MemoryOrb, contentDescription = null)
Icon(ChimeraIcons.DispositionHeart, contentDescription = "Relationship")
Icon(ChimeraIcons.OmensStrike, contentDescription = "Strike")
Icon(ChimeraIcons.OmensWard, contentDescription = "Ward")
Icon(ChimeraIcons.OmensFeint, contentDescription = "Feint")
```

- [ ] **Step 6: Run icon tests and compile**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.icons.ChimeraIconsTest"
./gradlew assembleMockDebug
```

Expected: both commands end with `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

Run:
```bash
git add core-ui/src/main/kotlin/com/chimera/ui/icons app/src/main/kotlin/com/chimera/ui/navigation feature-* app/src/main/kotlin/com/chimera/ui/screens
git commit -m "feat: add custom chimera icon system

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 5: Grain, Vignette, and Particle Overlays

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt`
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/ParticleOverlay.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/components/ParticleOverlayTest.kt`

- [ ] **Step 1: Write particle mapping test**

Create `core-ui/src/test/kotlin/com/chimera/ui/components/ParticleOverlayTest.kt`:
```kotlin
package com.chimera.ui.components

import com.chimera.ui.theme.SceneAtmosphere
import org.junit.Assert.assertEquals
import org.junit.Test

class ParticleOverlayTest {
    @Test
    fun `atmosphere maps to expected default particle types`() {
        assertEquals(ParticleType.FIREFLY, ParticleType.forAtmosphere(SceneAtmosphere.CAMP))
        assertEquals(ParticleType.MIST, ParticleType.forAtmosphere(SceneAtmosphere.CAVE))
        assertEquals(ParticleType.ASH, ParticleType.forAtmosphere(SceneAtmosphere.DUNGEON))
        assertEquals(ParticleType.LEAVES, ParticleType.forAtmosphere(SceneAtmosphere.FOREST))
        assertEquals(ParticleType.NONE, ParticleType.forAtmosphere(SceneAtmosphere.WORLD_MAP))
        assertEquals(ParticleType.DUST, ParticleType.forAtmosphere(SceneAtmosphere.DIALOGUE))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.components.ParticleOverlayTest"
```

Expected: compile failure because `ParticleType` does not exist.

- [ ] **Step 3: Implement `AtmosphereOverlay`**

Create `core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt`:
```kotlin
package com.chimera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.absoluteValue

@Composable
fun AtmosphereOverlay(
    grainIntensity: Float = 0.03f,
    vignetteStrength: Float = 0.4f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawVignette(vignetteStrength.coerceIn(0f, 0.8f))
        drawDeterministicGrain(grainIntensity.coerceIn(0f, 0.1f))
    }
}

private fun DrawScope.drawVignette(strength: Float) {
    if (strength <= 0f) return
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = strength)),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.minDimension * 0.82f,
            tileMode = TileMode.Clamp
        )
    )
}

private fun DrawScope.drawDeterministicGrain(intensity: Float) {
    if (intensity <= 0f) return
    val step = 18
    val alpha = intensity.coerceIn(0f, 0.1f)
    var y = 0
    while (y < size.height.toInt()) {
        var x = 0
        while (x < size.width.toInt()) {
            val hash = ((x * 73856093) xor (y * 19349663)).absoluteValue
            if (hash % 7 == 0) {
                drawCircle(
                    color = Color.White.copy(alpha = alpha * ((hash % 100) / 100f)),
                    radius = 0.65f,
                    center = Offset(x.toFloat(), y.toFloat())
                )
            }
            x += step
        }
        y += step
    }
}
```

- [ ] **Step 4: Implement `ParticleOverlay`**

Create `core-ui/src/main/kotlin/com/chimera/ui/components/ParticleOverlay.kt`:
```kotlin
package com.chimera.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.chimera.ui.theme.SceneAtmosphere
import kotlin.math.sin

enum class ParticleType {
    NONE,
    ASH,
    FIREFLY,
    MIST,
    LEAVES,
    EMBER,
    DUST;

    companion object {
        fun forAtmosphere(atmosphere: SceneAtmosphere): ParticleType = when (atmosphere) {
            SceneAtmosphere.FOREST -> LEAVES
            SceneAtmosphere.CAVE -> MIST
            SceneAtmosphere.DUNGEON -> ASH
            SceneAtmosphere.CAMP -> FIREFLY
            SceneAtmosphere.WORLD_MAP -> NONE
            SceneAtmosphere.DIALOGUE -> DUST
        }
    }
}

@Composable
fun ParticleOverlay(
    type: ParticleType,
    modifier: Modifier = Modifier,
    particleCount: Int = 36
) {
    if (type == ParticleType.NONE) return
    val transition = rememberInfiniteTransition(label = "particle_overlay")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "particle_progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        repeat(particleCount.coerceIn(8, 80)) { index ->
            val seed = index * 37
            val baseX = ((seed * 53) % 100) / 100f * size.width
            val drift = sin((progress * 6.28f) + index) * 18f
            val baseY = (((seed * 97) % 100) / 100f * size.height + progress * size.height) % size.height
            val color = when (type) {
                ParticleType.ASH -> Color(0xFFB8B0A6).copy(alpha = 0.18f)
                ParticleType.FIREFLY -> Color(0xFFFFC66D).copy(alpha = 0.42f)
                ParticleType.MIST -> Color(0xFFB8C7D9).copy(alpha = 0.12f)
                ParticleType.LEAVES -> Color(0xFF9AC27D).copy(alpha = 0.16f)
                ParticleType.EMBER -> Color(0xFFFF8A4C).copy(alpha = 0.28f)
                ParticleType.DUST -> Color(0xFFD8C4A4).copy(alpha = 0.14f)
                ParticleType.NONE -> Color.Transparent
            }
            val radius = when (type) {
                ParticleType.MIST -> 5f
                ParticleType.FIREFLY -> 2.2f
                else -> 1.6f
            }
            drawCircle(color = color, radius = radius, center = Offset(baseX + drift, baseY))
        }
    }
}
```

- [ ] **Step 5: Run overlay tests**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.components.ParticleOverlayTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

Run:
```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt core-ui/src/main/kotlin/com/chimera/ui/components/ParticleOverlay.kt core-ui/src/test/kotlin/com/chimera/ui/components/ParticleOverlayTest.kt
git commit -m "feat: add atmosphere and particle overlays

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 6: Phase 2 Scene Visual Manifest and Artist Spec

**Files:**
- Create: `app/src/main/assets/visual/scene_background_manifest.json`
- Create: `docs/art/visual-upgrade-phase-2.md`
- Create: `core-ui/src/main/kotlin/com/chimera/ui/assets/SceneVisuals.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/assets/SceneVisualsTest.kt`

- [ ] **Step 1: Write visual mapping tests**

Create `core-ui/src/test/kotlin/com/chimera/ui/assets/SceneVisualsTest.kt`:
```kotlin
package com.chimera.ui.assets

import com.chimera.ui.components.ParticleType
import com.chimera.ui.theme.SceneAtmosphere
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SceneVisualsTest {
    @Test
    fun `all known dialogue scenes have visual mappings`() {
        SceneVisuals.knownSceneIds.forEach { sceneId ->
            assertNotNull("Missing visual mapping for $sceneId", SceneVisuals.forScene(sceneId))
        }
        assertEquals(30, SceneVisuals.knownSceneIds.size)
    }

    @Test
    fun `camp route uses camp atmosphere and fireflies`() {
        val visual = SceneVisuals.forRoute("camp")
        assertEquals(SceneAtmosphere.CAMP, visual.atmosphere)
        assertEquals(ParticleType.FIREFLY, visual.particleType)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.assets.SceneVisualsTest"
```

Expected: compile failure because `SceneVisuals` does not exist.

- [ ] **Step 3: Implement scene visual mappings**

Create `core-ui/src/main/kotlin/com/chimera/ui/assets/SceneVisuals.kt`:
```kotlin
package com.chimera.ui.assets

import com.chimera.ui.components.ParticleType
import com.chimera.ui.theme.SceneAtmosphere

enum class Expression {
    IDLE,
    HAPPY,
    ANGRY,
    SAD,
    SURPRISED,
    THINKING
}

data class SceneVisual(
    val sceneId: String,
    val backgroundSet: String,
    val atmosphere: SceneAtmosphere,
    val particleType: ParticleType
)

object SceneVisuals {
    val knownSceneIds = listOf(
        "prologue_scene_1", "outer_ruins_1", "watchtower_1", "merchants_1", "deep_hollow_1",
        "elena_recruitment", "thorne_encounter", "vessa_shrine", "warden_betrayal", "hollow_approach",
        "ashen_gate", "ash_market", "memorial_field", "ember_sanctum", "reforged_camp",
        "aria_confession", "kael_loyalty", "echo_confrontation", "seren_alliance", "act2_climax",
        "coastal_arrival", "salvage_yard", "drowned_temple", "smugglers_cove", "tidewall_garrison",
        "aria_laboratory", "dara_ritual", "rook_betrayal", "seren_fleet", "act3_climax"
    )

    private val visuals = mapOf(
        "prologue_scene_1" to SceneVisual("prologue_scene_1", "act1_hollow_gate_ruins", SceneAtmosphere.FOREST, ParticleType.LEAVES),
        "outer_ruins_1" to SceneVisual("outer_ruins_1", "act1_outer_ruins_corridors", SceneAtmosphere.DUNGEON, ParticleType.DUST),
        "watchtower_1" to SceneVisual("watchtower_1", "act1_cracked_watchtower", SceneAtmosphere.WORLD_MAP, ParticleType.NONE),
        "merchants_1" to SceneVisual("merchants_1", "act1_outer_ruins_corridors", SceneAtmosphere.DIALOGUE, ParticleType.DUST),
        "deep_hollow_1" to SceneVisual("deep_hollow_1", "act1_deep_hollow_throne", SceneAtmosphere.DUNGEON, ParticleType.ASH),
        "elena_recruitment" to SceneVisual("elena_recruitment", "act1_outer_ruins_corridors", SceneAtmosphere.DIALOGUE, ParticleType.DUST),
        "thorne_encounter" to SceneVisual("thorne_encounter", "act1_deserter_camp", SceneAtmosphere.CAMP, ParticleType.FIREFLY),
        "vessa_shrine" to SceneVisual("vessa_shrine", "act1_broken_shrine", SceneAtmosphere.DUNGEON, ParticleType.ASH),
        "warden_betrayal" to SceneVisual("warden_betrayal", "act1_hidden_warden_chamber", SceneAtmosphere.CAVE, ParticleType.MIST),
        "hollow_approach" to SceneVisual("hollow_approach", "act1_deep_hollow_throne", SceneAtmosphere.DUNGEON, ParticleType.ASH),
        "ashen_gate" to SceneVisual("ashen_gate", "act2_ashen_gate_forge", SceneAtmosphere.DUNGEON, ParticleType.EMBER),
        "ash_market" to SceneVisual("ash_market", "act2_cinder_market", SceneAtmosphere.WORLD_MAP, ParticleType.ASH),
        "memorial_field" to SceneVisual("memorial_field", "act2_memorial_field", SceneAtmosphere.WORLD_MAP, ParticleType.ASH),
        "ember_sanctum" to SceneVisual("ember_sanctum", "act2_ember_sanctum", SceneAtmosphere.CAVE, ParticleType.EMBER),
        "reforged_camp" to SceneVisual("reforged_camp", "act2_reforged_enclave", SceneAtmosphere.CAMP, ParticleType.EMBER),
        "aria_confession" to SceneVisual("aria_confession", "act2_ruined_library", SceneAtmosphere.DIALOGUE, ParticleType.DUST),
        "kael_loyalty" to SceneVisual("kael_loyalty", "act2_reality_bending_forge", SceneAtmosphere.DUNGEON, ParticleType.EMBER),
        "echo_confrontation" to SceneVisual("echo_confrontation", "act2_ashen_throne", SceneAtmosphere.DUNGEON, ParticleType.ASH),
        "seren_alliance" to SceneVisual("seren_alliance", "act2_reforged_enclave", SceneAtmosphere.CAMP, ParticleType.EMBER),
        "act2_climax" to SceneVisual("act2_climax", "act2_ashen_throne", SceneAtmosphere.DUNGEON, ParticleType.ASH),
        "coastal_arrival" to SceneVisual("coastal_arrival", "act3_broken_shore_shipwreck", SceneAtmosphere.WORLD_MAP, ParticleType.MIST),
        "salvage_yard" to SceneVisual("salvage_yard", "act3_broken_shore_shipwreck", SceneAtmosphere.CAMP, ParticleType.EMBER),
        "drowned_temple" to SceneVisual("drowned_temple", "act3_drowned_temple_tide_pools", SceneAtmosphere.CAVE, ParticleType.MIST),
        "smugglers_cove" to SceneVisual("smugglers_cove", "act3_broken_shore_shipwreck", SceneAtmosphere.CAVE, ParticleType.DUST),
        "tidewall_garrison" to SceneVisual("tidewall_garrison", "act3_tidewall_caves_lab", SceneAtmosphere.DIALOGUE, ParticleType.MIST),
        "aria_laboratory" to SceneVisual("aria_laboratory", "act3_tidewall_caves_lab", SceneAtmosphere.CAVE, ParticleType.MIST),
        "dara_ritual" to SceneVisual("dara_ritual", "act3_drowned_temple_tide_pools", SceneAtmosphere.CAVE, ParticleType.MIST),
        "rook_betrayal" to SceneVisual("rook_betrayal", "act3_reforged_fleet_undersea_throne", SceneAtmosphere.DUNGEON, ParticleType.ASH),
        "seren_fleet" to SceneVisual("seren_fleet", "act3_reforged_fleet_undersea_throne", SceneAtmosphere.WORLD_MAP, ParticleType.MIST),
        "act3_climax" to SceneVisual("act3_climax", "act3_reforged_fleet_undersea_throne", SceneAtmosphere.DUNGEON, ParticleType.MIST)
    )

    fun forScene(sceneId: String): SceneVisual = visuals[sceneId]
        ?: SceneVisual(sceneId, "fallback_hollow_parchment", SceneAtmosphere.DIALOGUE, ParticleType.DUST)

    fun forRoute(route: String?): SceneVisual = when {
        route == "map" -> SceneVisual("map", "world_map_hollow", SceneAtmosphere.WORLD_MAP, ParticleType.NONE)
        route == "camp" || route == "inventory" || route == "crafting" -> SceneVisual("camp", "act1_camp_site", SceneAtmosphere.CAMP, ParticleType.FIREFLY)
        route == "home" -> SceneVisual("home", "act1_hollow_gate_ruins", SceneAtmosphere.FOREST, ParticleType.LEAVES)
        route?.startsWith("dialogue/") == true -> forScene(route.substringAfter("dialogue/"))
        route?.startsWith("duel/") == true -> SceneVisual("duel", "act2_ashen_throne", SceneAtmosphere.DUNGEON, ParticleType.ASH)
        else -> SceneVisual("fallback", "fallback_hollow_parchment", SceneAtmosphere.DIALOGUE, ParticleType.DUST)
    }

    fun expressionFor(disposition: Float, archetype: String?): Expression = when {
        disposition > 0.7f && archetype == "ESCALATION" -> Expression.ANGRY
        disposition > 0.7f -> Expression.HAPPY
        disposition < -0.5f -> Expression.ANGRY
        disposition < 0.3f && archetype == "SHIFTING_THE_BURDEN" -> Expression.SAD
        disposition < 0.3f -> Expression.THINKING
        else -> Expression.IDLE
    }
}
```

- [ ] **Step 4: Add background manifest**

Create `app/src/main/assets/visual/scene_background_manifest.json`:
```json
{
  "formatVersion": 1,
  "layerSize": { "width": 1920, "height": 1080 },
  "layerNames": ["back", "mid", "front"],
  "backgroundSets": [
    { "id": "act1_hollow_gate_ruins", "act": 1, "title": "The Hollow Gate Ruins", "sceneIds": ["prologue_scene_1"], "atmosphere": "FOREST", "particleType": "LEAVES" },
    { "id": "act1_outer_ruins_corridors", "act": 1, "title": "Outer Ruins Corridors", "sceneIds": ["outer_ruins_1", "merchants_1", "elena_recruitment"], "atmosphere": "DIALOGUE", "particleType": "DUST" },
    { "id": "act1_cracked_watchtower", "act": 1, "title": "The Cracked Watchtower", "sceneIds": ["watchtower_1"], "atmosphere": "WORLD_MAP", "particleType": "NONE" },
    { "id": "act1_deep_hollow_throne", "act": 1, "title": "The Deep Hollow Throne", "sceneIds": ["deep_hollow_1", "hollow_approach"], "atmosphere": "DUNGEON", "particleType": "ASH" },
    { "id": "act1_deserter_camp", "act": 1, "title": "The Deserter's Camp", "sceneIds": ["thorne_encounter"], "atmosphere": "CAMP", "particleType": "FIREFLY" },
    { "id": "act1_broken_shrine", "act": 1, "title": "The Broken Shrine", "sceneIds": ["vessa_shrine"], "atmosphere": "DUNGEON", "particleType": "ASH" },
    { "id": "act1_hidden_warden_chamber", "act": 1, "title": "The Warden's Hidden Chamber", "sceneIds": ["warden_betrayal"], "atmosphere": "CAVE", "particleType": "MIST" },
    { "id": "act1_camp_site", "act": 1, "title": "Safe Camp Site", "sceneIds": ["camp"], "atmosphere": "CAMP", "particleType": "FIREFLY" },
    { "id": "act2_ashen_gate_forge", "act": 2, "title": "The Ashen Gate Forge", "sceneIds": ["ashen_gate"], "atmosphere": "DUNGEON", "particleType": "EMBER" },
    { "id": "act2_cinder_market", "act": 2, "title": "The Cinder Market", "sceneIds": ["ash_market"], "atmosphere": "WORLD_MAP", "particleType": "ASH" },
    { "id": "act2_memorial_field", "act": 2, "title": "The Field of Names", "sceneIds": ["memorial_field"], "atmosphere": "WORLD_MAP", "particleType": "ASH" },
    { "id": "act2_ember_sanctum", "act": 2, "title": "The Ember Sanctum", "sceneIds": ["ember_sanctum"], "atmosphere": "CAVE", "particleType": "EMBER" },
    { "id": "act2_reforged_enclave", "act": 2, "title": "The Reforged Enclave", "sceneIds": ["reforged_camp", "seren_alliance"], "atmosphere": "CAMP", "particleType": "EMBER" },
    { "id": "act2_ruined_library", "act": 2, "title": "The Ruined Library", "sceneIds": ["aria_confession"], "atmosphere": "DIALOGUE", "particleType": "DUST" },
    { "id": "act2_reality_bending_forge", "act": 2, "title": "Reality-Bending Forge", "sceneIds": ["kael_loyalty"], "atmosphere": "DUNGEON", "particleType": "EMBER" },
    { "id": "act2_ashen_throne", "act": 2, "title": "The Ashen Throne", "sceneIds": ["echo_confrontation", "act2_climax"], "atmosphere": "DUNGEON", "particleType": "ASH" },
    { "id": "act3_broken_shore_shipwreck", "act": 3, "title": "Broken Shore and Shipwrecks", "sceneIds": ["coastal_arrival", "salvage_yard", "smugglers_cove"], "atmosphere": "WORLD_MAP", "particleType": "MIST" },
    { "id": "act3_drowned_temple_tide_pools", "act": 3, "title": "Drowned Temple and Tide Pools", "sceneIds": ["drowned_temple", "dara_ritual"], "atmosphere": "CAVE", "particleType": "MIST" },
    { "id": "act3_tidewall_caves_lab", "act": 3, "title": "Tidewall Caves and Laboratory", "sceneIds": ["tidewall_garrison", "aria_laboratory"], "atmosphere": "CAVE", "particleType": "MIST" },
    { "id": "act3_reforged_fleet_undersea_throne", "act": 3, "title": "Fleet and Undersea Throne", "sceneIds": ["rook_betrayal", "seren_fleet", "act3_climax"], "atmosphere": "DUNGEON", "particleType": "MIST" }
  ]
}
```

- [ ] **Step 5: Add artist-facing Phase 2 spec**

Create `docs/art/visual-upgrade-phase-2.md`:
```markdown
# Chimera Visual Upgrade Phase 2 Artist Spec

## Style

2D illustrated matte paintings with depth layers. Painterly, slightly stylized, atmospheric, and readable on phone screens. Avoid photorealism. Keep value contrast strong enough for overlaid dialogue and navigation chrome.

## Scene Backgrounds

Deliver 20 background sets. Each set contains:

- `<background_id>_back.png`
- `<background_id>_mid.png`
- `<background_id>_front.png`

Each PNG is 1920 x 1080, transparent where needed, sRGB, under 2 MB after optimization.

The complete background ID list is the `backgroundSets[].id` list in `app/src/main/assets/visual/scene_background_manifest.json`.

## Character Expressions

Deliver 72 character PNGs: 12 NPCs x 6 expressions. Each PNG is 512 x 512, transparent background, consistent lighting, bust framing.

NPC IDs:

- aria
- corruption
- dara
- elena
- hollow_king
- kael
- marcus
- rook
- seren
- thorne
- vessa
- warden

Expressions:

- idle
- happy
- angry
- sad
- surprised
- thinking

Path format:

```text
app/src/main/res/drawable-nodpi/npc_<npc_id>_<expression>.png
```

Example:

```text
app/src/main/res/drawable-nodpi/npc_elena_idle.png
app/src/main/res/drawable-nodpi/npc_elena_happy.png
app/src/main/res/drawable-nodpi/npc_elena_angry.png
app/src/main/res/drawable-nodpi/npc_elena_sad.png
app/src/main/res/drawable-nodpi/npc_elena_surprised.png
app/src/main/res/drawable-nodpi/npc_elena_thinking.png
```

## Budget

- Scene backgrounds: 20 sets x 3 layers = 60 PNGs, target $4,000 to $6,000.
- Character expressions: 72 PNGs, target $2,000 to $4,000.
- Illustrated map node icons: 25 PNGs, target $500 to $800.
- Total art budget target: $6,500 to $10,800.
```

- [ ] **Step 6: Run mapping tests**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.assets.SceneVisualsTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

Run:
```bash
git add core-ui/src/main/kotlin/com/chimera/ui/assets core-ui/src/test/kotlin/com/chimera/ui/assets app/src/main/assets/visual docs/art/visual-upgrade-phase-2.md
git commit -m "feat: define scene visual asset manifest

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 7: Parallax Background and NPC Expression Rendering

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/ParallaxBackground.kt`
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/components/NpcPortrait.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/components/NpcPortraitTest.kt`

- [ ] **Step 1: Extend portrait tests for expression resource naming**

Add to `core-ui/src/test/kotlin/com/chimera/ui/components/NpcPortraitTest.kt`:
```kotlin
@Test
fun `expression portrait resource names are deterministic`() {
    assertEquals("npc_elena_happy", npcExpressionResourceName("elena", "happy"))
    assertEquals("npc_hollow_king_thinking", npcExpressionResourceName("hollow_king", "thinking"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.components.NpcPortraitTest"
```

Expected: compile failure because `npcExpressionResourceName` does not exist.

- [ ] **Step 3: Implement parallax background**

Create `core-ui/src/main/kotlin/com/chimera/ui/components/ParallaxBackground.kt`:
```kotlin
package com.chimera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chimera.ui.assets.SceneVisual
import com.chimera.ui.theme.AtmosphereTokens

@Composable
fun ParallaxBackground(
    visual: SceneVisual,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = AtmosphereTokens.paletteFor(visual.atmosphere)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.background, palette.surface)))
    ) {
        listOf("back", "mid", "front").forEach { layer ->
            val name = "${visual.backgroundSet}_$layer"
            val drawableId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (drawableId != 0) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(drawableId).crossfade(250).build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
```

- [ ] **Step 4: Extend `NpcPortrait` for expressions**

Modify `NpcPortrait.kt`:
```kotlin
internal fun npcExpressionResourceName(npcId: String, expression: String): String {
    val safeNpc = npcId.lowercase().replace(Regex("[^a-z0-9_]+"), "_").trim('_')
    val safeExpression = expression.lowercase().replace(Regex("[^a-z0-9_]+"), "_").trim('_')
    return "npc_${safeNpc}_${safeExpression}"
}
```

Then add an optional parameter to `NpcPortrait`:
```kotlin
expression: String? = null,
```

Update portrait resolution inside the composable so `expression` wins over `portraitResName`:
```kotlin
val resolvedPortraitResName = expression?.let { npcExpressionResourceName(npcId, it) } ?: portraitResName
if (resolvedPortraitResName != null) {
    val context = LocalContext.current
    val drawableId = context.resources.getIdentifier(
        resolvedPortraitResName,
        "drawable",
        context.packageName
    )
    if (drawableId != 0) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(drawableId)
                .crossfade(300)
                .build(),
            contentDescription = contentDescription ?: npcName,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.Medium,
            modifier = Modifier.matchParentSize()
        )
    }
}
```

- [ ] **Step 5: Run portrait tests**

Run:
```bash
./gradlew :core-ui:test --tests "com.chimera.ui.components.NpcPortraitTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

Run:
```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/ParallaxBackground.kt core-ui/src/main/kotlin/com/chimera/ui/components/NpcPortrait.kt core-ui/src/test/kotlin/com/chimera/ui/components/NpcPortraitTest.kt
git commit -m "feat: add parallax backgrounds and expression portraits

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 8: App-Wide Atmosphere Scaffold Integration

**Files:**
- Modify: `app/src/main/kotlin/com/chimera/ui/MainActivity.kt`
- Modify: `app/src/main/kotlin/com/chimera/ui/navigation/ChimeraBottomBar.kt`
- Modify: `feature-home/src/main/kotlin/com/chimera/feature/home/HomeScreen.kt`
- Modify: `feature-map/src/main/kotlin/com/chimera/feature/map/MapScreen.kt`
- Modify: `feature-dialogue/src/main/kotlin/com/chimera/feature/dialogue/DialogueSceneScreen.kt`
- Modify: `feature-camp/src/main/kotlin/com/chimera/feature/camp/CampScreen.kt`
- Modify: `feature-journal/src/main/kotlin/com/chimera/feature/journal/JournalScreen.kt`
- Modify: `app/src/main/kotlin/com/chimera/ui/screens/duel/DuelScreen.kt`

- [ ] **Step 1: Make `MainActivity` derive the atmosphere from route**

In `MainActivity.kt`, remove the outer `Surface` and `Scaffold`, import `SceneVisuals`, and wrap the nav content with `AtmosphereScaffold`:
```kotlin
val route = currentDestination?.route
val visual = com.chimera.ui.assets.SceneVisuals.forRoute(route)
AtmosphereScaffold(
    atmosphere = visual.atmosphere,
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
    }
) {
    ChimeraNavHost(
        navController = navController,
        preferences = preferences,
        modifier = Modifier
    )
}
```

- [ ] **Step 2: Apply themed bottom bar**

In `ChimeraBottomBar.kt`, keep Material 3 `NavigationBar`, but use current palette:
```kotlin
NavigationBar(
    modifier = modifier,
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
    contentColor = MaterialTheme.colorScheme.onSurface,
    tonalElevation = 8.dp
)
```

- [ ] **Step 3: Add visual layers to dialogue**

In `DialogueSceneScreen.kt`, derive the visual at the top:
```kotlin
val visual = com.chimera.ui.assets.SceneVisuals.forScene(sceneId)
val expression = com.chimera.ui.assets.SceneVisuals.expressionFor(
    disposition = uiState.npcDisposition,
    archetype = uiState.npcArchetype
)
```

Add background and particles behind the existing `Column`:
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    com.chimera.ui.components.ParallaxBackground(
        visual = visual,
        modifier = Modifier.matchParentSize()
    )
    com.chimera.ui.components.ParticleOverlay(
        type = visual.particleType,
        modifier = Modifier.matchParentSize()
    )
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Existing dialogue content remains here.
    }
}
```

Pass expression into the portrait:
```kotlin
expression = expression.name.lowercase()
```

- [ ] **Step 4: Apply atmosphere wrappers to top-level screens**

For Home, Map, Camp, Journal, and Duel, preserve current state collection and layout. Replace only flat full-screen backgrounds and selected `Card` or `Button` calls with shared primitives:
```kotlin
AtmosphereCard(modifier = Modifier.fillMaxWidth()) {
    // existing card content
}
```

```kotlin
AtmosphereButton(onClick = onEnterScene) {
    Text("Enter Scene", style = MaterialTheme.typography.labelLarge)
}
```

- [ ] **Step 5: Add screen-specific particles**

Add particles with these mappings:
```kotlin
ParticleOverlay(type = ParticleType.LEAVES) // Home
ParticleOverlay(type = ParticleType.NONE)   // Map
ParticleOverlay(type = ParticleType.FIREFLY) // Camp
ParticleOverlay(type = ParticleType.DUST)   // Journal
ParticleOverlay(type = ParticleType.ASH)    // Duel
```

- [ ] **Step 6: Run compile**

Run:
```bash
./gradlew assembleMockDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

Run:
```bash
git add app/src/main/kotlin/com/chimera/ui feature-home feature-map feature-dialogue feature-camp feature-journal app/src/main/kotlin/com/chimera/ui/screens/duel
git commit -m "feat: integrate atmosphere visuals across screens

Co-authored-by: Codex <noreply@openai.com>"
```

## Task 9: Documentation and Final Verification

**Files:**
- Modify: `README.md`
- Modify: `CLAUDE.md`
- Modify: `AGENTS.md`

- [ ] **Step 1: Update README visual section**

Add a short section to `README.md` after `## Architecture`:
```markdown
## Visual System

The app uses a shared Compose visual system in `core-ui`:

- `AtmosphereTheme` and `AtmosphereScaffold` provide scene-context palettes for forest, cave, dungeon, camp, world map, and dialogue.
- `ChimeraTypography` embeds Cinzel, Source Sans 3, and JetBrains Mono from Google Fonts.
- `ChimeraIcons` provides custom 24 dp fantasy line-art icons instead of Material system icons.
- `SceneVisuals` maps checked-in scene IDs to atmosphere, particle type, and future background asset IDs.
```

- [ ] **Step 2: Update agent instructions**

In both `CLAUDE.md` and `AGENTS.md`, add this under Architecture direction:
```markdown
- `core-ui/` owns the visual design system: atmosphere tokens, typography, icons, overlays, and reusable Compose visual primitives.
- Feature modules should consume `core-ui` visual primitives instead of defining independent palettes or importing Material system icons directly.
```

- [ ] **Step 3: Run focused tests**

Run:
```bash
./gradlew :core-ui:test
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Run app unit tests**

Run:
```bash
./gradlew testMockDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Run debug build**

Run:
```bash
./gradlew assembleMockDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Run static analysis**

Run:
```bash
./gradlew detekt
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit documentation**

Run:
```bash
git add README.md CLAUDE.md AGENTS.md
git commit -m "docs: document chimera visual system

Co-authored-by: Codex <noreply@openai.com>"
```

## Subagent Execution Order

Use one implementer subagent at a time, with spec review followed by code-quality review after each task.

1. Task 1: baseline verification.
2. Task 2: atmosphere tokens and theme primitives.
3. Task 3: typography and fonts.
4. Task 4: custom icons.
5. Task 5: grain, vignette, and particles.
6. Task 6: Phase 2 visual manifest and artist spec.
7. Task 7: parallax and expressions.
8. Task 8: screen integration.
9. Task 9: docs and verification.

Reviewer checks after every task:

- Spec compliance: confirm the task implemented exactly the requested files, APIs, and behavior.
- Code quality: confirm Compose code is stateless where practical, accessibility labels remain present, no feature module imports another feature module, and no `chimera-core` Android dependency is introduced.

## Acceptance Criteria

- `AtmosphereTheme`, `AtmosphereScaffold`, `AtmosphereSurface`, `AtmosphereCard`, and `AtmosphereButton` exist in `core-ui`.
- `SceneAtmosphere` supports `FOREST`, `CAVE`, `DUNGEON`, `CAMP`, `WORLD_MAP`, and `DIALOGUE`.
- Typography uses embedded Cinzel, Source Sans 3, and JetBrains Mono fonts from `core-ui/src/main/res/font/`.
- `ChimeraIcons` exposes at least 20 custom 24 dp line-art icons, including all requested map, scene, memory, dialogue, vow, and duel icons.
- Grain, vignette, and six particle types exist and can be layered behind any screen.
- All 30 checked-in scene IDs map to one of 20 background sets.
- Artist spec documents the exact background and NPC expression asset naming contract.
- Existing screens compile after consuming the new shared visual primitives.
- Final verification commands pass from repository root: `./gradlew :core-ui:test`, `./gradlew testMockDebugUnitTest`, `./gradlew assembleMockDebug`, and `./gradlew detekt`.
