# Project Chimera: Gothic Elegance UI Design System

**Date:** 2026-04-25
**Status:** Approved Design Spec

## Overview

A comprehensive UI overhaul for Project Chimera (Android dark-fantasy RPG) adopting a **Gothic Elegance** aesthetic — medieval manuscript-inspired visuals with per-atmosphere illumination. This spec covers the full design system from tokens through screens, targeting all 7 feature modules plus navigation, onboarding, and transitions.

## Scope

- **In:** All 14 composable screens across 7 feature modules, bottom navigation, onboarding flow, splash, save-slot select, duel, and act transitions
- **In:** Full design token system (colors, typography, textures, spacing, elevation)
- **In:** Shared component library (cards, buttons, inputs, stat bars, dialogue bubbles, navigation)
- **In:** Game-specific UI (combat HUD, character sheet, inventory grid, crafting station, map nodes)
- **In:** Figma design system + Canva mood boards + Compose implementation
- **Out:** No gameplay logic changes; visual-only transformation
- **Out:** No new screens beyond those already defined in the navigation graph

## Design Philosophy

"Every screen is a page from a weathered codex." The UI should feel like an illuminated manuscript brought to life — rich textures, ornate borders, and typography evoking medieval scribes, while maintaining modern mobile UX patterns.

### Visual Pillars

1. **Typography:** Serif-heavy with a custom display font (Uncial/Antiqua style) for titles, elegant serif for body. Drop caps, text flourishes, chapter-style headings. Labels and UI text remain sans-serif for readability.
2. **Textures & Materials:** Parchment/paper backgrounds, aged-paper overlays, ink-bleed effects on borders, gold-leaf accents, leather-bound card frames. Film grain + vignette overlay on all screens.
3. **Ornamentation:** Illuminated initial capitals, filigree corner decorations, heraldic shields for faction symbols, ribbon banners for titles.
4. **Refined Palette:** Colors drawn from medieval pigments — deep oxblood, aged gold leaf, vellum cream, iron gray, muted verdigris.

## Design Tokens

### Color Palette (Refined from Existing)

| Token | Current | Refined | Usage |
|-------|---------|---------|-------|
| Oxblood (primary) | `#8B1A1A` | `#5C1A1A` | Primary actions, deep accents |
| Aged Gold (secondary) | `#D4A017` | `#C89B3C` | Highlights, active states, gold leaf |
| Vellum (text) | `#E8DCC8` | `#F5ECD7` | Primary text, headings |
| Iron (surface) | `#1A1A1F` | `#2A2A2E` | Surfaces, card backgrounds |
| Verdigris (tertiary) | `#2E5E3F` | `#4A7C59` | Success states, nature accents |
| Ash Black (background) | `#0D0D0F` | Keep | Main background |
| Faded Bone (secondary text) | `#A89B8C` | Keep | Secondary text |
| Dim Ash (tertiary text) | `#5A6270` | Keep | Muted text, placeholders |

### Typography

- **Display (36sp):** [Cinzel Decorative](https://fonts.google.com/specimen/Cinzel+Decorative) (OFL-licensed, Google Fonts), bold, letter-spacing 2sp, gold (#C89B3C). Fallback chain: Cinzel Decorative → Cinzel → FontFamily.Serif.
- **Headlines (24-28sp):** Serif (FontFamily.Serif), semi-bold, letter-spacing 1sp
- **Titles (16-18sp):** Serif, semi-bold, parchment (#F5ECD7)
- **Body (14-16sp):** Serif, regular, line-height 1.5, vellum (#F5ECD7) or faded bone (#A89B8C)
- **Labels (10-14sp):** Sans-serif, medium/semi-bold, uppercase with letter-spacing

### Texture System

1. **Parchment Background:** Slightly textured off-white for cards, journals, dialogue boxes. Achieved via Compose Canvas grain + gradient.
2. **Leather Borders:** Card borders with subtle emboss effect via layered strokes and shadows.
3. **Gold Leaf Accents:** Metallic gold shimmer on key interactive elements (buttons, active tabs, selection states).
4. **Film Grain Overlay:** Atmospheric grain + vignette via existing `AtmosphereOverlay` composable, intensity varies by atmosphere (0.08-0.22).

### Per-Atmosphere Illumination

| Atmosphere | Style | Accent | Vignette Color | Vignette Alpha | Grain |
|-----------|-------|--------|----------------|----------------|-------|
| Forest | Verdant Manuscript | Green ink vines, gold leaf initials | `#1A3A1A` | 0.30 | 10% |
| Cave | Iron Script | Charcoal tones, rust accents | `#1A1A1A` | 0.45 | 18% |
| Dungeon | Blood Inquisition | Crimson drop caps, iron chains | `#1A0A0A` | 0.55 | 22% |
| Camp | Candlelit Chronicle | Warm amber, flickering light | `#2A1A0A` | 0.35 | 14% |
| World Map | Cartographer's Folio | Compass rose motifs, ocean ink | `#1A1A2A` | 0.25 | 8% |
| Dialogue | Scribe's Record | Sepia tones, ruled lines, wax seals | `#2A1A0A` | 0.30 | 12% |

## Component Library

### Core Components (Compose)

1. **ManuscriptCard** — Tooled leather border (2-tone stroke), parchment interior, optional gold-leaf accent border, illuminated initial cap on title, subtle shadow.
2. **GothicButton** — Iron/stone textured background (dark gradient), gold-leaf text, pressed effect on click, ribbon-style primary variant, shield-shaped secondary variant.
3. **IlluminatedDialogueBubble** — Parchment scroll shape for NPC (left-aligned), ink-on-paper for player (right-aligned), speaker name in rubricated (red) ink, mood ribbon, animated entrance.
4. **ManuscriptStatBar** — Ornate bordered bar with filigree corner decorations, segmented fill with animation, heraldic icon for stat type, label in rubricated text.
5. **GothicBottomNav** — Tooled leather strip background, heraldic shield icons, gold for active destination, iron for inactive, subtle emboss effect on press.
6. **ParchmentInputField** — Parchment-textured background, ink-quill cursor animation, ruled-line background guide, focus indicator in gold.

### Game-Specific Components

1. **CombatHUD** — Shield-shaped health bars (player left, enemy right), sword-cross turn indicator, medieval dice roll animation, intent card hand (fan layout), resolve bar as segmented manuscript columns.
2. **CharacterSheet** — Manuscript-style stat page with illuminated portrait frame, heraldic stat icons, ribbon-bound section dividers, quill-pen edit animation for renamable fields.
3. **InventoryGrid** — Scrollable parchment grid (2-3 columns), item cards as miniature manuscript pages with illuminated rarity borders (legendary=gold, rare=verdigris, uncommon=faded bone, common=dim ash), quantity as Roman numerals.
4. **CraftingStation** — Alchemist's table layout, recipe as unfurled scroll, material slots with ingredient illustrations, result animation with shimmering reveal.
5. **HeraldicMapNode** — Shield-shaped markers instead of circles, compass-rose connection lines, parchment-style detail sheet in bottom panel.
6. **CodexJournal** — True codex-style layout with facing pages, illuminated chapter headings, marginalia decorations, ribbon bookmarks for tabs, page-turn animation on navigation.

## Screen Architecture

### Navigation Flow

Splash -> Onboarding -> SaveSlotSelect -> GameGraph

GameGraph contains:
- Home (bottom nav)
- Map (bottom nav)
- Camp (bottom nav) -> Inventory, Crafting
- Journal (bottom nav)
- Party (bottom nav)
- Settings -> Faction Standing
- Dialogue/{sceneId}
- Duel/{opponentId}
- ActTransition/{actTag}

### Screen-Specific Treatments

1. **Home** — Chapter opening feel. Welcome greeting in illuminated display font. Continue CTA as GothicButton with scroll icon. Active Vows as manuscript-marginalia list.
2. **Map** — Cartographer's folio. Full-screen canvas with compass rose, heraldic node markers, dashed ink connection lines, fog-of-war as faded parchment spots, bottom sheet as scroll unfurling.
3. **Camp** — Candlelit chronicle. Warm amber palette, flickering firelight animation, morale bar as candle-wax gauge, companion cards with tooled leather borders, duty assignments as wax seal toggles.
4. **Dialogue** — Scribe's record. NPC header with illuminated portrait + disposition ring, transcript as ruled manuscript lines, quick intents as rubricated chips, free input as parchment field, speaking wave as quill animation.
5. **Journal** — Codex layout. Facing-page entry display, illuminated chapter headings, ribbon bookmark tabs, search as "search the archives" with ink-wash effect, entry cards as manuscript excerpts.
6. **Party** — Herald's registry. Tab bar as ribbon banners, companion cards with full heraldic achievement (shield, motto, sigil), detail view as heraldic scroll with disposition graph as wave line, faction view as vassalage chart.
7. **Settings** — Scribe's ledger. Cleaner parchment-style cards, switch toggles as wax seal stamps, sliders as ruled measure marks, section headers as rubricated chapter titles.
8. **Duel** — Combat manuscript. Turn indicator as hourglass animation, intent cards as combat scrolls, dice as bone tokens, health as shield gauges, resolve as manuscript columns.
9. **ActTransition** — Chapter frontispiece. Full-screen illuminated manuscript page with chapter number in Roman numerals, ornate border frame, title in display font, subtitle in rubricated text, staggered fade-in animation.
10. **Onboarding** — Novice's primer. Step-by-step illuminated pages with instructional illustrations, "turn the page" prompt, final page as oath-taking ceremony with signature line.

## Implementation Approach

### Phase 1: Foundation (Design Tokens + Theme)
1. Refine ChimeraColors.kt with Gothic palette
2. Add custom font loading for Cinzel Decorative (OFL, bundled as Android resource)
3. Enhance ChimeraTypography.kt with serif body styles
4. Update Atmosphere.kt palettes for per-atmosphere illumination
5. Add texture/grain enhancements to AtmosphereOverlay.kt
6. _(Parallel, design-track)_ Create Figma token library (variables + styles)

### Phase 2: Core Components
1. Build ManuscriptCard component
2. Build GothicButton component
3. Build IlluminatedDialogueBubble component
4. Build ManuscriptStatBar component
5. Build GothicBottomNav component
6. Build ParchmentInputField component
7. _(Parallel, design-track)_ Create Figma component library

### Phase 3: Game Components
1. Build CombatHUD components
2. Build CharacterSheet components
3. Build InventoryGrid components
4. Build CraftingStation components
5. Build HeraldicMapNode components
6. Build CodexJournal components
7. _(Parallel, design-track)_ Create Canva mood boards for visual reference

### Phase 4: Screen Integration
1. Apply design system to Home screen
2. Apply design system to Map screen
3. Apply design system to Camp/Inventory/Crafting
4. Apply design system to Dialogue
5. Apply design system to Journal
6. Apply design system to Party
7. Apply design system to Settings/Faction Standing
8. Apply design system to Duel/ActTransition/Onboarding

## Constraints

- Keep Android-first approach; no web dependencies
- Preserve existing architecture (Hilt, Navigation Compose, Material3)
- Maintain offline-first: AI must remain optional
- All existing game logic, data models, and navigation structure must be preserved
- Strings should move to @StringRes where currently hardcoded
- Custom fonts must be bundled as Android resources, not loaded from network
- Display font: Cinzel Decorative (OFL-licensed via Google Fonts), fallback chain: Cinzel Decorative → Cinzel → FontFamily.Serif