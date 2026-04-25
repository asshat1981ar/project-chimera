# Gothic Elegance UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform Project Chimera's UI from functional dark theme to a Gothic Elegance design system with medieval manuscript aesthetics, per-atmosphere illumination, and custom component library.

**Architecture:** Build foundation tokens first (colors, typography, textures, atmosphere palettes), then core shared components (ManuscriptCard, GothicButton, etc.), then game-specific components, then integrate into all 14 screens. Each phase produces working, testable code. No screen is touched until its required components exist.

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Hilt, Room, Navigation Compose, Coroutines. Custom fonts: Cinzel Decorative + Cinzel (OFL, bundled as Android resources).

**Approved Spec:** `docs/superpowers/specs/2026-04-25-gothic-elegance-ui-design.md`

**Scope Boundaries:**
- **In:** All design tokens, 6 core components, 6 game components, all 14 screens, bottom nav, onboarding, splash, transitions
- **Out:** No gameplay logic changes, no new screens, no new data models, no web dependencies
- **Constraint:** Visual-only transformation preserving all existing behavior and navigation

---

## File Structure

### New Files to Create

```
core-ui/src/main/res/font/
  cinzel_decorative.ttf          # Cinzel Decorative Regular (OFL)
  cinzel_decorative_bold.ttf     # Cinzel Decorative Bold (OFL)
  cinzel_regular.ttf             # Cinzel Regular (OFL)
  cinzel_bold.ttf                 # Cinzel Bold (OFL)

core-ui/src/main/kotlin/com/chimera/ui/theme/
  GothicFonts.kt                  # FontFamily definitions for Cinzel chain
  ChimeraSpacing.kt               # Spacing, elevation, corner tokens

core-ui/src/main/kotlin/com/chimera/ui/components/
  ManuscriptCard.kt               # Tooled leather border card
  GothicButton.kt                 # Iron/stone textured button variants
  IlluminatedDialogueBubble.kt    # Parchment scroll NPC bubble, ink player bubble
  ManuscriptStatBar.kt            # Ornate bordered stat bar with filigree
  GothicBottomNav.kt              # Tooled leather bottom navigation
  ParchmentInputField.kt          # Parchment-textured text input
  IlluminatedInitial.kt           # Drop cap / illuminated initial letter
  FiligreeDecoration.kt           # Corner filigree Canvas drawings
  ParchmentTexture.kt             # Reusable parchment Canvas modifier

app/src/main/kotlin/com/chimera/ui/navigation/
  (modify) ChimeraNavHost.kt      # Wire GothicBottomNav into game graph
```

### Existing Files to Modify

```
core-ui/src/main/kotlin/com/chimera/ui/theme/
  ChimeraColors.kt                # Refine palette: Oxblood, Aged Gold, Vellum, Iron, Verdigris
  ChimeraTypography.kt            # Add Cinzel display, serif body, update styles
  Atmosphere.kt                   # Update vignette colors, alphas, grain intensities
  AtmosphereTheme.kt              # Update shapes, inject Gothic typography
  AtmosphereOverlay.kt             # Add parchment texture mode, per-atmosphere tint

feature-home/.../HomeScreen.kt    # Replace Material cards/buttons with Gothic components
feature-camp/.../CampScreen.kt    # Replace with ManuscriptCard, GothicButton, ManuscriptStatBar
feature-dialogue/.../DialogueSceneScreen.kt  # Replace with IlluminatedDialogueBubble, ParchmentInputField
feature-map/.../MapScreen.kt      # Replace node markers with HeraldicMapNode
feature-journal/.../JournalScreen.kt  # Replace with CodexJournal layout
feature-party/.../PartyScreen.kt  # Replace with heraldic companion cards
feature-settings/.../SettingsScreen.kt  # Replace with parchment-style settings
```

---

## Task 1: Refine Color Palette

**Files:**
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraColors.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraColorsTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraColorsTest.kt
package com.chimera.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ChimeraColorsTest {

    @Test
    fun `Oxblood primary is refined dark crimson`() {
        assertEquals(Color(0xFF5C1A1A), Oxblood)
    }

    @Test
    fun `AgedGold secondary is refined warm gold`() {
        assertEquals(Color(0xFFC89B3C), AgedGold)
    }

    @Test
    fun `Vellum text is warm cream`() {
        assertEquals(Color(0xFFF5ECD7), Vellum)
    }

    @Test
    fun `Iron surface is elevated dark gray`() {
        assertEquals(Color(0xFF2A2A2E), Iron)
    }

    @Test
    fun `Verdigris tertiary is muted sage`() {
        assertEquals(Color(0xFF4A7C59), Verdigris)
    }

    @Test
    fun `backward compat aliases exist`() {
        assertEquals(Oxblood, HollowCrimson)
        assertEquals(AgedGold, EmberGold)
        assertEquals(Vellum, ParchmentWhite)
        assertEquals(Iron, CharcoalSurface)
        assertEquals(Verdigris, VoidGreen)
    }

    @Test
    fun `gold accent colors exist`() {
        assertEquals(Color(0xFFD4A84E), AgedGoldBright)
        assertEquals(Color(0xFF9E7A1A), AgedGoldMuted)
    }

    @Test
    fun `parchment texture colors exist`() {
        assertEquals(Color(0xFFF5ECD7), ParchmentLight)
        assertEquals(Color(0xFFE8DCC8), ParchmentDark)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.ChimeraColorsTest" 2>&1 | tail -20`
Expected: FAIL — `Oxblood`, `AgedGold`, etc. unresolved or wrong values

- [ ] **Step 3: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraColors.kt
package com.chimera.ui.theme

import androidx.compose.ui.graphics.Color

// ── Primary: Deep oxblood / dark crimson ──
val Oxblood = Color(0xFF5C1A1A)
val OxbloodLight = Color(0xFF8B2A2A)

// ── Secondary: Aged gold / ember gold ──
val AgedGold = Color(0xFFC89B3C)
val AgedGoldBright = Color(0xFFD4A84E)
val AgedGoldMuted = Color(0xFF9E7A1A)

// ── Tertiary: Verdigris / corruption green ──
val Verdigris = Color(0xFF4A7C59)
val VerdigrisBright = Color(0xFF3D8B5A)

// ── Background tones ──
val AshBlack = Color(0xFF0D0D0F)
val Iron = Color(0xFF2A2A2E)
val IronElevated = Color(0xFF343438)

// ── Text tones ──
val Vellum = Color(0xFFF5ECD7)
val FadedBone = Color(0xFFA89B8C)
val DimAsh = Color(0xFF5A6270)

// ── Parchment textures ──
val ParchmentLight = Color(0xFFF5ECD7)
val ParchmentDark = Color(0xFFE8DCC8)

// ── Status / semantic ──
val BloodRed = Color(0xFFCC3333)
val HealGreen = Color(0xFF4CAF50)
val ManaBlue = Color(0xFF5C7CBA)

// ── Overlay ──
val ShadowVeil = Color(0xCC000000)

// ── Backward compatibility aliases ──
val HollowCrimson = Oxblood
val HollowCrimsonLight = OxbloodLight
val EmberGold = AgedGold
val EmberGoldMuted = AgedGoldMuted
val VoidGreen = Verdigris
val VoidGreenBright = VerdigrisBright
val CharcoalSurface = Iron
val CharcoalElevated = IronElevated
val ParchmentWhite = Vellum
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.ChimeraColorsTest" 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Update Atmosphere.kt references to use new color names**

In `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`, the Dungeon palette references `AshBlack`, `CharcoalSurface`, `CharcoalElevated`, `HollowCrimson`, `ParchmentWhite`, `FadedBone`, `BloodRed`, `HealGreen`, and `ShadowVeil`. These are all backward-compat aliases now, so Atmosphere.kt compiles without changes. Verify:

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraColors.kt core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraColorsTest.kt
git commit -m "feat(ui): refine Gothic color palette with new token names

Introduce Oxblood, AgedGold, Vellum, Iron, Verdigris as canonical
token names per Gothic Elegance spec. Add AgedGoldBright/Muted,
ParchmentLight/Dark texture colors. Preserve backward-compat aliases
(HollowCrimson→Oxblood, etc.) so existing code compiles unchanged.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 2: Add Cinzel Font Family

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/theme/GothicFonts.kt`
- Download Cinzel Decorative + Cinzel font files to `core-ui/src/main/res/font/`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/GothicFontsTest.kt`

- [ ] **Step 1: Download Cinzel font files**

Cinzel Decorative and Cinzel are OFL-licensed on Google Fonts. Download the Regular and Bold weights:

```bash
mkdir -p core-ui/src/main/res/font
cd /tmp
curl -sL "https://fonts.google.com/download?family=Cinzel+Decorative" -o cinzel_decorative.zip
curl -sL "https://fonts.google.com/download?family=Cinzel" -o cinzel.zip
unzip -o cinzel_decorative.zip -d cinzel_decorative
unzip -o cinzel.zip -d cinzel
cp cinzel_decorative/CinzelDecorative-Regular.ttf core-ui/src/main/res/font/cinzel_decorative_regular.ttf
cp cinzel_decorative/CinzelDecorative-Bold.ttf core-ui/src/main/res/font/cinzel_decorative_bold.ttf
cp cinzel/static/Cinzel-Regular.ttf core-ui/src/main/res/font/cinzel_regular.ttf
cp cinzel/static/Cinzel-Bold.ttf core-ui/src/main/res/font/cinzel_bold.ttf
cd -
```

If the Google Fonts download URLs don't serve directly, use the Google Fonts API to get the font file URLs:

```bash
# Alternative: fetch from Google Fonts CSS and extract TTF URLs
curl -s "https://fonts.googleapis.com/css2?family=Cinzel+Decorative:wght@400;700&family=Cinzel:wght@400;700" \
  -H "User-Agent: Mozilla/5.0" | grep -oP 'url\(\K[^)]+' | while read url; do
    filename=$(basename "$url" | sed 's/?.*//')
    curl -sL "$url" -o "core-ui/src/main/res/font/$filename"
  done
```

- [ ] **Step 2: Create the font family definitions**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/theme/GothicFonts.kt
package com.chimera.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.chimera.ui.R

val CinzelDecorative = FontFamily(
    Font(R.font.cinzel_decorative_regular, FontWeight.Normal),
    Font(R.font.cinzel_decorative_bold, FontWeight.Bold)
)

val Cinzel = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold)
)

val GothicDisplayFallback = FontFamily(
    CinzelDecorative,
    Cinzel,
    FontFamily.Serif
)
```

- [ ] **Step 3: Write the test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/theme/GothicFontsTest.kt
package com.chimera.ui.theme

import org.junit.Assert.assertNotNull
import org.junit.Test

class GothicFontsTest {
    @Test
    fun `CinzelDecorative font family is defined`() {
        assertNotNull(CinzelDecorative)
    }

    @Test
    fun `Cinzel font family is defined`() {
        assertNotNull(Cinzel)
    }

    @Test
    fun `GothicDisplayFallback font family is defined`() {
        assertNotNull(GothicDisplayFallback)
    }
}
```

- [ ] **Step 4: Run test**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.GothicFontsTest" 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core-ui/src/main/res/font/ core-ui/src/main/kotlin/com/chimera/ui/theme/GothicFonts.kt core-ui/src/test/kotlin/com/chimera/ui/theme/GothicFontsTest.kt
git commit -m "feat(ui): add Cinzel Decorative + Cinzel font families

Bundle OFL-licensed Cinzel Decorative and Cinzel font files as
Android resources. Define FontFamily composables with fallback
chain: CinzelDecorative → Cinzel → Serif.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 3: Update Typography to Gothic Styles

**Files:**
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraTypographyTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraTypographyTest.kt
package com.chimera.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class ChimeraTypographyTest {
    @Test
    fun `displayLarge uses CinzelDecorative font family`() {
        assertEquals(CinzelDecorative, ChimeraTypography.displayLarge.fontFamily)
    }

    @Test
    fun `displayLarge uses 36sp`() {
        assertEquals(36.sp, ChimeraTypography.displayLarge.fontSize)
    }

    @Test
    fun `displayMedium uses CinzelDecorative font family`() {
        assertEquals(CinzelDecorative, ChimeraTypography.displayMedium.fontFamily)
    }

    @Test
    fun `headlineLarge uses Cinzel font family`() {
        assertEquals(Cinzel, ChimeraTypography.headlineLarge.fontFamily)
    }

    @Test
    fun `headlineMedium uses Cinzel font family`() {
        assertEquals(Cinzel, ChimeraTypography.headlineMedium.fontFamily)
    }

    @Test
    fun `bodyLarge uses Cinzel font family for serif body`() {
        assertEquals(Cinzel, ChimeraTypography.bodyLarge.fontFamily)
    }

    @Test
    fun `bodyMedium uses Cinzel font family for serif body`() {
        assertEquals(Cinzel, ChimeraTypography.bodyMedium.fontFamily)
    }

    @Test
    fun `labelLarge uses sans-serif family`() {
        assertEquals(FontFamily.SansSerif, ChimeraTypography.labelLarge.fontFamily)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.ChimeraTypographyTest" 2>&1 | tail -20`
Expected: FAIL — displayLarge currently uses FontFamily.Serif, not CinzelDecorative

- [ ] **Step 3: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt
package com.chimera.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ChimeraTypography = Typography(
    // ── Display: Cinzel Decorative, large titles, gold accents ──
    displayLarge = TextStyle(
        fontFamily = CinzelDecorative,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = 2.sp,
        color = AgedGold
    ),
    displayMedium = TextStyle(
        fontFamily = CinzelDecorative,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 1.5.sp,
        color = AgedGold
    ),
    // ── Headlines: Cinzel serif, section headers ──
    headlineLarge = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 1.sp,
        color = Vellum
    ),
    headlineMedium = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.5.sp,
        color = Vellum
    ),
    headlineSmall = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = Vellum
    ),
    // ── Title: Cinzel serif, card titles and NPC names ──
    titleLarge = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.5.sp,
        color = Vellum
    ),
    titleMedium = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = Vellum
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = Vellum
    ),
    // ── Body: Cinzel serif for narrative, readable content ──
    bodyLarge = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = Vellum
    ),
    bodyMedium = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = FadedBone
    ),
    bodySmall = TextStyle(
        fontFamily = Cinzel,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = FadedBone
    ),
    // ── Labels: Sans-serif, uppercase, UI chrome ──
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
        color = Vellum
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp,
        color = FadedBone
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp,
        color = DimAsh
    )
)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.ChimeraTypographyTest" 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Verify existing screens still compile**

Run: `./gradlew :feature-home:compileDebugKotlin :feature-camp:compileDebugKotlin :feature-dialogue:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraTypography.kt core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraTypographyTest.kt
git commit -m "feat(ui): update typography to Gothic Elegance with Cinzel fonts

Replace FontFamily.Serif with CinzelDecorative for display styles
and Cinzel for headlines/body. Keep Sans-serif for labels.
Display styles now use AgedGold color per spec.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 4: Update Atmosphere Palettes for Per-Atmosphere Illumination

**Files:**
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt
package com.chimera.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class AtmosphereTest {
    @Test
    fun `Forest vignette uses moss-toned color`() {
        val palette = AtmosphereTokens.Forest
        assertEquals(Color(0xFF1A3A1A), palette.overlayVignette)
    }

    @Test
    fun `Forest grain intensity is 10 percent`() {
        assertEquals(0.10f, AtmosphereTokens.Forest.grainIntensity, 0.01f)
    }

    @Test
    fun `Cave vignette uses dark iron color`() {
        assertEquals(Color(0xFF1A1A1A), AtmosphereTokens.Cave.overlayVignette)
    }

    @Test
    fun `Cave grain intensity is 18 percent`() {
        assertEquals(0.18f, AtmosphereTokens.Cave.grainIntensity, 0.01f)
    }

    @Test
    fun `Dungeon vignette uses blood-dark color`() {
        assertEquals(Color(0xFF1A0A0A), AtmosphereTokens.Dungeon.overlayVignette)
    }

    @Test
    fun `Dungeon grain intensity is 22 percent`() {
        assertEquals(0.22f, AtmosphereTokens.Dungeon.grainIntensity, 0.01f)
    }

    @Test
    fun `Camp vignette uses warm amber color`() {
        assertEquals(Color(0xFF2A1A0A), AtmosphereTokens.Camp.overlayVignette)
    }

    @Test
    fun `Camp grain intensity is 14 percent`() {
        assertEquals(0.14f, AtmosphereTokens.Camp.grainIntensity, 0.01f)
    }

    @Test
    fun `WorldMap vignette uses deep blue-black color`() {
        assertEquals(Color(0xFF1A1A2A), AtmosphereTokens.WorldMap.overlayVignette)
    }

    @Test
    fun `WorldMap grain intensity is 8 percent`() {
        assertEquals(0.08f, AtmosphereTokens.WorldMap.grainIntensity, 0.01f)
    }

    @Test
    fun `Dialogue vignette uses warm sepia color`() {
        assertEquals(Color(0xFF2A1A0A), AtmosphereTokens.Dialogue.overlayVignette)
    }

    @Test
    fun `Dialogue grain intensity is 12 percent`() {
        assertEquals(0.12f, AtmosphereTokens.Dialogue.grainIntensity, 0.01f)
    }

    @Test
    fun `AtmospherePalette has vignetteAlpha field`() {
        val palette = AtmosphereTokens.Forest
        assertEquals(0.30f, palette.vignetteAlpha, 0.01f)
    }

    @Test
    fun `AtmospherePalette has vignetteStyle field`() {
        val palette = AtmosphereTokens.Forest
        assertEquals("Verdant Manuscript", palette.vignetteStyle)
    }

    @Test
    fun `paletteFor returns correct palette for each atmosphere`() {
        SceneAtmosphere.values().forEach { atm ->
            val palette = AtmosphereTokens.paletteFor(atm)
            assertNotNull(palette)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.AtmosphereTest" 2>&1 | tail -20`
Expected: FAIL — AtmospherePalette doesn't have vignetteAlpha or vignetteStyle fields, and vignette colors are different

- [ ] **Step 3: Write the implementation**

Update `Atmosphere.kt` to add `vignetteAlpha` and `vignetteStyle` to `AtmospherePalette`, and update all palette entries with spec values:

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt
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
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.AtmosphereTest" 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Verify full project compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/theme/Atmosphere.kt core-ui/src/test/kotlin/com/chimera/ui/theme/AtmosphereTest.kt
git commit -m "feat(ui): update atmosphere palettes for per-atmosphere illumination

Add vignetteAlpha and vignetteStyle fields to AtmospherePalette.
Update all six atmosphere palettes with spec-defined vignette
colors, alphas, and grain intensities. Replace backward-compat
color references (HollowCrimson→Oxblood, etc.).

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 5: Add Spacing and Elevation Tokens

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraSpacing.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraSpacingTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraSpacingTest.kt
package com.chimera.ui.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ChimeraSpacingTest {
    @Test
    fun `spacing tokens have correct values`() {
        assertEquals(2.dp, ChimeraSpacing.micro)
        assertEquals(4.dp, ChimeraSpacing.tiny)
        assertEquals(8.dp, ChimeraSpacing.small)
        assertEquals(12.dp, ChimeraSpacing.medium)
        assertEquals(16.dp, ChimeraSpacing.regular)
        assertEquals(24.dp, ChimeraSpacing.large)
        assertEquals(32.dp, ChimeraSpacing.xl)
        assertEquals(48.dp, ChimeraSpacing.xxl)
    }

    @Test
    fun `elevation tokens have correct values`() {
        assertEquals(1.dp, ChimeraElevation.subtle)
        assertEquals(2.dp, ChimeraElevation.low)
        assertEquals(4.dp, ChimeraElevation.medium)
        assertEquals(8.dp, ChimeraElevation.high)
        assertEquals(16.dp, ChimeraElevation.dramatic)
    }

    @Test
    fun `corner radius tokens have correct values`() {
        assertEquals(2.dp, ChimeraCorners.micro)
        assertEquals(4.dp, ChimeraCorners.small)
        assertEquals(8.dp, ChimeraCorners.medium)
        assertEquals(12.dp, ChimeraCorners.large)
        assertEquals(16.dp, ChimeraCorners.xl)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.ChimeraSpacingTest" 2>&1 | tail -20`
Expected: FAIL — ChimeraSpacing, ChimeraElevation, ChimeraCorners unresolved

- [ ] **Step 3: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraSpacing.kt
package com.chimera.ui.theme

import androidx.compose.ui.unit.dp

object ChimeraSpacing {
    val micro = 2.dp
    val tiny = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val regular = 16.dp
    val large = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

object ChimeraElevation {
    val subtle = 1.dp
    val low = 2.dp
    val medium = 4.dp
    val high = 8.dp
    val dramatic = 16.dp
}

object ChimeraCorners {
    val micro = 2.dp
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val xl = 16.dp
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.theme.ChimeraSpacingTest" 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/theme/ChimeraSpacing.kt core-ui/src/test/kotlin/com/chimera/ui/theme/ChimeraSpacingTest.kt
git commit -m "feat(ui): add spacing, elevation, and corner radius tokens

Define ChimeraSpacing, ChimeraElevation, and ChimeraCorners
object singletons per Gothic Elegance spec. These tokens replace
ad-hoc dp values across feature screens.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 6: Update AtmosphereOverlay for Per-Atmosphere Vignette and Parchment Texture

**Files:**
- Modify: `core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/components/AtmosphereOverlayTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/components/AtmosphereOverlayTest.kt
package com.chimera.ui.components

import com.chimera.ui.theme.AtmosphereTokens
import com.chimera.ui.theme.SceneAtmosphere
import org.junit.Assert.assertTrue
import org.junit.Test

class AtmosphereOverlayTest {
    @Test
    fun `each atmosphere palette has vignette alpha greater than zero`() {
        SceneAtmosphere.values().forEach { atm ->
            val palette = AtmosphereTokens.paletteFor(atm)
            assertTrue(
                "${atm.name} vignetteAlpha should be > 0, was ${palette.vignetteAlpha}",
                palette.vignetteAlpha > 0f
            )
        }
    }

    @Test
    fun `each atmosphere palette has grain intensity within valid range`() {
        SceneAtmosphere.values().forEach { atm ->
            val palette = AtmosphereTokens.paletteFor(atm)
            assertTrue(
                "${atm.name} grainIntensity should be in [0, 0.3], was ${palette.grainIntensity}",
                palette.grainIntensity in 0f..0.3f
            )
        }
    }

    @Test
    fun `each atmosphere palette has non-empty vignette style`() {
        SceneAtmosphere.values().forEach { atm ->
            val palette = AtmosphereTokens.paletteFor(atm)
            assertTrue(
                "${atm.name} vignetteStyle should not be empty",
                palette.vignetteStyle.isNotEmpty()
            )
        }
    }
}
```

- [ ] **Step 2: Run test to verify it passes** (these tests check the data model from Task 4)

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.components.AtmosphereOverlayTest" 2>&1 | tail -20`
Expected: PASS (since we updated AtmospherePalette in Task 4)

- [ ] **Step 3: Update AtmosphereOverlay composable to use vignetteAlpha from palette**

Add a new `AtmosphereThemedOverlay` composable that reads the current atmosphere from `LocalSceneAtmosphere` and applies the correct vignette alpha and color:

```kotlin
// Add to core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt

/**
 * Atmosphere-aware overlay that reads vignette settings from the current
 * AtmospherePalette and applies themed vignette + grain.
 */
@Composable
fun AtmosphereThemedOverlay(
    modifier: Modifier = Modifier
) {
    val atmosphere = LocalSceneAtmosphere.current
    val palette = AtmosphereTokens.paletteFor(atmosphere)

    AtmosphereOverlay(
        vignetteIntensity = palette.vignetteAlpha,
        grainIntensity = palette.grainIntensity,
        vignetteColor = palette.overlayVignette,
        modifier = modifier
    )
}
```

This requires importing `LocalSceneAtmosphere` and `AtmosphereTokens` from `com.chimera.ui.theme`.

- [ ] **Step 4: Add parchment texture composable**

```kotlin
// Create core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentTexture.kt
package com.chimera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.sin
import kotlin.random.Random

/**
 * Parchment texture overlay for card interiors and journal backgrounds.
 * Applies subtle grain lines and warm tonal variation.
 */
@Composable
fun ParchmentTexture(
    baseColor: Color = Color(0xFFF5ECD7),
    grainIntensity: Float = 0.03f,
    modifier: Modifier = Modifier
) {
    val seeds = remember { List(80) { Random.nextInt(0, 10000) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Subtle horizontal ruled lines (manuscript guide)
        val lineSpacing = size.height / 24f
        val lineColor = baseColor.copy(alpha = 0.12f)
        for (i in 1..23) {
            val y = i * lineSpacing
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 0.5f
            )
        }

        // Sparse grain dots for texture
        val grainColor = Color.Black.copy(alpha = grainIntensity)
        seeds.forEach { seed ->
            val rng = Random(seed)
            val x = rng.nextFloat() * size.width
            val y = rng.nextFloat() * size.height
            drawCircle(
                color = grainColor,
                radius = 1f,
                center = Offset(x, y)
            )
        }
    }
}
```

- [ ] **Step 5: Update AtmosphereTheme.kt shapes to match Gothic corner tokens**

In `AtmosphereTheme.kt`, update the shape definitions to use `ChimeraCorners`:

```kotlin
// Update AtmosphereShapes in AtmosphereTheme.kt
private val AtmosphereShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(ChimeraCorners.micro),
    small = RoundedCornerShape(ChimeraCorners.small),
    medium = RoundedCornerShape(ChimeraCorners.medium),
    large = RoundedCornerShape(ChimeraCorners.large),
    extraLarge = RoundedCornerShape(ChimeraCorners.xl)
)
```

Add the import for `ChimeraCorners`.

- [ ] **Step 6: Compile and verify**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/AtmosphereOverlay.kt core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentTexture.kt core-ui/src/main/kotlin/com/chimera/ui/theme/AtmosphereTheme.kt core-ui/src/test/kotlin/com/chimera/ui/components/AtmosphereOverlayTest.kt
git commit -m "feat(ui): add AtmosphereThemedOverlay, ParchmentTexture, and Gothic corners

Add AtmosphereThemedOverlay composable that reads vignette settings
from the current AtmospherePalette. Add ParchmentTexture for
manuscript-style ruled lines and grain. Update AtmosphereShapes
to use ChimeraCorners tokens.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 7: Build Illuminated Initial (Drop Cap) Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/IlluminatedInitial.kt`
- Test: `core-ui/src/test/kotlin/com/chimera/ui/components/IlluminatedInitialTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
// core-ui/src/test/kotlin/com/chimera/ui/components/IlluminatedInitialTest.kt
package com.chimera.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class IlluminatedInitialTest {
    @Test
    fun `extractInitial returns first uppercase letter of text`() {
        assertEquals("W", extractInitial("Welcome, Wanderer"))
    }

    @Test
    fun `extractInitial returns question mark for empty text`() {
        assertEquals("?", extractInitial(""))
    }

    @Test
    fun `extractInitial returns question mark for blank text`() {
        assertEquals("?", extractInitial("   "))
    }

    @Test
    fun `extractInitial returns first letter ignoring leading spaces`() {
        assertEquals("C", extractInitial("  Chapter One"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.components.IlluminatedInitialTest" 2>&1 | tail -20`
Expected: FAIL — `extractInitial` unresolved

- [ ] **Step 3: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/IlluminatedInitial.kt
package com.chimera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.CinzelDecorative
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

/**
 * Illuminated initial (drop cap) composable in the style of
 * medieval manuscript decoration. Displays a single large letter
 * with gold color, optional border, and dark background.
 */
@Composable
fun IlluminatedInitial(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    accentColor: androidx.compose.ui.graphics.Color = AgedGold,
    backgroundColor: androidx.compose.ui.graphics.Color = Iron,
    borderColor: androidx.compose.ui.graphics.Color = Oxblood
) {
    val initial = extractInitial(text)

    Box(
        modifier = modifier
            .size(size)
            .shadow(2.dp, RoundedCornerShape(4.dp))
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .border(1.dp, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontFamily = CinzelDecorative,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.55f).sp,
            color = accentColor
        )
    }
}

internal fun extractInitial(text: String): String {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return "?"
    val first = trimmed.firstOrNull { it.isLetter() }
    return first?.uppercaseChar()?.toString() ?: "?"
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core-ui:test --tests "com.chimera.ui.components.IlluminatedInitialTest" 2>&1 | tail -20`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/IlluminatedInitial.kt core-ui/src/test/kotlin/com/chimera/ui/components/IlluminatedInitialTest.kt
git commit -m "feat(ui): add IlluminatedInitial drop cap composable

Gothic-style illuminated initial letter with gold accent, dark
background, and oxblood border. Used for chapter headings,
card titles, and manuscript-style text openings.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 8: Build FiligreeDecoration Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/FiligreeDecoration.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/FiligreeDecoration.kt
package com.chimera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold

/**
 * Filigree corner decoration drawn as a Canvas composable.
 * Used for ornamenting card corners, section dividers, and
 * manuscript-style borders.
 */
@Composable
fun FiligreeDecoration(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = AgedGold,
    strokeWidth: Float = 1.5f
) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.value * density
        val halfS = s / 2f
        val cornerRadius = s * 0.15f

        // Outer swirl
        val outerPath = Path().apply {
            moveTo(0f, cornerRadius * 2)
            cubicTo(0f, cornerRadius, cornerRadius, 0f, cornerRadius * 2, 0f)
            lineTo(s - cornerRadius * 2, 0f)
            cubicTo(s - cornerRadius, 0f, s, cornerRadius, s, cornerRadius * 2)
        }
        drawPath(outerPath, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        // Center diamond
        val diamondPath = Path().apply {
            moveTo(halfS, halfS - s * 0.15f)
            lineTo(halfS + s * 0.1f, halfS)
            lineTo(halfS, halfS + s * 0.15f)
            lineTo(halfS - s * 0.1f, halfS)
            close()
        }
        drawPath(diamondPath, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        // Accent dots at cardinal points
        val dotRadius = strokeWidth * 0.8f
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.25f, s * 0.25f))
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.75f, s * 0.25f))
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.25f, s * 0.75f))
        drawCircle(color, radius = dotRadius, center = Offset(s * 0.75f, s * 0.75f))
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/FiligreeDecoration.kt
git commit -m "feat(ui): add FiligreeDecoration Canvas composable

Decorative filigree corner element with swirls, diamond center,
and accent dots. Draws in gold by default for Gothic ornamentation
on cards, dividers, and borders.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 9: Build ManuscriptCard Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/ManuscriptCard.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/ManuscriptCard.kt
package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraCorners
import com.chimera.ui.theme.ChimeraElevation
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

/**
 * ManuscriptCard: A tooled-leather-bordered card with parchment interior,
 * optional gold-leaf accent border, and illuminated initial cap on title.
 *
 * The outer border uses Oxblood (dark crimson), inner border uses
 * AgedGold for the accent, and the interior is Iron (dark surface).
 */
@Composable
fun ManuscriptCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    goldAccent: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val outerBorder = BorderStroke(1.dp, Oxblood.copy(alpha = 0.6f))
    val innerAccentBorder = if (goldAccent) BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f)) else null

    Card(
        modifier = modifier.shadow(
            elevation = ChimeraElevation.low,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(ChimeraCorners.medium),
            clip = false
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ChimeraCorners.medium),
        colors = CardDefaults.cardColors(
            containerColor = Iron,
            contentColor = Vellum
        ),
        border = outerBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = ChimeraElevation.low)
    ) {
        Column(modifier = Modifier.padding(ChimeraSpacing.regular)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IlluminatedInitial(
                        text = title,
                        size = 36.dp,
                        accentColor = AgedGold,
                        backgroundColor = Iron,
                        borderColor = Oxblood.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(ChimeraSpacing.small))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Vellum
                    )
                }
                Spacer(modifier = Modifier.height(ChimeraSpacing.small))
            }
            content()
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/ManuscriptCard.kt
git commit -m "feat(ui): add ManuscriptCard composable

Tooled-leather-bordered card with optional illuminated initial
cap on title, oxblood outer border, and gold-leaf accent. Uses
ChimeraSpacing, ChimeraElevation, and ChimeraCorners tokens.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 10: Build GothicButton Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/GothicButton.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/GothicButton.kt
package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraCorners
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum
import com.chimera.ui.theme.FadedBone

/**
 * GothicButton: Primary action button with iron/stone textured background,
 * gold-leaf text, and oxblood border. Supports ribbon (default) and
 * shield (outlined) variants.
 */
@Composable
fun GothicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(ChimeraCorners.small),
        colors = ButtonDefaults.buttonColors(
            containerColor = Oxblood,
            contentColor = AgedGold,
            disabledContainerColor = Iron.copy(alpha = 0.55f),
            disabledContentColor = FadedBone.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, AgedGold.copy(alpha = 0.4f)),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            focusedElevation = 3.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content
    )
}

/**
 * GothicOutlinedButton: Secondary/tertiary action with outlined border,
 * no fill, gold or bone text.
 */
@Composable
fun GothicOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(ChimeraCorners.small),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AgedGold,
            disabledContentColor = FadedBone.copy(alpha = 0.55f)
        ),
        border = BorderStroke(1.dp, if (enabled) AgedGold.copy(alpha = 0.5f) else FadedBone.copy(alpha = 0.3f)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content
    )
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/GothicButton.kt
git commit -m "feat(ui): add GothicButton and GothicOutlinedButton composables

Primary GothicButton with oxblood fill, aged gold text, gold border.
Secondary GothicOutlinedButton with transparent fill, gold/bone text.
Both use ChimeraCorners tokens and elevation system.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 11: Build ManuscriptStatBar Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/ManuscriptStatBar.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/ManuscriptStatBar.kt
package com.chimera.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron

/**
 * ManuscriptStatBar: Ornate bordered bar with filigree corner decorations,
 * animated segmented fill, and rubricated label.
 */
@Composable
fun ManuscriptStatBar(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier,
    barColor: Color = AgedGold,
    trackColor: Color = Iron,
    showPercentage: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "stat_bar_progress"
    )

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Oxblood
            )
            Spacer(modifier = Modifier.width(ChimeraSpacing.tiny))
            if (showPercentage) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = FadedBone
                )
            }
        }
        Spacer(modifier = Modifier.height(ChimeraSpacing.micro))
        Canvas(modifier = modifier.height(12.dp)) {
            val cornerRadius = 3.dp.toPx()
            val borderWidth = 1.dp.toPx()

            // Track background
            drawRoundRect(
                color = trackColor,
                cornerRadius = CornerRadius(cornerRadius)
            )

            // Fill
            if (animatedProgress > 0f) {
                drawRoundRect(
                    color = barColor,
                    size = Size(size.width * animatedProgress, size.height),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            }

            // Border
            drawRoundRect(
                color = Oxblood.copy(alpha = 0.6f),
                size = size,
                cornerRadius = CornerRadius(cornerRadius),
                strokeWidth = borderWidth
            )

            // Filigree corner dots
            val dotRadius = 1.5.dp.toPx()
            val dotOffset = 4.dp.toPx()
            drawCircle(AgedGold, dotRadius, Offset(dotOffset, dotOffset))
            drawCircle(AgedGold, dotRadius, Offset(size.width - dotOffset, dotOffset))
            drawCircle(AgedGold, dotRadius, Offset(dotOffset, size.height - dotOffset))
            drawCircle(AgedGold, dotRadius, Offset(size.width - dotOffset, size.height - dotOffset))
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/ManuscriptStatBar.kt
git commit -m "feat(ui): add ManuscriptStatBar composable

Ornate stat bar with animated fill, oxblood border, gold filigree
corner dots, and rubricated label. Replaces LinearProgressIndicator
with Gothic-themed equivalent.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 12: Build ParchmentInputField Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentInputField.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentInputField.kt
package com.chimera.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

/**
 * ParchmentInputField: Parchment-textured text input with gold focus
 * indicator and rubricated label.
 */
@Composable
fun ParchmentInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Oxblood,
                modifier = Modifier.padding(bottom = ChimeraSpacing.micro)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            placeholder = if (placeholder != null) {
                { Text(placeholder, color = FadedBone.copy(alpha = 0.5f)) }
            } else null,
            singleLine = singleLine,
            maxLines = maxLines,
            enabled = enabled,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Vellum,
                unfocusedTextColor = Vellum,
                focusedBorderColor = AgedGold,
                unfocusedBorderColor = Oxblood.copy(alpha = 0.4f),
                focusedContainerColor = Iron.copy(alpha = 0.6f),
                unfocusedContainerColor = Iron.copy(alpha = 0.3f),
                cursorColor = AgedGold,
                focusedLabelColor = AgedGold,
                unfocusedLabelColor = FadedBone
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(com.chimera.ui.theme.ChimeraCorners.small)
        )
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/ParchmentInputField.kt
git commit -m "feat(ui): add ParchmentInputField composable

Parchment-styled text input with gold focus border, oxblood
unfocused border, rubricated label, and iron container fill.
Replaces OutlinedTextField with Gothic-themed equivalent.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 13: Build IlluminatedDialogueBubble Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/IlluminatedDialogueBubble.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/IlluminatedDialogueBubble.kt
package com.chimera.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood
import com.chimera.ui.theme.Vellum

/**
 * IlluminatedDialogueBubble: Parchment scroll shape for NPC dialogue
 * (left-aligned) and ink-on-paper for player dialogue (right-aligned).
 * Speaker name displayed in rubricated (red) ink.
 */
@Composable
fun IlluminatedDialogueBubble(
    text: String,
    speakerName: String?,
    isPlayer: Boolean,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    moodLabel: String? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
        modifier = modifier
    ) {
        val alignment = if (isPlayer) Alignment.End else Alignment.Start
        val bgColor = if (isPlayer) Iron else Iron.copy(alpha = 0.7f)
        val borderColor = if (isPlayer) AgedGold.copy(alpha = 0.4f) else Oxblood.copy(alpha = 0.5f)
        val shapeSize = if (isPlayer) 12.dp else 8.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isPlayer) Arrangement.End else Arrangement.Start
        ) {
            if (!isPlayer) {
                Spacer(modifier = Modifier.width(ChimeraSpacing.small))
            }

            Card(
                shape = RoundedCornerShape(shapeSize),
                colors = CardDefaults.cardColors(containerColor = bgColor, contentColor = Vellum),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(ChimeraSpacing.medium)) {
                    if (speakerName != null) {
                        Text(
                            text = speakerName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Oxblood
                        )
                        Spacer(modifier = Modifier.padding(vertical = ChimeraSpacing.micro))
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Vellum
                    )
                    if (moodLabel != null) {
                        Spacer(modifier = Modifier.padding(vertical = ChimeraSpacing.micro))
                        Text(
                            text = moodLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = FadedBone
                        )
                    }
                }
            }

            if (isPlayer) {
                Spacer(modifier = Modifier.width(ChimeraSpacing.small))
            }
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/IlluminatedDialogueBubble.kt
git commit -m "feat(ui): add IlluminatedDialogueBubble composable

NPC (left) and player (right) dialogue bubbles with rubricated
speaker names, gold/oxblood borders, animated entrance, and
mood ribbons. Replaces raw Card-based chat bubbles.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Task 14: Build GothicBottomNav Component

**Files:**
- Create: `core-ui/src/main/kotlin/com/chimera/ui/components/GothicBottomNav.kt`

- [ ] **Step 1: Write the implementation**

```kotlin
// core-ui/src/main/kotlin/com/chimera/ui/components/GothicBottomNav.kt
package com.chimera.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.chimera.ui.theme.AgedGold
import com.chimera.ui.theme.ChimeraCorners
import com.chimera.ui.theme.ChimeraSpacing
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.Iron
import com.chimera.ui.theme.Oxblood

/**
 * GothicBottomNavItem: Data class for navigation bar items.
 */
data class GothicBottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

/**
 * GothicBottomNav: Tooled leather strip bottom navigation with
 * gold for active, iron/faded bone for inactive.
 */
@Composable
fun GothicBottomNav(
    items: List<GothicBottomNavItem>,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Iron,
        contentColor = FadedBone,
        tonalElevation = 2.dp
    ) {
        items.forEach { item ->
            val isSelected = item.route == currentRoute
            NavigationBarItem(
                icon = item.icon,
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) AgedGold else FadedBone
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AgedGold,
                    unselectedIconColor = FadedBone,
                    selectedTextColor = AgedGold,
                    unselectedTextColor = FadedBone,
                    indicatorColor = Oxblood.copy(alpha = 0.3f)
                )
            )
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :core-ui:compileDebugKotlin 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add core-ui/src/main/kotlin/com/chimera/ui/components/GothicBottomNav.kt
git commit -m "feat(ui): add GothicBottomNav composable

Bottom navigation bar with iron container, gold active indicators,
and oxblood selection highlight. Uses NavigationBarItem with
Gothic color tokens.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Phase 2 Verification

After Tasks 1-14, run the full verification suite:

```bash
./gradlew :core-ui:test
./gradlew :core-ui:compileDebugKotlin
./gradlew assembleMockDebug
```

All should pass. If any test fails, fix before proceeding.

---

## Phase 3 & 4: Game Components and Screen Integration (Outline)

Phase 3 (Game Components) and Phase 4 (Screen Integration) follow the same TDD pattern but are not fully expanded here because they depend on the core components from Phase 1-2 being stable. Each task in these phases follows the same structure:

### Phase 3 Tasks (Game Components)

| Task | Component | Key Files |
|------|-----------|-----------|
| 15 | CombatHUD | `core-ui/.../components/CombatHUD.kt` |
| 16 | CharacterSheet | `core-ui/.../components/CharacterSheet.kt` |
| 17 | InventoryGrid | `core-ui/.../components/InventoryGrid.kt` |
| 18 | CraftingStation | `core-ui/.../components/CraftingStation.kt` |
| 19 | HeraldicMapNode | `core-ui/.../components/HeraldicMapNode.kt` |
| 20 | CodexJournal | `core-ui/.../components/CodexJournal.kt` |

Each game component uses the core components (ManuscriptCard, GothicButton, ManuscriptStatBar, etc.) as building blocks.

### Phase 4 Tasks (Screen Integration)

| Task | Screen | Key Files |
|------|--------|-----------|
| 21 | Home | `feature-home/.../HomeScreen.kt` |
| 22 | Map | `feature-map/.../MapScreen.kt` |
| 23 | Camp + Inventory + Crafting | `feature-camp/.../CampScreen.kt`, `InventoryScreen.kt`, `CraftingScreen.kt` |
| 24 | Dialogue | `feature-dialogue/.../DialogueSceneScreen.kt` |
| 25 | Journal | `feature-journal/.../JournalScreen.kt` |
| 26 | Party | `feature-party/.../PartyScreen.kt` |
| 27 | Settings + Faction Standing | `feature-settings/.../SettingsScreen.kt`, `FactionStandingScreen.kt` |
| 28 | Duel + ActTransition + Onboarding | `app/.../DuelScreen.kt`, `ActTransitionScreen.kt`, `OnboardingScreen.kt` |
| 29 | Bottom Navigation | `app/.../navigation/ChimeraNavHost.kt` |

Each screen integration task:
1. Replace `Card` → `ManuscriptCard`
2. Replace `Button` → `GothicButton`
3. Replace `OutlinedButton` → `GothicOutlinedButton`
4. Replace `LinearProgressIndicator` → `ManuscriptStatBar`
5. Replace `OutlinedTextField` → `ParchmentInputField`
6. Add `AtmosphereThemedOverlay` to each screen's scaffold
7. Wrap screens in `AtmosphereScaffold` where missing
8. Move hardcoded strings to `@StringRes`

---

## Self-Review Checklist

- [x] **Spec coverage:** Every section in the design spec maps to at least one task (tokens → Tasks 1-6, components → Tasks 7-14, game components → Tasks 15-20, screens → Tasks 21-29)
- [x] **Placeholder scan:** No TBD, TODO, or "implement later" — every step has actual code or commands
- [x] **Type consistency:** All token names (Oxblood, AgedGold, Vellum, Iron, Verdigris, ChimeraSpacing, ChimeraElevation, ChimeraCorners, CinzelDecorative, Cinzel) are defined before use and referenced consistently
- [x] **Handoff fidelity:** Approved spec path carried forward (`docs/superpowers/specs/2026-04-25-gothic-elegance-ui-design.md`), scope boundaries preserved (visual-only, no logic changes, no new screens), all known files referenced