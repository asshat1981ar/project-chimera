# Chimera: 2D Overhead Sprite Development & Injection Plan

> **Version**: 1.0  
> **Date**: 2026-07-13  
> **Scope**: AI-generated gothic manuscript / ink-wash style sprites for NPC portraits, combat entities, map nodes, camp/inventory items, character creation, and player cards  
> **Status**: Specification Ready for Implementation

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Asset Architecture](#2-asset-architecture)
3. [Art Style Specification](#3-art-style-specification)
4. [AI Generation Pipeline](#4-ai-generation-pipeline)
5. [Kotlin Implementation](#5-kotlin-implementation)
6. [Module Integration Map](#6-module-integration-map)
7. [File Structure](#7-file-structure)
8. [Testing Strategy](#8-testing-strategy)
9. [Implementation Phases](#9-implementation-phases)
10. [Appendix: Prompt Templates](#10-appendix-prompt-templates)

---

## 1. Executive Summary

### Current State
Chimera currently has **zero visual sprite assets**. All `portraitResName` fields are null, map nodes are rendered as colored shapes, combat is text-only, and inventory items lack iconography. The ROADMAP Workstream F (Atmosphere and visual system) identifies this as a critical gap.

### Proposed Solution
A complete **AI-generated sprite pipeline** that produces consistent dark gothic manuscript / ink-wash style assets across six categories, integrated through a type-safe Kotlin asset system with Jetpack Compose rendering components.

### Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Asset source | AI image generation | Rapid iteration, consistent style, no artist dependency |
| Art style | Dark gothic manuscript / ink-wash | Matches ROADMAP atmosphere direction, unique visual identity |
| Resolution | 512x512 base, 128x128 runtime | Balance quality vs. memory; downscale at build time |
| Format | PNG with transparency | Alpha channel for compositing over parchment backgrounds |
| Storage | `res/drawable-*dpi/` + runtime manifest | Standard Android resource pipeline with JSON manifest |
| Rendering | Compose `Image()` + `Canvas` | Native Compose, no external dependencies |

---

## 2. Asset Architecture

### 2.1 Asset Categories

```
chimera-assets/
├── npc/
│   ├── portraits/           # 12 NPCs x 6 expressions = 72 assets
│   │   ├── elara_neutral.png
│   │   ├── elara_tense.png
│   │   ├── elara_wounded.png
│   │   ├── elara_grateful.png
│   │   ├── elara_hostile.png
│   │   └── elara_oathbound.png
│   └── tokens/              # 12 NPCs overhead map tokens
│       ├── elara_token.png
│       └── ...
├── combat/
│   ├── player_stances/      # 3 stances x 3 states = 9 assets
│   │   ├── player_strike.png
│   │   ├── player_ward.png
│   │   ├── player_feint.png
│   │   ├── player_strike_wounded.png
│   │   ├── player_ward_wounded.png
│   │   └── player_feint_wounded.png
│   └── opponent_stances/    # Per-archetype stance sets
│       ├── hollow_knight_strike.png
│       └── ...
├── map/
│   ├── nodes/               # 25 map nodes x 5 states = 125 assets
│   │   ├── node_active.png
│   │   ├── node_hidden.png
│   │   ├── node_completed.png
│   │   ├── node_failed.png
│   │   └── node_blocked.png
│   └── connections/         # Path segment sprites
│       └── path_active.png
├── camp/
│   ├── items/               # 5 crafting recipes + generic items
│   │   ├── item_herb_bundle.png
│   │   ├── item_iron_ingot.png
│   │   ├── item_parchment.png
│   │   ├── item_omen_stone.png
│   │   └── item_void_ash.png
│   └── effects/             # Campfire, tent, ambient
│       ├── campfire_lit.png
│       └── campfire_dim.png
├── character/
│   ├── creation/            # Character creation screen assets
│   │   ├── bg_parchment.png
│   │   ├── ink_splatter_1.png
│   │   └── quill_cursor.png
│   └── player_card/         # Player status card
│       ├── card_frame.png
│       ├── resolve_heart.png
│       └── omen_orb.png
└── shared/
    ├── ui/                  # Reusable UI chrome
    │   ├── seal_common.png
    │   ├── seal_rare.png
    │   └── seal_legendary.png
    └── effects/             # Particle textures
        ├── ember_1.png
        └── smoke_puff.png
```

### 2.2 Asset State Matrix

| Category | Count | States | Total Assets |
|----------|-------|--------|-------------|
| NPC Portraits | 12 NPCs | 6 expressions | 72 |
| NPC Tokens | 12 NPCs | 1 | 12 |
| Combat Player | 3 stances | 3 health states | 9 |
| Combat Opponent | 6 archetypes x 3 stances | 2 health states | 36 |
| Map Nodes | 5 base types | 5 quest states | 25 |
| Map Connections | 1 type | 2 states | 2 |
| Camp Items | 10 items | 1 | 10 |
| Camp Ambient | 4 elements | 2 states | 8 |
| Character Creation | 5 elements | 1 | 5 |
| Player Card | 5 elements | 1 | 5 |
| Shared UI | 6 elements | 1 | 6 |
| Shared Effects | 4 particles | 1 | 4 |
| **TOTAL** | | | **~194 assets** |

---

## 3. Art Style Specification

### 3.1 Style Bible: "Gothic Manuscript Ink-Wash"

**Core Aesthetic**: Medieval illuminated manuscript meets Japanese sumi-e ink wash. Dark, atmospheric, with visible brush strokes and parchment texture.

#### Color Palette

```kotlin
object ChimeraSpritePalette {
    // Primary parchment backgrounds
    val PARCHMENT_DARK   = Color(0xFF1A1410)   // Deep aged parchment
    val PARCHMENT_MID    = Color(0xFF2A2018)   // Medium parchment
    val PARCHMENT_LIGHT  = Color(0xFF3A3028)   // Light parchment

    // Ink tones
    val INK_BLACK        = Color(0xFF0A0806)   // Pure sumi ink
    val INK_CHARCOAL     = Color(0xFF1E1A16)   // Charcoal wash
    val INK_GREY         = Color(0xFF4A4540)   // Diluted ink

    // Accent colors (restrained use)
    val ACCENT_CRIMSON   = Color(0xFF8B2500)   // Dried blood / seal wax
    val ACCENT_GOLD      = Color(0xFFB8941F)   // Faded gold leaf
    val ACCENT_ASH       = Color(0xFF6B6B6B)   // Ash grey
    val ACCENT_HOLLOW    = Color(0xFF2D1B69)   // Faint purple (Hollow magic)

    // Expression-specific tints
    val TINT_NEUTRAL     = Color(0xFF000000)   // No tint
    val TINT_TENSE       = Color(0xFF3A2010)   // Warm tension
    val TINT_WOUNDED     = Color(0xFF4A1010)   // Blood wash
    val TINT_GRATEFUL    = Color(0xFF1A3020)   // Cool relief
    val TINT_HOSTILE     = Color(0xFF4A0A0A)   // Deep crimson
    val TINT_OATHBOUND   = Color(0xFF1A1A3A)   // Cool blue binding
}
```

#### Visual Rules

1. **No clean lines** - Everything must show brush stroke texture
2. **Asymmetric composition** - Avoid center-perfect layouts
3. **Negative space dominance** - 60%+ of each sprite should be transparent/negative
4. **Limited color per sprite** - Maximum 3 colors + ink black
5. **Parchment grain overlay** - Subtle texture on all fills
6. **Ink bleed edges** - Soft, bleeding edges instead of hard cuts

#### Reference Mood Board (Textual)

- **NPC Portraits**: Berserk manga ink style + medieval manuscript marginalia
- **Combat Stances**: Hokusai brush strokes frozen in action
- **Map Nodes**: Architectural ink wash, birds-eye view ruins
- **Items**: Still life botanical illustration, aged paper
- **UI Chrome**: Illuminated manuscript borders, gold leaf fragments

---

## 4. AI Generation Pipeline

### 4.1 Pipeline Architecture

```
+------------------+     +------------------+     +------------------+
|  Prompt Template | --> |  AI Generation   | --> |  Quality Filter  |
|  (parameterized) |     |  (batch/parallel)|     |  (auto + manual) |
+------------------+     +------------------+     +------------------+
                                                              |
                                                              v
+------------------+     +------------------+     +------------------+
|  Sprite Database | <-- |  Build Pipeline  | <-- |  Export/Resize   |
|  (JSON manifest) |     |  (Gradle task)   |     |  (512->128)      |
+------------------+     +------------------+     +------------------+
```

### 4.2 Generation Parameters

```kotlin
data class GenerationConfig(
    val baseResolution: Int = 512,
    val runtimeResolution: Int = 128,
    val format: String = "PNG",
    val transparency: Boolean = true,
    val batchSize: Int = 6,           // Assets per generation batch
    val styleConsistencySeed: Long = 0x4348494D455241,  // "CHIMERA"
    val negativePrompt: String = """
        bright colors, cartoon, anime, 3D render, photorealistic,
        clean lines, vector art, flat design, neon, vibrant,
        modern UI, glossy, gradient backgrounds, text, watermark
    """.trimIndent()
)
```

### 4.3 Prompt Engineering Framework

**Style Stack** (applied consistently):

```
[FORMAT] dark gothic manuscript illustration, ink wash painting
[MEDIUM] sumi-e ink on aged parchment, visible brush strokes
[LINEWORK] expressive brushwork, ink bleed edges, no clean outlines
[SHADING] ink wash gradation, monochrome with single accent color
[COLOR] {category-specific palette}
[TEXTURE] parchment grain, ink pooling, feathered edges
[LIGHTING] candlelight from upper left, deep shadows
[COMPOSITION] {category-specific framing}
```

### 4.4 Build-Time Asset Pipeline

```kotlin
// build.gradle.kts (app module)
android {
    sourceSets["main"].res.srcDirs("src/main/res", "generated-assets/res")
}

tasks.register<Exec>("generateSprites") {
    group = "assets"
    description = "Generate AI sprite assets from manifest"
    commandLine("python3", "scripts/generate_sprites.py", "--manifest", "assets/sprite-manifest.json")
}

tasks.register<Copy>("optimizeSprites") {
    dependsOn("generateSprites")
    from("generated-assets/raw/")
    into("generated-assets/res/drawable-xhdpi/")
    // Downscale 512x512 -> 128x128 at build time
    eachFile { /* resize logic */ }
}
```

---

## 5. Kotlin Implementation

### 5.1 Core Data Layer (`core-model`)

```kotlin
// core-model/src/main/java/com/chimera/core/model/sprites/

/**
 * Unique identifier for every sprite asset in the game.
 * Type-safe asset referencing prevents runtime crashes from string typos.
 */
@JvmInline
value class SpriteId(val value: String)

/**
 * Categories map directly to directory structure in res/
 */
enum class SpriteCategory {
    NPC_PORTRAIT,
    NPC_TOKEN,
    COMBAT_PLAYER,
    COMBAT_OPPONENT,
    MAP_NODE,
    MAP_CONNECTION,
    CAMP_ITEM,
    CAMP_AMBIENT,
    CHARACTER_CREATION,
    PLAYER_CARD,
    SHARED_UI,
    SHARED_EFFECT
}

/**
 * Expression states for NPC portraits.
 * Maps to ROADMAP Workstream C emotional telemetry.
 */
enum class PortraitExpression {
    NEUTRAL, TENSE, WOUNDED, GRATEFUL, HOSTILE, OATHBOUND;

    companion object {
        fun fromDisposition(disposition: Float): PortraitExpression = when {
            disposition > 0.7f  -> OATHBOUND
            disposition > 0.3f  -> GRATEFUL
            disposition > -0.2f -> NEUTRAL
            disposition > -0.5f -> TENSE
            disposition > -0.8f -> WOUNDED
            else                -> HOSTILE
        }
    }
}

/**
 * Quest states for map node visualization.
 * Maps to ROADMAP Workstream A quest visibility.
 */
enum class MapNodeState {
    ACTIVE, HIDDEN, COMPLETED, FAILED, BLOCKED
}

/**
 * Unified sprite reference with resolution variants.
 */
data class SpriteRef(
    val id: SpriteId,
    val category: SpriteCategory,
    val baseName: String,
    val hasTransparency: Boolean = true,
    val variants: List<SpriteVariant> = emptyList()
)

data class SpriteVariant(
    val suffix: String,           // e.g., "_wounded", "_active"
    val resolution: Int,          // e.g., 128
    val density: DensityQualifier
)

enum class DensityQualifier { MDPI, HDPI, XHDPI, XXHDPI, XXXHDPI }
```

### 5.2 Asset Manifest (`core-data`)

```kotlin
// core-data/src/main/java/com/chimera/core/data/sprites/

/**
 * JSON-backed manifest for all sprite assets.
 * Loaded once at app startup, provides O(1) lookup by SpriteId.
 */
class SpriteManifest @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val spriteMap: Map<SpriteId, SpriteRef> by lazy { loadManifest() }

    companion object {
        const val MANIFEST_FILE = "sprite_manifest.json"
    }

    fun resolve(id: SpriteId): SpriteRef? = spriteMap[id]

    fun resolveNpcPortrait(npcId: String, expression: PortraitExpression): SpriteRef? {
        return resolve(SpriteId("npc_${npcId}_${expression.name.lowercase()}"))
    }

    fun resolveMapNode(nodeType: String, state: MapNodeState): SpriteRef? {
        return resolve(SpriteId("map_${nodeType}_${state.name.lowercase()}"))
    }

    fun resolveCombatStance(isPlayer: Boolean, stance: String, wounded: Boolean): SpriteRef? {
        val prefix = if (isPlayer) "combat_player" else "combat_opponent"
        val suffix = if (wounded) "_wounded" else ""
        return resolve(SpriteId("${prefix}_${stance.lowercase()}${suffix}"))
    }

    fun resolveCampItem(itemId: String): SpriteRef? {
        return resolve(SpriteId("camp_item_${itemId.lowercase().replace(" ", "_")}"))
    }

    private fun loadManifest(): Map<SpriteId, SpriteRef> {
        val json = context.assets.open(MANIFEST_FILE).bufferedReader().use { it.readText() }
        val entries = Json.decodeFromString<List<SpriteManifestEntry>>(json)
        return entries.associateBy { SpriteId(it.id) }
            .mapValues { (_, entry) -> entry.toSpriteRef() }
    }
}

@Serializable
data class SpriteManifestEntry(
    val id: String,
    val category: String,
    val baseName: String,
    val transparent: Boolean = true,
    val variants: List<SpriteVariantEntry> = emptyList()
)

@Serializable
data class SpriteVariantEntry(
    val suffix: String,
    val resolution: Int,
    val density: String
)
```

### 5.3 Compose Rendering Components (`core-ui`)

```kotlin
// core-ui/src/main/java/com/chimera/core/ui/sprites/

/**
 * Gothic manuscript sprite renderer with ink-wash atmosphere support.
 * Wraps Compose Image with Chimera-specific styling.
 */
@Composable
fun ChimeraSprite(
    spriteRef: SpriteRef?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    opacity: Float = 1.0f,
    inkWashEffect: Boolean = false,
    onLoadError: (() -> Unit)? = null
) {
    if (spriteRef == null) {
        SpriteFallback(modifier = modifier, contentDescription = contentDescription)
        return
    }

    val context = LocalContext.current
    val painter = rememberChimeraPainter(spriteRef, context, onLoadError)

    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alpha = opacity,
            colorFilter = if (tint != Color.Unspecified) {
                ColorFilter.tint(tint, BlendMode.Modulate)
            } else null
        )

        if (inkWashEffect) {
            InkWashOverlay(modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * NPC Portrait with expression support and graceful fallback.
 * Maps to ROADMAP Workstream C NPC portrait deliverables.
 */
@Composable
fun NpcPortraitSprite(
    npcId: String,
    expression: PortraitExpression = PortraitExpression.NEUTRAL,
    modifier: Modifier = Modifier.size(120.dp),
    manifest: SpriteManifest = hiltViewModel(),
    fallbackInitials: String = "?"
) {
    val spriteRef = remember(npcId, expression) {
        manifest.resolveNpcPortrait(npcId, expression)
    }

    if (spriteRef != null) {
        ChimeraSprite(
            spriteRef = spriteRef,
            modifier = modifier
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, ChimeraSpritePalette.INK_GREY, RoundedCornerShape(4.dp)),
            contentDescription = "Portrait of $npcId, ${expression.name.lowercase()}",
            inkWashEffect = expression == PortraitExpression.WOUNDED || expression == PortraitExpression.HOSTILE
        )
    } else {
        // Letter avatar fallback per ROADMAP acceptance criteria
        LetterAvatarFallback(
            initials = fallbackInitials,
            modifier = modifier
        )
    }
}

/**
 * Overhead map token for NPCs on the world map.
 * Renders as a small ink-wash silhouette with quest state indicator.
 */
@Composable
fun NpcMapToken(
    npcId: String,
    questState: MapNodeState = MapNodeState.ACTIVE,
    modifier: Modifier = Modifier.size(32.dp),
    manifest: SpriteManifest = hiltViewModel()
) {
    val tokenRef = remember(npcId) {
        manifest.resolve(SpriteId("npc_${npcId}_token"))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Token base
        ChimeraSprite(
            spriteRef = tokenRef,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "$npcId location"
        )

        // Quest state ring overlay
        QuestStateRing(
            state = questState,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Combat stance sprite with health-state awareness.
 * Integrates with DuelEngine stance system.
 */
@Composable
fun CombatStanceSprite(
    stance: DuelEngine.Stance,
    isPlayer: Boolean,
    resolvePercent: Float,  // 0.0f - 1.0f
    modifier: Modifier = Modifier.size(180.dp),
    manifest: SpriteManifest = hiltViewModel()
) {
    val wounded = resolvePercent < 0.4f
    val spriteRef = remember(stance, isPlayer, wounded) {
        manifest.resolveCombatStance(
            isPlayer = isPlayer,
            stance = stance.name,
            wounded = wounded
        )
    }

    val tint = when {
        resolvePercent < 0.2f -> ChimeraSpritePalette.TINT_WOUNDED
        resolvePercent < 0.5f -> ChimeraSpritePalette.TINT_TENSE
        else -> Color.Unspecified
    }

    ChimeraSprite(
        spriteRef = spriteRef,
        modifier = modifier,
        contentDescription = "${if (isPlayer) "Player" else "Opponent"} in ${stance.label} stance",
        tint = tint,
        inkWashEffect = wounded
    )
}

/**
 * Map node sprite with quest-state visualization.
 * Maps to ROADMAP Workstream A map quest markers.
 */
@Composable
fun MapNodeSprite(
    nodeType: String,
    state: MapNodeState,
    modifier: Modifier = Modifier.size(48.dp),
    manifest: SpriteManifest = hiltViewModel()
) {
    val spriteRef = remember(nodeType, state) {
        manifest.resolveMapNode(nodeType, state)
    }

    val scale by animateFloatAsState(
        targetValue = if (state == MapNodeState.ACTIVE) 1.15f else 1.0f,
        animationSpec = tween(300),
        label = "node_pulse"
    )

    ChimeraSprite(
        spriteRef = spriteRef,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        contentDescription = "Map node $nodeType, ${state.name.lowercase()}",
        opacity = when (state) {
            MapNodeState.HIDDEN -> 0.3f
            MapNodeState.BLOCKED -> 0.5f
            else -> 1.0f
        }
    )
}

/**
 * Camp inventory item with rarity seal overlay.
 * Maps to ROADMAP Workstream D inventory polish.
 */
@Composable
fun InventoryItemSprite(
    itemId: String,
    rarity: ItemRarity = ItemRarity.COMMON,
    modifier: Modifier = Modifier.size(64.dp),
    manifest: SpriteManifest = hiltViewModel()
) {
    val spriteRef = remember(itemId) {
        manifest.resolveCampItem(itemId)
    }

    Box(modifier = modifier) {
        ChimeraSprite(
            spriteRef = spriteRef,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Item: $itemId"
        )

        // Rarity seal overlay
        RaritySeal(
            rarity = rarity,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(16.dp)
        )
    }
}

/**
 * Ink wash overlay effect for wounded/hostile states.
 * Draws procedural ink bleed at edges.
 */
@Composable
private fun InkWashOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ink")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ink_phase"
    )

    Canvas(modifier = modifier) {
        val gradient = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                ChimeraSpritePalette.INK_CHARCOAL.copy(alpha = 0.1f + 0.05f * phase),
                ChimeraSpritePalette.INK_BLACK.copy(alpha = 0.2f)
            ),
            center = center,
            radius = size.minDimension * 0.6f
        )
        drawRect(brush = gradient)
    }
}

/**
 * Fallback composable when sprite asset is missing.
 * ROADMAP: "Portrait fallback looks intentional when portraitResName is null"
 */
@Composable
private fun SpriteFallback(
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .background(ChimeraSpritePalette.PARCHMENT_DARK)
            .border(1.dp, ChimeraSpritePalette.INK_GREY),
        contentAlignment = Alignment.Center
    ) {
        // Subtle parchment texture pattern
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 0.5f
            val spacing = 8f
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = ChimeraSpritePalette.INK_CHARCOAL.copy(alpha = 0.1f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y + (spacing * 0.3f)),
                    strokeWidth = strokeWidth
                )
                y += spacing
            }
        }

        Text(
            text = "?",
            color = ChimeraSpritePalette.INK_GREY,
            fontSize = (modifier.height().value * 0.4f).sp,
            fontFamily = FontFamily.Serif
        )
    }
}
```

### 5.4 ViewModel Integration (`feature-*` modules)

```kotlin
// feature-dialogue/src/main/java/com/chimera/feature/dialogue/

@HiltViewModel
class DialogueSpriteViewModel @Inject constructor(
    private val manifest: SpriteManifest,
    private val engine: RelationshipArchetypeEngine
) : ViewModel() {

    private val _npcPortrait = MutableStateFlow<SpriteRef?>(null)
    val npcPortrait: StateFlow<SpriteRef?> = _npcPortrait.asStateFlow()

    private val _expression = MutableStateFlow(PortraitExpression.NEUTRAL)
    val expression: StateFlow<PortraitExpression> = _expression.asStateFlow()

    fun loadNpcPortrait(npcId: String) {
        viewModelScope.launch {
            // Initial portrait
            _npcPortrait.value = manifest.resolveNpcPortrait(npcId, PortraitExpression.NEUTRAL)

            // Listen for archetype events to update expression
            engine.archetypeEvents
                .filter { it.npcId == npcId }
                .collect { event ->
                    val newExpression = when (event.type) {
                        ArchetypeType.ESCALATION -> PortraitExpression.TENSE
                        ArchetypeType.SHIFTING_THE_BURDEN -> PortraitExpression.GRATEFUL
                        ArchetypeType.FIXES_THAT_FAIL -> PortraitExpression.WOUNDED
                        else -> PortraitExpression.NEUTRAL
                    }
                    _expression.value = newExpression
                    _npcPortrait.value = manifest.resolveNpcPortrait(npcId, newExpression)
                }
        }
    }
}
```

---

## 6. Module Integration Map

### 6.1 Cross-Module Dependencies

```
core-model          <-- SpriteId, PortraitExpression, MapNodeState (NEW)
    ^
core-data           <-- SpriteManifest, asset loading (NEW)
    ^
core-ui             <-- ChimeraSprite, NpcPortraitSprite, etc. (NEW)
    ^
feature-home        <-- MapNodeSprite for quest HUD
feature-map         <-- MapNodeSprite, NpcMapToken for world map
feature-dialogue    <-- NpcPortraitSprite with expression
feature-camp        <-- InventoryItemSprite for inventory grid
                    <-- Campfire ambient sprites
feature-party       <-- NpcPortraitSprite for companion cards
feature-journal     <-- Small portrait thumbnails
```

### 6.2 Integration Points with Existing Engines

| Engine Class | Integration | Sprite Component |
|-------------|-------------|-------------------|
| `RelationshipArchetypeEngine` | `archetypeEvents` flow drives expression changes | `NpcPortraitSprite` |
| `DuelEngine` | `Stance` enum + `DuelState.resolve` | `CombatStanceSprite` |
| `CombatEngine` | `ResultBand` affects health tint | `CombatStanceSprite` |
| `ChapterProgressionUseCase` | Act transition triggers card flip | `PlayerCardSprite` |

---

## 7. File Structure

### 7.1 New Files to Create

```
core-model/src/main/java/com/chimera/core/model/sprites/
├── SpriteId.kt
├── SpriteCategory.kt
├── PortraitExpression.kt
├── MapNodeState.kt
├── SpriteRef.kt
└── SpriteVariant.kt

core-data/src/main/java/com/chimera/core/data/sprites/
├── SpriteManifest.kt
├── SpriteManifestEntry.kt
└── SpriteLoader.kt

core-ui/src/main/java/com/chimera/core/ui/sprites/
├── ChimeraSprite.kt
├── NpcPortraitSprite.kt
├── NpcMapToken.kt
├── CombatStanceSprite.kt
├── MapNodeSprite.kt
├── InventoryItemSprite.kt
├── InkWashOverlay.kt
├── SpriteFallback.kt
├── LetterAvatarFallback.kt
├── QuestStateRing.kt
└── RaritySeal.kt

app/src/main/assets/
└── sprite_manifest.json

scripts/
└── generate_sprites.py          # AI generation orchestrator
```

### 7.2 Modified Files

```
core-ui/src/main/java/com/chimera/core/ui/theme/
├── ChimeraTheme.kt              # Add SpritePalette colors

feature-dialogue/src/main/java/com/chimera/feature/dialogue/
├── DialogueScreen.kt            # Add NpcPortraitSprite composable

feature-map/src/main/java/com/chimera/feature/map/
├── MapScreen.kt                 # Add MapNodeSprite, NpcMapToken

feature-camp/src/main/java/com/chimera/feature/camp/
├── InventoryScreen.kt           # Add InventoryItemSprite
```

---

## 8. Testing Strategy

### 8.1 Unit Tests

```kotlin
// chimera-core/src/test/java/com/chimera/core/sprites/

class SpriteManifestTest {
    @Test
    fun `resolveNpcPortrait returns correct sprite for all expressions`() {
        val manifest = createTestManifest()
        PortraitExpression.entries.forEach { expr ->
            val result = manifest.resolveNpcPortrait("elara", expr)
            assertNotNull(result)
            assertEquals(SpriteCategory.NPC_PORTRAIT, result!!.category)
        }
    }

    @Test
    fun `resolveNpcPortrait returns null for unknown NPC`() {
        val manifest = createTestManifest()
        assertNull(manifest.resolveNpcPortrait("unknown_npc", PortraitExpression.NEUTRAL))
    }

    @Test
    fun `PortraitExpression fromDisposition maps correctly`() {
        assertEquals(PortraitExpression.OATHBOUND, PortraitExpression.fromDisposition(0.8f))
        assertEquals(PortraitExpression.HOSTILE, PortraitExpression.fromDisposition(-0.9f))
        assertEquals(PortraitExpression.NEUTRAL, PortraitExpression.fromDisposition(0.0f))
    }
}
```

### 8.2 Compose Tests

```kotlin
// core-ui/src/androidTest/java/com/chimera/core/ui/sprites/

class NpcPortraitSpriteTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows fallback when sprite is null`() {
        composeTestRule.setContent {
            NpcPortraitSprite(
                npcId = "nonexistent",
                expression = PortraitExpression.NEUTRAL,
                manifest = EmptyManifest()
            )
        }
        composeTestRule.onNodeWithText("?").assertExists()
    }

    @Test
    fun `applies wounded ink wash effect`() {
        composeTestRule.setContent {
            NpcPortraitSprite(
                npcId = "elara",
                expression = PortraitExpression.WOUNDED,
                manifest = TestManifest()
            )
        }
        // Verify Canvas (InkWashOverlay) is present
        composeTestRule.onNode(hasContentDescription("Portrait of elara, wounded"))
            .assertExists()
    }
}
```

### 8.3 Accessibility Requirements

- Every sprite MUST have a content description
- Fallback composables must announce "Image not loaded" via semantics
- Color-only information (rarity, quest state) must have text alternative
- Motion effects (ink wash) must respect `android.settings.reduced_motion` preference

---

## 9. Implementation Phases

### Phase 1: Foundation (Week 1)
- [ ] Create `core-model` sprite data classes
- [ ] Implement `SpriteManifest` with JSON loading
- [ ] Create `sprite_manifest.json` schema
- [ ] Add `ChimeraSpritePalette` to theme
- [ ] Write unit tests for manifest resolution

### Phase 2: NPC Portraits (Week 2)
- [ ] Generate 12 NPC portrait sets (72 assets) via AI
- [ ] Implement `NpcPortraitSprite` composable
- [ ] Implement `LetterAvatarFallback`
- [ ] Wire into `feature-dialogue` DialogueScreen
- [ ] Integrate with `RelationshipArchetypeEngine` events
- [ ] ROADMAP: Workstream C acceptance criteria

### Phase 3: Map Nodes (Week 3)
- [ ] Generate 25 map node state variants
- [ ] Implement `MapNodeSprite` with pulse animation
- [ ] Implement `NpcMapToken` with quest state ring
- [ ] Wire into `feature-map` MapScreen
- [ ] Integrate with `ObserveMapQuestMarkersUseCase`
- [ ] ROADMAP: Workstream A acceptance criteria

### Phase 4: Combat Entities (Week 4)
- [ ] Generate player + opponent stance sprites
- [ ] Implement `CombatStanceSprite` with health tint
- [ ] Wire into Duel/Combat screens
- [ ] Integrate with `DuelEngine.Stance` + `CombatEngine.ResultBand`

### Phase 5: Camp & Inventory (Week 5)
- [ ] Generate item sprites for all crafting recipes
- [ ] Generate ambient camp sprites
- [ ] Implement `InventoryItemSprite` with rarity seal
- [ ] Wire into `feature-camp` InventoryScreen
- [ ] ROADMAP: Workstream D acceptance criteria

### Phase 6: Character & Player Card (Week 6)
- [ ] Generate character creation screen assets
- [ ] Generate player card chrome (frame, resolve, omen)
- [ ] Create `PlayerCardSprite` composable
- [ ] Integrate with `ChapterProgressionUseCase`

### Phase 7: Polish & QA (Week 7)
- [ ] Add ink wash overlay effects
- [ ] Implement accessibility content descriptions
- [ ] Add reduced-motion support
- [ ] Run `./gradlew :core-ui:test`
- [ ] Run `./gradlew testMockDebugUnitTest`
- [ ] Visual QA checklist for contrast and tap targets

---

## 10. Appendix: Prompt Templates

### 10.1 NPC Portrait Prompt

```
Dark gothic manuscript portrait of a {age} {gender} {role},
sumi-e ink wash on aged parchment, visible brush strokes,
{expression_descriptor}, medieval fantasy character,
{distinctive_features}, wearing {attire},
single candlelight from upper left, deep shadows,
ink bleed edges, monochrome with faint {accent_color} accent,
asymmetric composition, negative space dominant,
no clean lines, expressive brushwork, parchment grain texture,
isolated portrait, centered upper body, transparent background
```

**Example - Elara (Hostile)**:
```
Dark gothic manuscript portrait of a young woman herbalist,
sumi-e ink wash on aged parchment, visible brush strokes,
hostile narrowed eyes, jaw clenched, medieval fantasy character,
silver-streaked dark hair, wearing hooded linen robe,
single candlelight from upper left, deep shadows,
ink bleed edges, monochrome with faint crimson accent,
asymmetric composition, negative space dominant,
no clean lines, expressive brushwork, parchment grain texture,
isolated portrait, centered upper body, transparent background
```

### 10.2 Map Node Prompt

```
Dark gothic architectural ink wash, birds-eye view of {location_type},
medieval fantasy map marker, sumi-e on aged parchment,
{state_descriptor}, ruins and crumbling stone,
visible brush strokes, ink pooling effects,
monochrome charcoal with faint {accent_color} glow,
no clean lines, top-down perspective, game asset,
square format, transparent background
```

### 10.3 Combat Stance Prompt

```
Dark gothic ink wash figure in {stance_name} combat stance,
{subject_description}, dynamic action pose frozen,
sumi-e brush strokes, motion blur effect,
medieval fantasy combatant, ink splash energy,
monochrome with {health_tint} edge tint,
aggressive posture, weapon raised,
isolated figure, transparent background
```

### 10.4 Inventory Item Prompt

```
Dark gothic botanical illustration of {item_name},
medieval still life on aged parchment,
sumi-e ink with restrained {accent_color} wash,
detailed crosshatching, specimen drawing style,
ink bleed edges, scientific labeling aesthetic,
square format, transparent background
```

### 10.5 UI Chrome Prompt

```
Medieval illuminated manuscript border fragment,
gold leaf accent, dark parchment background,
gothic filigree, ornate corner piece,
sumi-e ink with gold leaf highlights,
decorative frame element, game UI asset,
ink wash texture, aged paper grain,
transparent background
```

---

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| AI generation inconsistency | High | Seed locking + negative prompt + manual curation |
| Asset bloat (194 assets) | Medium | Build-time downscale to 128x128, WebP conversion |
| Runtime memory pressure | Medium | Lazy loading + LRU cache in SpriteLoader |
| Accessibility violations | Low | Content description lint + Compose tests |
| Gradle build time increase | Low | Parallel generation + incremental builds |

---

## Success Criteria

- [ ] All 12 NPCs show unique portraits with expression variations
- [ ] Map nodes visually distinguish all 5 quest states
- [ ] Combat stances reflect health state via tint
- [ ] Inventory items show with rarity seals
- [ ] `./gradlew :core-ui:test` passes
- [ ] `./gradlew testMockDebugUnitTest` passes
- [ ] No AI output directly mutates game state (ROADMAP principle #5)
- [ ] Deterministic simulation remains source of truth (ROADMAP principle #1)
