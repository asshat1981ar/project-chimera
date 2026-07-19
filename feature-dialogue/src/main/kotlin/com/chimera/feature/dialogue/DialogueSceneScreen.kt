package com.chimera.feature.dialogue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically

import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.background
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import com.chimera.ui.components.ManuscriptCard
import com.chimera.ui.components.ParchmentInputField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.core.model.sprites.EmptySpriteResolver
import com.chimera.core.model.sprites.PortraitExpression
import com.chimera.core.model.sprites.SpriteResolver
import com.chimera.core.ui.sprites.NpcPortraitSprite
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * v2 (2026-07-14, Workstream H Phase 0.5 / WU-02): sprite-wired dialogue screen.
 *
 * The header portrait now prefers the ink-wash sprite system: the NPC's live
 * disposition maps to a [PortraitExpression] (fromDisposition), and when
 * [spriteResolver] has a portrait for (npcId, expression) it renders via
 * [NpcPortraitSprite] — expression changes with the simulation. When no
 * sprite asset exists, the original [com.chimera.ui.components.NpcPortrait]
 * (with its disposition ring and drawn fallback) renders unchanged.
 *
 * Simulation contract unchanged: expression is a pure projection of
 * ViewModel state; nothing here writes back into the sim.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogueSceneScreen(
    onSceneComplete: (nextSceneId: String?) -> Unit,
    onTriggerDuel: (opponentId: String) -> Unit = {},
    spriteResolver: SpriteResolver = EmptySpriteResolver,
    viewModel: DialogueSceneViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(uiState.transcript.size) {
        if (uiState.transcript.isNotEmpty()) {
            listState.animateScrollToItem(uiState.transcript.size - 1)
        }
    }

    DialogueSceneEffects(uiState = uiState, viewModel = viewModel, onSceneComplete = onSceneComplete, onTriggerDuel = onTriggerDuel)

    DialogueSceneContent(
        uiState = uiState,
        listState = listState,
        onSceneComplete = onSceneComplete,
        viewModel = viewModel,
        spriteResolver = spriteResolver
    )
}

/**
 * Standard (non-cinematic) dialogue header: back button, NPC portrait (sprite
 * system first, legacy [com.chimera.ui.components.NpcPortrait] fallback),
 * scene title / NPC name+mood, AUTHORED badge, speaking-wave indicator, and
 * loading spinner.
 */
@Composable
internal fun DialogueHeader(
    uiState: DialogueUiState,
    spriteResolver: SpriteResolver,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.testTag("btn_back_scene"),
                onClick = onClose
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Leave scene", tint = FadedBone)
            }
            // NPC portrait: sprite system first (expression follows live
            // disposition), legacy NpcPortrait with disposition ring otherwise.
            DialogueHeaderPortrait(uiState = uiState, spriteResolver = spriteResolver)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(uiState.sceneTitle, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${uiState.npcName} — ${uiState.npcMood}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FadedBone
                )
            }
            if (uiState.isFallbackMode) {
                Text(
                    "AUTHORED",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmberGold.copy(alpha = 0.6f),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            // Animated wave bars — visible while TTS is speaking an NPC line
            AnimatedVisibility(
                visible = uiState.isSpeaking,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(300))
            ) {
                SpeakingWaveIcon(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(width = 20.dp, height = 16.dp)
                )
            }
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = EmberGold
                )
            }
        }
    }
}

/**
 * NPC portrait for the dialogue header. Prefers the sprite system (expression
 * follows live disposition); falls back to the legacy [NpcPortrait] with its
 * disposition ring when no sprite asset is available.
 */
@Composable
internal fun DialogueHeaderPortrait(
    uiState: DialogueUiState,
    spriteResolver: SpriteResolver
) {
    val headerNpcId = uiState.npcId.ifBlank { uiState.npcName }
    val expression = remember(uiState.npcDisposition) {
        PortraitExpression.fromDisposition(uiState.npcDisposition)
    }
    val hasPortraitSprite = remember(headerNpcId, expression, spriteResolver) {
        spriteResolver.resolveNpcPortrait(headerNpcId, expression) != null
    }

    if (hasPortraitSprite) {
        NpcPortraitSprite(
            npcId = headerNpcId,
            resolver = spriteResolver,
            expression = expression,
            size = 40.dp
        )
    } else {
        com.chimera.ui.components.NpcPortrait(
            npcId = headerNpcId,
            npcName = uiState.npcName,
            disposition = uiState.npcDisposition,
            archetype = uiState.npcArchetype,
            portraitResName = uiState.npcPortraitResName,
            size = 40.dp,
            contentDescription = "${uiState.npcName} portrait"
        )
    }
}

/**
 * Animated relationship-delta banner. Shows the NPC name and signed percentage
 * change, auto-dismisses after 2.5s via [onDismiss].
 */
@Composable
internal fun RelationshipBanner(
    banner: RelationshipBanner?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically() + fadeIn(),
        exit = fadeOut()
    ) {
        banner?.let {
            Surface(
                color = if (it.delta > 0) VoidGreen.copy(alpha = 0.2f) else HollowCrimson.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${it.npcName}: ${if (it.delta > 0) "+" else ""}${String.format(java.util.Locale.US, "%.0f", it.delta * 100)}%",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = if (it.delta > 0) VoidGreen else HollowCrimson,
                    textAlign = TextAlign.Center
                )
            }

            LaunchedEffect(it) {
                kotlinx.coroutines.delay(2500)
                onDismiss()
            }
        }
    }
}

/**
 * Row of quick-intent assist chips. Each chip is test-tagged with
 * `btn_intent_<sanitized-intent>` for UI automation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickIntentsRow(
    intents: List<String>,
    onSelectIntent: (String) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            intents.forEach { intent ->
                AssistChip(
                    modifier = Modifier.testTag("btn_intent_${intent.take(20).lowercase().replace(" ", "_")}"),
                    onClick = { onSelectIntent(intent) },
                    label = { Text(intent, style = MaterialTheme.typography.bodySmall) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        borderColor = EmberGold.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

/**
 * Text input composer: parchment input field + send button. The send button is
 * enabled only when [typedInput] is non-blank and [isEnabled] is true.
 */
@Composable
internal fun DialogueComposer(
    typedInput: String,
    onTypedInput: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ParchmentInputField(
                value = typedInput,
                onValueChange = onTypedInput,
                modifier = Modifier
                    .weight(1f)
                    .testTag("field_dialogue_input"),
                placeholder = "Speak your mind...",
                singleLine = true,
                enabled = isEnabled
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                modifier = Modifier.testTag("btn_send_dialogue"),
                onClick = onSend,
                enabled = typedInput.isNotBlank() && isEnabled
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send dialogue", tint = EmberGold)
            }
        }
    }
}

/**
 * Tap-to-advance hint shown at the bottom of cinematic scenes when auto-advance is active.
 */
@Composable
internal fun CinematicAdvanceHint() {
    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
        Text(
            text = "Tap to advance",
            style = MaterialTheme.typography.labelSmall,
            color = FadedBone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Minimal header for cinematic scenes - just the scene title and close button.
 */
@Composable
internal fun CinematicHeader(
    sceneTitle: String,
    onClose: () -> Unit
) {
    Surface(
        color = Color(0xFF1A1818),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Leave cinematic scene", tint = FadedBone)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = sceneTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = EmberGold
            )
        }
    }
}

/**
 * Full-screen cinematic narration display with fade-in animation and tap-to-advance.
 */
@Composable
internal fun CinematicNarration(
    lines: List<DialogueLine>,
    currentIndex: Int,
    totalLines: Int,
    onTapNext: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex) {
        visible = false
        kotlinx.coroutines.delay(200)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { onTapNext() }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Progress indicator (e.g., "1 / 5")
            Text(
                text = "${currentIndex + 1} / $totalLines",
                style = MaterialTheme.typography.labelSmall,
                color = DimAsh,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Current narration line with fade animation
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)),
                exit = fadeOut(tween(400))
            ) {
                Text(
                    text = lines.lastOrNull()?.text ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        lineHeight = 28.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Speaker name below the text
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)),
                exit = fadeOut(tween(300))
            ) {
                Text(
                    text = lines.lastOrNull()?.speakerName ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = EmberGold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
