# Chimera Sprite System - Integration Guide

> Quick-start guide for wiring the 2D sprite system into the existing Chimera Android project.

---

## Step 1: Copy Files into Modules

### `core-model` (new package: `com.chimera.core.model.sprites`)
```
core-model/src/main/java/com/chimera/core/model/sprites/
├── SpriteId.kt
├── SpriteCategory.kt
├── PortraitExpression.kt
├── MapNodeState.kt
└── SpriteRef.kt
```

### `core-data` (new package: `com.chimera.core.data.sprites`)
```
core-data/src/main/java/com/chimera/core/data/sprites/
├── SpriteManifest.kt
└── SpriteLoader.kt
```

### `core-ui` (new package: `com.chimera.core.ui.sprites`)
```
core-ui/src/main/java/com/chimera/core/ui/sprites/
├── ChimeraSprite.kt
├── ChimeraSpritePalette.kt
└── SpriteTestFixtures.kt
```

### `app/src/main/assets/`
```
sprite_manifest.json
```

### `scripts/`
```
generate_sprites.py
```

---

## Step 2: Add to `core-ui` Theme

In `core-ui/src/main/java/com/chimera/core/ui/theme/ChimeraTheme.kt`, add the palette:

```kotlin
// Import at top
import com.chimera.core.ui.theme.ChimeraSpritePalette

// In theme composition, palette is available as object:
// ChimeraSpritePalette.PARCHMENT_DARK
// ChimeraSpritePalette.ACCENT_CRIMSON
// etc.
```

---

## Step 3: Initialize Manifest at App Startup

In `app/src/main/java/com/chimera/app/ChimeraApplication.kt`:

```kotlin
@HiltAndroidApp
class ChimeraApplication : Application() {
    @Inject lateinit var spriteManifest: SpriteManifest

    override fun onCreate() {
        super.onCreate()
        // ... existing init

        // Initialize sprite manifest
        applicationScope.launch {
            spriteManifest.initialize()
            Timber.i("Sprite manifest loaded: ${spriteManifest.totalSprites} sprites")
        }
    }
}
```

---

## Step 4: Wire into Feature Screens

### Dialogue Screen (`feature-dialogue`)

Replace the existing portrait placeholder:

```kotlin
// DialogueScreen.kt
@Composable
fun DialogueScreen(
    npcId: String,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val manifest: SpriteManifest = hiltViewModel()
    val expression by viewModel.currentExpression.collectAsState()

    Row(modifier = Modifier.fillMaxWidth()) {
        // NPC Portrait with live expression
        NpcPortraitSprite(
            npcId = npcId,
            expression = expression,
            manifest = manifest,
            modifier = Modifier.size(120.dp),
            fallbackInitials = npcId.take(2).uppercase()
        )

        // Dialogue content
        DialogueContent(
            npcId = npcId,
            viewModel = viewModel,
            modifier = Modifier.weight(1f)
        )
    }
}
```

### Map Screen (`feature-map`)

```kotlin
// MapScreen.kt
@Composable
fun MapNodeItem(
    node: MapNode,
    manifest: SpriteManifest
) {
    MapNodeSprite(
        nodeType = node.type,
        state = node.questState.toMapNodeState(),
        manifest = manifest,
        modifier = Modifier
            .size(48.dp)
            .clickable { onNodeSelected(node) }
    )
}
```

### Inventory Screen (`feature-camp`)

```kotlin
// InventoryScreen.kt
@Composable
fun InventoryGridItem(
    item: InventoryItem,
    manifest: SpriteManifest
) {
    InventoryItemSprite(
        itemId = item.id,
        rarity = item.rarity.toItemRarity(),
        manifest = manifest,
        modifier = Modifier.size(64.dp)
    )
}
```

### Combat Screen (`feature-duel` or `feature-combat`)

```kotlin
// CombatScreen.kt
@Composable
fun CombatArena(
    duelState: DuelEngine.DuelState,
    manifest: SpriteManifest
) {
    val playerResolvePercent = duelState.playerOmens / 4f // Normalize

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CombatStanceSprite(
            stanceName = "strike", // From player selection
            isPlayer = true,
            resolvePercent = playerResolvePercent,
            manifest = manifest
        )

        CombatStanceSprite(
            stanceName = duelState.lastOpponentStance?.name ?: "ward",
            isPlayer = false,
            resolvePercent = duelState.opponentResolve / 3f,
            manifest = manifest
        )
    }
}
```

---

## Step 5: Generate Assets

### Option A: AI Generation (Full Pipeline)

```bash
# Install dependencies
pip install Pillow requests

# Set your image generation API endpoint
export GENERATION_API="https://your-api.com/generate"

# Generate all assets
python scripts/generate_sprites.py \
    --manifest assets/sprite_manifest.json \
    --output app/src/main/res/ \
    --api $GENERATION_API

# Or generate by category
python scripts/generate_sprites.py --category NPC_PORTRAIT --api $GENERATION_API
python scripts/generate_sprites.py --category MAP_NODE --api $GENERATION_API
python scripts/generate_sprites.py --category COMBAT_PLAYER --api $GENERATION_API
```

### Option B: Placeholder + Manual

```bash
# Generate placeholders (no API needed)
python scripts/generate_sprites.py \
    --manifest assets/sprite_manifest.json \
    --output app/src/main/res/

# Replace placeholder PNGs with actual artwork manually
# Placeholders have borders + labels for identification
```

### Option C: AI Generation via This Tool

Use the `generate_image` tool with the prompt templates from the plan:

```
# NPC Portrait
Dark gothic manuscript portrait of [NPC DESCRIPTION],
sumi-e ink wash on aged parchment, [EXPRESSION],
medieval fantasy character, [FEATURES], [ATTIRE],
single candlelight from upper left, deep shadows,
ink bleed edges, monochrome with faint [ACCENT] accent,
isolated portrait, upper body, transparent background

# Map Node
Dark gothic architectural ink wash, birds-eye view of [LOCATION],
medieval fantasy map marker, [STATE DESCRIPTOR],
monochrome charcoal with faint [ACCENT] glow,
top-down perspective, game asset sprite, transparent background

# Combat Stance
Dark gothic ink wash figure in [STANCE] combat stance,
[SUBJECT], dynamic action pose frozen,
sumi-e brush strokes, motion blur effect,
monochrome with [HEALTH] edge tint,
isolated figure, transparent background

# Item
Dark gothic botanical illustration of [ITEM],
medieval still life, sumi-e ink with restrained [COLOR] wash,
detailed crosshatching, specimen drawing style,
square format, transparent background
```

---

## Step 6: Build & Test

```bash
# Clean build
./gradlew clean

# Compile check
./gradlew :core-model:compileKotlin
./gradlew :core-data:compileKotlin
./gradlew :core-ui:compileDebugKotlin

# Run tests
./gradlew :core-model:test
./gradlew :core-data:test
./gradlew :core-ui:testDebugUnitTest

# Full test suite
./gradlew testMockDebugUnitTest

# Verify no regressions
./gradlew :chimera-core:test
./gradlew assembleMockDebug
```

---

## Module Dependency Graph

```
                    feature-*
                        |
                    core-ui  <--- Sprite components (ChimeraSprite.kt)
                        |
                    core-data <--- Manifest + Loader (SpriteManifest.kt)
                        |
                    core-model <--- Data classes (SpriteId, SpriteRef, etc.)
```

**No new external dependencies.** The system uses:
- Kotlin stdlib (existing)
- Jetpack Compose (existing)
- Hilt DI (existing)
- kotlinx.serialization (existing in core-data)
- Android assets system (built-in)

---

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| `SpriteManifest` not found at runtime | Assets not in APK | `sprite_manifest.json` must be in `app/src/main/assets/` |
| Drawables show as missing | Wrong density folder | Use `drawable-xhdpi/` for 128px sprites |
| Ink wash overlay too heavy | Density scaling | Reduce alpha in `InkWashOverlay` composable |
| Portrait fallback shows "?" | Manifest entry mismatch | Check `id` field matches `resolveNpcPortrait()` key |
| Cache grows too large | High sprite count | Adjust `DEFAULT_CACHE_SIZE` in `SpriteLoader` |
| Gradle build slow | Many assets | Use `--dry-run` to batch-generate later |

---

## Acceptance Checklist

- [ ] `sprite_manifest.json` loads without errors
- [ ] All 12 NPCs show unique portraits (not "?" fallback)
- [ ] Expression changes reflect archetype events
- [ ] Map nodes pulse for active quests
- [ ] Combat stances tint red when wounded
- [ ] Inventory items show rarity seals
- [ ] `./gradlew :core-ui:testDebugUnitTest` passes
- [ ] `./gradlew testMockDebugUnitTest` passes
- [ ] `./gradlew assembleMockDebug` succeeds
- [ ] No new Android framework deps in `chimera-core`
- [ ] Accessibility descriptions on all sprites
- [ ] Works offline (no network dependency)
