package com.chimera.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.components.GothicButton
import com.chimera.ui.components.ManuscriptCard
import com.chimera.ui.components.ObjectiveChip
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import androidx.compose.runtime.LaunchedEffect
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen
import com.chimera.model.ObjectivePrimaryAction

// File-level Modifier constants for reusable chains
private val FillMaxSizeWithPadding = Modifier.fillMaxSize().padding(horizontal = 24.dp)
private val DefaultOnActTransition: (String) -> Unit = {}

@Composable
fun HomeScreen(
    onEnterScene: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onActTransition: (String) -> Unit = DefaultOnActTransition,
    onPrimaryObjectiveAction: (ObjectivePrimaryAction, relatedId: String?) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Fire act-transition interstitial exactly once per chapter advance
    LaunchedEffect(uiState.pendingActTransition) {
        uiState.pendingActTransition?.let { actTag ->
            onActTransition(actTag)
            viewModel.clearActTransition()
        }
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) { CircularProgressIndicator(color = EmberGold) }
        return
    }

    LazyColumn(
        modifier = FillMaxSizeWithPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(32.dp)) }

        // ── Greeting + chapter ───────────────────────────────────────────────
        item {
            Column {
                Text(
                    text = if (uiState.playerName.isNotBlank())
                        "Welcome back, ${uiState.playerName}"
                    else "Welcome, Wanderer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmberGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.chapterTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FadedBone
                )
                if (uiState.completedSceneCount > 0) {
                    Text(
                        text = "${uiState.completedSceneCount} scenes explored",
                        style = MaterialTheme.typography.labelSmall,
                        color = DimAsh
                    )
                }
            }
        }

        // ── Continue CTA ─────────────────────────────────────────────────────
        item {
            ManuscriptCard(
                modifier = Modifier.fillMaxWidth(),
                fillColor = MaterialTheme.colorScheme.surface,
                borderColor = HollowCrimson.copy(alpha = 0.5f),
                borderWidth = 1.dp,
                contentPadding = 24.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoStories,
                        contentDescription = "Auto stories",
                        tint = EmberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Continue Your Journey",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                uiState.continueSceneTitle?.let { sceneTitle ->
                    Text(
                        text = sceneTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FadedBone,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } ?: Text(
                    text = "The Hollow awaits. Shadows stir where the king once sat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FadedBone
                )

                Spacer(modifier = Modifier.height(16.dp))

                GothicButton(
                    onClick = {
                        val target = uiState.continueSceneId ?: "prologue_scene_1"
                        onEnterScene(target)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_enter_hollow")
                ) {
                    Text("Enter the Hollow")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ── Active vows ───────────────────────────────────────────────────────
        item {
            ManuscriptCard(
                modifier = Modifier.fillMaxWidth(),
                fillColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                borderWidth = 1.dp,
                contentPadding = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = HollowCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Active Vows",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmberGold
                        )
                    }
                    if (uiState.activeVowCount > 0) {
                        Badge(containerColor = HollowCrimson) {
                            Text("${uiState.activeVowCount}")
                        }
                    }
                }
                if (uiState.activeVowCount == 0) {
                    Text(
                        text = "No vows sworn yet. Your choices will forge them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FadedBone,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                    )
                }
            }
        }

        // ── Active Objectives HUD ─────────────────────────────────────────────
        if (uiState.objectives.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current Objectives",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmberGold
                    )
                    uiState.objectives.forEach { obj ->
                        ObjectiveChip(
                            summary = obj,
                            onPrimaryAction = {
                                onPrimaryObjectiveAction(obj.primaryAction, obj.relatedLocationId ?: obj.relatedNpcId)
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
