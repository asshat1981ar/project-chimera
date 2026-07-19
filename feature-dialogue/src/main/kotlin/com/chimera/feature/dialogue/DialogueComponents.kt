package com.chimera.feature.dialogue

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.chimera.core.model.sprites.SpriteResolver
import com.chimera.ui.components.ManuscriptCard
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

/**
 * Single dialogue transcript bubble. Player lines align right with an EmberGold
 * accent; NPC lines align left with a VoidGreen accent. Non-neutral NPC emotions
 * are shown beside the speaker name.
 */
@Composable
internal fun DialogueBubble(line: DialogueLine) {
    val alignment = if (line.isPlayer) Alignment.End else Alignment.Start
    val borderColor = remember(line.isPlayer) {
        if (line.isPlayer) EmberGold.copy(alpha = 0.4f) else VoidGreen.copy(alpha = 0.4f)
    }
    val nameColor = if (line.isPlayer) EmberGold else HollowCrimson

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = line.speakerName,
                style = MaterialTheme.typography.labelMedium,
                color = nameColor
            )
            if (!line.isPlayer && line.emotion != "neutral") {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = line.emotion,
                    style = MaterialTheme.typography.labelSmall,
                    color = FadedBone.copy(alpha = 0.6f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        ManuscriptCard(
            fillColor = MaterialTheme.colorScheme.surfaceVariant,
            borderColor = borderColor,
            borderWidth = 1.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Three animated vertical bars that pulse out of phase — a minimal "speaking" indicator.
 * Renders in EmberGold so it matches the rest of the dialogue chrome.
 */
@Composable
internal fun SpeakingWaveIcon(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "wave")
    val barColor = EmberGold

    // Each bar gets a staggered delay so they ripple left-to-right
    val h1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "bar1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(420, 140, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "bar2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(420, 280, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "bar3"
    )

    Canvas(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Speaking indicator"
        }
    ) {
        val barW = size.width / 5f          // 3 bars + 2 gaps in 5 equal parts
        val gap  = barW                     // gap == bar width
        val maxH = size.height
        val barHeights = listOf(h1, h2, h3)

        barHeights.forEachIndexed { i, fraction ->
            val barH  = maxH * fraction
            val left  = i * (barW + gap)
            val top   = (maxH - barH) / 2f
            drawRoundRect(
                color        = barColor,
                topLeft      = androidx.compose.ui.geometry.Offset(left, top),
                size         = androidx.compose.ui.geometry.Size(barW, barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f)
            )
        }
    }
}

/**
 * Side-effect wiring for the dialogue screen: duel trigger, scene-completion
 * events, and cinematic auto-advance. Extracted to keep [DialogueSceneScreen]
 * under the cyclomatic-complexity and length thresholds.
 */
@Composable
internal fun DialogueSceneEffects(
    uiState: DialogueUiState,
    viewModel: DialogueSceneViewModel,
    onSceneComplete: (nextSceneId: String?) -> Unit,
    onTriggerDuel: (opponentId: String) -> Unit
) {
    LaunchedEffect(uiState.triggerDuelWith) {
        uiState.triggerDuelWith?.let { opponentId -> onTriggerDuel(opponentId) }
    }
    LaunchedEffect(Unit) {
        viewModel.sceneCompleteEvent.collect { nextSceneId -> onSceneComplete(nextSceneId) }
    }
    LaunchedEffect(uiState.cinematicIndex, uiState.autoAdvanceTimerMs) {
        if (uiState.isCinematic && uiState.autoAdvanceTimerMs > 0) {
            kotlinx.coroutines.delay(uiState.autoAdvanceTimerMs)
            viewModel.advanceCinematic()
        }
    }
}

/**
 * Layout body for the dialogue screen: header, relationship banner, transcript
 * (standard or cinematic), quick intents, composer, and advance hint. Owns the
 * text-input state and send logic for the dialogue composer.
 */
@Composable
internal fun DialogueSceneContent(
    uiState: DialogueUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSceneComplete: (nextSceneId: String?) -> Unit,
    viewModel: DialogueSceneViewModel,
    spriteResolver: SpriteResolver
) {
    val onClose = { onSceneComplete(null) }
    var typedInput by remember { mutableStateOf("") }
    val sendInput = {
        if (typedInput.isNotBlank()) { viewModel.submitTypedInput(typedInput); typedInput = "" }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(
            if (uiState.isCinematic) Color(0xFF0D0B0B) else MaterialTheme.colorScheme.background
        )
    ) {
        if (uiState.isCinematic) {
            CinematicHeader(sceneTitle = uiState.sceneTitle, onClose = onClose)
        } else {
            DialogueHeader(uiState = uiState, spriteResolver = spriteResolver, onClose = onClose)
        }

        RelationshipBanner(banner = uiState.relationshipBanner, onDismiss = { viewModel.dismissRelationshipBanner() })

        if (uiState.isCinematic) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CinematicNarration(
                    lines = uiState.transcript,
                    currentIndex = uiState.cinematicIndex,
                    totalLines = uiState.transcript.size,
                    onTapNext = { viewModel.advanceCinematic() }
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.transcript) { line -> DialogueBubble(line = line) }
            }
        }

        if (uiState.quickIntents.isNotEmpty() && !uiState.isLoading && !uiState.isCinematic) {
            QuickIntentsRow(intents = uiState.quickIntents, onSelectIntent = { viewModel.selectIntent(it) })
        }

        if (!uiState.isSceneComplete && !uiState.isCinematic) {
            DialogueComposer(
                typedInput = typedInput,
                onTypedInput = { typedInput = it },
                onSend = sendInput,
                isEnabled = !uiState.isLoading
            )
        }

        if (uiState.isCinematic && !uiState.isSceneComplete && uiState.autoAdvanceTimerMs > 0) {
            CinematicAdvanceHint()
        }
    }
}
