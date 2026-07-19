package com.chimera.core.ui.sprites

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chimera.core.data.sprites.SpriteLoader
import com.chimera.core.model.sprites.*
import com.chimera.core.ui.theme.ChimeraSpritePalette
import timber.log.Timber

/**
 * CompositionLocal for the app-wide SpriteLoader.
 * Provide it once near the root (e.g. in ChimeraTheme or the activity):
 *
 *   CompositionLocalProvider(LocalSpriteLoader provides spriteLoader) { ... }
 *
 * A per-composition fallback loader is created if none is provided (fine for
 * previews; in production always provide the Hilt singleton so the LRU cache
 * is shared).
 */
val LocalSpriteLoader = staticCompositionLocalOf<SpriteLoader?> { null }

/**
 * CompositionLocal reduced-motion flag. Wire it to the user's system/app
 * accessibility preference; when true, pulse/ink animations render statically.
 * (ROADMAP Workstream F: motion effects can be disabled or reduced.)
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Gothic manuscript sprite renderer with ink-wash atmosphere support.
 *
 * v2 (2026-07-14) changes vs. the Drive drop:
 * - Depends on [SpriteResolver]/[SpriteLoader] via parameters + CompositionLocal
 *   instead of constructing loaders ad hoc.
 * - FIX: palette values are Compose Colors; removed invalid `.toInt()` calls.
 * - FIX: fallback text sizing no longer reads `modifier.height()` (not a thing);
 *   fallbacks take an explicit [fallbackSize] and derive font size from it.
 * - Added missing imports (RoundedCornerShape, Stroke).
 * - Honors [LocalReducedMotion].
 */
@Composable
fun ChimeraSprite(
    spriteRef: SpriteRef?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    opacity: Float = 1.0f,
    inkWashEffect: Boolean = false,
    fallbackSize: Dp = 48.dp,
    onLoadError: (() -> Unit)? = null
) {
    if (spriteRef == null) {
        SpriteFallback(
            modifier = modifier,
            fallbackSize = fallbackSize,
            contentDescription = contentDescription ?: "Missing sprite asset"
        )
        return
    }

    val context = LocalContext.current
    val spriteLoader = LocalSpriteLoader.current ?: remember { SpriteLoader(context) }

    var bitmap by remember(spriteRef.id) { mutableStateOf<ImageBitmap?>(null) }
    var loadError by remember(spriteRef.id) { mutableStateOf(false) }

    LaunchedEffect(spriteRef.id) {
        try {
            bitmap = spriteLoader.load(spriteRef)
            if (bitmap == null) {
                loadError = true
                onLoadError?.invoke()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load sprite: ${spriteRef.id}")
            loadError = true
            onLoadError?.invoke()
        }
    }

    if (loadError || bitmap == null) {
        SpriteFallback(
            modifier = modifier,
            fallbackSize = fallbackSize,
            contentDescription = contentDescription ?: "Failed to load ${spriteRef.id}"
        )
        return
    }

    Box(
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        }
    ) {
        Image(
            bitmap = bitmap!!,
            contentDescription = null, // Handled at Box level
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

        ParchmentGrainOverlay(modifier = Modifier.fillMaxSize())
    }
}

/**
 * NPC Portrait with expression support and graceful fallback.
 * Maps to ROADMAP Workstream C: NPC portrait and emotional telemetry.
 */
@Composable
fun NpcPortraitSprite(
    npcId: String,
    resolver: SpriteResolver,
    expression: PortraitExpression = PortraitExpression.NEUTRAL,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier,
    fallbackInitials: String = npcId.take(2).uppercase()
) {
    val spriteRef = remember(npcId, expression, resolver) {
        resolver.resolveNpcPortrait(npcId, expression)
    }

    val tint = remember(expression) {
        if (expression != PortraitExpression.NEUTRAL) {
            Color(expression.tintColorHex).copy(alpha = 0.15f) // Subtle tint overlay
        } else {
            Color.Unspecified
        }
    }

    val inkWash = expression.inkWashIntensity > 0.3f

    if (spriteRef != null) {
        ChimeraSprite(
            spriteRef = spriteRef,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, ChimeraSpritePalette.INK_GREY, RoundedCornerShape(4.dp)),
            contentDescription = "Portrait of $npcId, ${expression.expressionName}",
            tint = tint,
            inkWashEffect = inkWash,
            fallbackSize = size
        )
    } else {
        LetterAvatarFallback(
            initials = fallbackInitials,
            size = size,
            modifier = modifier
        )
    }
}

/**
 * Overhead map token for NPCs on the world map.
 */
@Composable
fun NpcMapToken(
    npcId: String,
    resolver: SpriteResolver,
    questState: MapNodeState = MapNodeState.ACTIVE,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val tokenRef = remember(npcId, resolver) {
        resolver.resolveNpcToken(npcId)
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        ChimeraSprite(
            spriteRef = tokenRef,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "$npcId location marker",
            opacity = questState.defaultOpacity,
            fallbackSize = size
        )

        QuestStateRing(
            state = questState,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Map node sprite with quest-state visualization.
 * Maps to ROADMAP Workstream A: Map quest markers.
 *
 * Active nodes pulse (disabled under LocalReducedMotion); hidden/blocked
 * nodes render at reduced opacity via MapNodeState.defaultOpacity.
 */
@Composable
fun MapNodeSprite(
    nodeType: String,
    state: MapNodeState,
    resolver: SpriteResolver,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val spriteRef = remember(nodeType, state, resolver) {
        resolver.resolveMapNode(nodeType, state)
    }

    val reducedMotion = LocalReducedMotion.current
    val shouldPulse = state.pulses && !reducedMotion

    val infiniteTransition = rememberInfiniteTransition(label = "node_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (shouldPulse) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        ChimeraSprite(
            spriteRef = spriteRef,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Map node $nodeType, ${state.stateName}",
            opacity = state.defaultOpacity,
            fallbackSize = size
        )

        if (state != MapNodeState.NEUTRAL) {
            QuestStateRing(
                state = state,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Combat stance sprite with health-state awareness.
 * Integrates with DuelEngine stance names and resolve attrition.
 */
@Composable
fun CombatStanceSprite(
    stanceName: String,
    isPlayer: Boolean,
    resolvePercent: Float,  // 0.0f - 1.0f
    resolver: SpriteResolver,
    size: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    val wounded = resolvePercent < 0.4f
    val spriteRef = remember(stanceName, isPlayer, wounded, resolver) {
        resolver.resolveCombatStance(
            isPlayer = isPlayer,
            stanceName = stanceName,
            wounded = wounded
        )
    }

    val tint = when {
        resolvePercent < 0.2f -> ChimeraSpritePalette.TINT_WOUNDED.copy(alpha = 0.3f)
        resolvePercent < 0.5f -> ChimeraSpritePalette.TINT_TENSE.copy(alpha = 0.2f)
        else -> Color.Unspecified
    }

    ChimeraSprite(
        spriteRef = spriteRef,
        modifier = modifier.size(size),
        contentDescription = "${if (isPlayer) "Player" else "Opponent"} in $stanceName stance",
        tint = tint,
        inkWashEffect = resolvePercent < 0.3f,
        opacity = 0.5f + (resolvePercent.coerceIn(0f, 1f) * 0.5f), // Fade as health drops
        fallbackSize = size
    )
}

/**
 * Camp inventory item with rarity seal overlay.
 * Maps to ROADMAP Workstream D: Inventory polish.
 * Rarity is announced in the content description (never color-only).
 */
@Composable
fun InventoryItemSprite(
    itemId: String,
    resolver: SpriteResolver,
    rarity: ItemRarity = ItemRarity.COMMON,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val spriteRef = remember(itemId, resolver) {
        resolver.resolveCampItem(itemId)
    }

    Box(modifier = modifier.size(size)) {
        ChimeraSprite(
            spriteRef = spriteRef,
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Item: $itemId, ${rarity.displayName}",
            fallbackSize = size
        )

        RaritySeal(
            rarity = rarity,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(16.dp)
        )
    }
}

// --- Item Rarity ---

enum class ItemRarity(
    val displayName: String,
    val sealColorHex: Long,
    val displayColorHex: Long
) {
    COMMON("Common", 0xFF6B6B6B, 0xFF9E9E9E),
    UNCOMMON("Uncommon", 0xFF2E7D32, 0xFF4CAF50),
    RARE("Rare", 0xFF1565C0, 0xFF2196F3),
    EPIC("Epic", 0xFF6A1B9A, 0xFF9C27B0),
    LEGENDARY("Legendary", 0xFFB8941F, 0xFFFFD700)
}

// --- Internal Components ---

/**
 * Ink wash overlay effect for wounded/hostile states.
 * Static (no phase animation) under LocalReducedMotion.
 */
@Composable
internal fun InkWashOverlay(modifier: Modifier = Modifier) {
    val reducedMotion = LocalReducedMotion.current

    val phase: Float = if (reducedMotion) {
        0.5f
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "ink")
        val animated by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ink_phase"
        )
        animated
    }

    Canvas(modifier = modifier) {
        val gradient = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                ChimeraSpritePalette.INK_CHARCOAL.copy(alpha = 0.1f + 0.05f * phase),
                ChimeraSpritePalette.INK_BLACK.copy(alpha = 0.15f)
            ),
            center = center,
            radius = size.minDimension * 0.6f
        )
        drawRect(brush = gradient)
    }
}

/**
 * Subtle parchment grain texture overlay.
 */
@Composable
internal fun ParchmentGrainOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 0.5f
        val spacing = 4f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = ChimeraSpritePalette.PARCHMENT_DARK.copy(alpha = 0.03f),
                start = Offset(0f, y),
                end = Offset(size.width, y + (spacing * 0.3f)),
                strokeWidth = strokeWidth
            )
            y += spacing
        }
    }
}

/**
 * Fallback composable when a sprite asset is missing.
 * ROADMAP: "Portrait fallback looks intentional when portraitResName is null"
 */
@Composable
internal fun SpriteFallback(
    modifier: Modifier = Modifier,
    fallbackSize: Dp = 48.dp,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .background(ChimeraSpritePalette.PARCHMENT_DARK)
            .border(1.dp, ChimeraSpritePalette.INK_GREY)
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        contentAlignment = Alignment.Center
    ) {
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
            fontSize = (fallbackSize.value * 0.4f).sp,
            fontFamily = FontFamily.Serif
        )
    }
}

/**
 * Letter avatar fallback for NPC portraits without assets.
 */
@Composable
internal fun LetterAvatarFallback(
    initials: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(4.dp))
            .background(ChimeraSpritePalette.PARCHMENT_MID)
            .border(1.dp, ChimeraSpritePalette.INK_GREY, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.take(2).uppercase(),
            color = ChimeraSpritePalette.INK_GREY,
            fontSize = (size.value * 0.35f).sp,
            fontFamily = FontFamily.Serif
        )
    }
}

/**
 * Quest state indicator ring drawn around map tokens.
 */
@Composable
internal fun QuestStateRing(
    state: MapNodeState,
    modifier: Modifier = Modifier
) {
    val color = remember(state) { Color(state.ringColorHex) }

    val alpha = when (state) {
        MapNodeState.ACTIVE -> 0.8f
        MapNodeState.HIDDEN -> 0.3f
        else -> 0.5f
    }

    Canvas(modifier = modifier) {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = (size.minDimension / 2f) - 2f,
            style = Stroke(width = 2f),
            center = center
        )
    }
}

/**
 * Rarity seal indicator for inventory items.
 */
@Composable
internal fun RaritySeal(
    rarity: ItemRarity,
    modifier: Modifier = Modifier
) {
    val color = remember(rarity) { Color(rarity.sealColorHex) }

    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = size.minDimension / 2f,
            center = center
        )
        // Inner highlight
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = (size.minDimension / 2f) * 0.6f,
            center = center
        )
    }
}
