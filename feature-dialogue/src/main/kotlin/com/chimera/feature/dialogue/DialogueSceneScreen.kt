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
import androidx.compose.ui.input.pointer.detectTapGestures
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DialogueSceneScreen(
    sceneId: String,
    onSceneComplete: () -> Unit,
    onTriggerDuel: (opponentId: String) -> Unit = {},
    viewModel: DialogueSceneViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to duel when triggered
    LaunchedEffect(uiState.triggerDuelWith) {
        uiState.triggerDuelWith?.let { opponentId ->
            onTriggerDuel(opponentId)
        }
    }

    // Auto-advance timer for cinematic scenes
    LaunchedEffect(uiState.cinematicIndex, uiState.autoAdvanceTimerMs) {
        if (uiState.isCinematic && uiState.autoAdvanceTimerMs > 0) {
            kotlinx.coroutines.delay(uiState.autoAdvanceTimerMs)
            viewModel.advanceCinematic()
        }
    }

    val listState = rememberLazyListState()
    var typedInput by remember { mutableStateOf("") }
    val typedInputState = rememberUpdatedState(typedInput)
    val sendMessage = remember {
        {
            if (typedInputState.value.isNotBlank()) {
                viewModel.submitTypedInput(typedInputState.value)
                typedInput = ""
            }
        }
    }

    LaunchedEffect(uiState.transcript.size) {
        if (uiState.transcript.isNotEmpty()) {
            listState.animateScrollToItem(uiState.transcript.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (uiState.isCinematic) Color(0xFF0D0B0B)
                else MaterialTheme.colorScheme.background
            )
    ) {
        // Cinematic mode: full-screen overlay with minimal chrome
        if (uiState.isCinematic) {
            CinematicHeader(
                sceneTitle = uiState.sceneTitle,
                onClose = onSceneComplete
            )
        } else {
            // Standard dialogue header
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
                        onClick = onSceneComplete
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Leave scene", tint = FadedBone)
                    }
                    // NPC portrait with live disposition ring
                    com.chimera.ui.components.NpcPortrait(
                        npcId = uiState.npcId.ifBlank { uiState.npcName },
                        npcName = uiState.npcName,
                        disposition = uiState.npcDisposition,
                        archetype = uiState.npcArchetype,
                        portraitResName = uiState.npcPortraitResName,
                        size = 40.dp,
                        contentDescription = "${uiState.npcName} portrait"
                    )
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

        // Relationship banner
        AnimatedVisibility(
            visible = uiState.relationshipBanner != null,
            enter = slideInVertically() + fadeIn(),
            exit = fadeOut()
        ) {
            uiState.relationshipBanner?.let { banner ->
                Surface(
                    color = if (banner.delta > 0) VoidGreen.copy(alpha = 0.2f) else HollowCrimson.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${banner.npcName}: ${if (banner.delta > 0) "+" else ""}${String.format("%.0f", banner.delta * 100)}%",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = if (banner.delta > 0) VoidGreen else HollowCrimson,
                        textAlign = TextAlign.Center
                    )
                }

                LaunchedEffect(banner) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.dismissRelationshipBanner()
                }
            }
        }

        // Transcript / Cinematic display
        if (uiState.isCinematic) {
            // Cinematic mode: center the current narration line
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
            // Standard dialogue mode
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.transcript) { line ->
                    DialogueBubble(line = line)
                }
            }
        }

        // Quick intents (hidden in cinematic mode)
        if (uiState.quickIntents.isNotEmpty() && !uiState.isLoading && !uiState.isCinematic) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.quickIntents.forEach { intent ->
                        AssistChip(
                            modifier = Modifier.testTag("btn_intent_${intent.take(20).lowercase().replace(" ", "_")}"),
                            onClick = { viewModel.selectIntent(intent) },
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

        // Text input composer (hidden in cinematic mode)
        if (!uiState.isSceneComplete && !uiState.isCinematic) {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ParchmentInputField(
                        value = typedInput,
                        onValueChange = { typedInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("field_dialogue_input"),
                        placeholder = "Speak your mind...",
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier.testTag("btn_send_dialogue"),
                        onClick = { sendMessage() },
                        enabled = typedInput.isNotBlank() && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send dialogue", tint = EmberGold)
                    }
                }
            }
        }

        // Cinematic tap-to-advance hint
        if (uiState.isCinematic && !uiState.isSceneComplete && uiState.autoAdvanceTimerMs > 0) {
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
    }
}

@Composable
private fun DialogueBubble(line: DialogueLine) {
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
private fun SpeakingWaveIcon(modifier: Modifier = Modifier) {
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
 * Minimal header for cinematic scenes - just the scene title and close button.
 */
@Composable
private fun CinematicHeader(
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
private fun CinematicNarration(
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
