package com.chimera.ui.screens.duel

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.core.engine.CombatEngine
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun DuelScreen(
    onDuelComplete: () -> Unit,
    viewModel: DuelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmberGold)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDuelComplete) {
                Icon(Icons.Default.ArrowBack, "Leave", tint = FadedBone)
            }
            Spacer(Modifier.width(6.dp))
            Column {
                Text("COMBAT", style = MaterialTheme.typography.labelSmall, color = EmberGold)
                Text(uiState.opponentName,
                    style = MaterialTheme.typography.titleLarge, color = FadedBone)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Resolve bars ──────────────────────────────────────────────────────
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, DimAsh.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ResolveRow(
                    label       = "${uiState.opponentName}'s Resolve",
                    current     = uiState.opponentResolve,
                    max         = CombatEngine.MAX_RESOLVE,
                    activeColor = HollowCrimson
                )
                Spacer(Modifier.height(10.dp))
                ResolveRow(
                    label       = "Your Resolve",
                    current     = uiState.playerResolve,
                    max         = CombatEngine.MAX_RESOLVE,
                    activeColor = VoidGreen
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Exchange ${uiState.rollCount} / ${CombatEngine.MAX_ROLLS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = DimAsh,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Phase content (animated transitions) ─────────────────────────────
        AnimatedContent(
            targetState = uiState.phase,
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn()) togetherWith
                (slideOutVertically { -it / 3 } + fadeOut())
            },
            label = "combat_phase"
        ) { phase ->
            when (phase) {
                CombatPhase.INTENT    -> IntentPhase(uiState, viewModel::executeIntent)
                CombatPhase.RESOLVING -> ResolvingPhase(uiState, viewModel::acknowledgeResult)
                CombatPhase.COMPLETE  -> CompletePhase(uiState, onDuelComplete)
            }
        }
    }
}

// ── Intent phase — pick a card ────────────────────────────────────────────────

@Composable
private fun IntentPhase(
    uiState: DuelUiState,
    onIntentSelected: (CombatEngine.IntentCard) -> Unit
) {
    Column {
        Text(
            "Choose your intent:",
            style = MaterialTheme.typography.titleMedium,
            color = EmberGold
        )
        Spacer(Modifier.height(12.dp))

        if (uiState.availableIntents.isEmpty()) {
            Text("No intents available.", style = MaterialTheme.typography.bodyMedium, color = DimAsh)
        } else {
            uiState.availableIntents.forEach { intent ->
                IntentCard(intent = intent, onClick = { onIntentSelected(intent) })
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun IntentCard(
    intent: CombatEngine.IntentCard,
    onClick: () -> Unit
) {
    val bonusColor = if (intent.statBonus >= 0) VoidGreen else HollowCrimson
    val bonusText  = if (intent.statBonus >= 0) "+${intent.statBonus}" else "${intent.statBonus}"

    OutlinedButton(
        onClick    = onClick,
        modifier   = Modifier.fillMaxWidth(),
        border     = BorderStroke(1.dp, EmberGold.copy(alpha = 0.4f)),
        colors     = ButtonDefaults.outlinedButtonColors(contentColor = FadedBone),
        shape      = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(intent.label, style = MaterialTheme.typography.titleSmall, color = EmberGold)
                Text(intent.description, style = MaterialTheme.typography.bodySmall, color = DimAsh)
            }
            Spacer(Modifier.width(12.dp))
            Text(bonusText, style = MaterialTheme.typography.titleMedium, color = bonusColor)
        }
    }
}

// ── Resolving phase — show roll result ────────────────────────────────────────

@Composable
private fun ResolvingPhase(
    uiState: DuelUiState,
    onContinue: () -> Unit
) {
    val result = uiState.lastResult ?: return

    val (bandColor, bandBg) = when (result.band) {
        CombatEngine.ResultBand.CRITICAL_SUCCESS -> EmberGold to EmberGold.copy(alpha = 0.12f)
        CombatEngine.ResultBand.SUCCESS          -> VoidGreen to VoidGreen.copy(alpha = 0.10f)
        CombatEngine.ResultBand.PARTIAL          -> FadedBone to FadedBone.copy(alpha = 0.08f)
        CombatEngine.ResultBand.FAILURE          -> HollowCrimson to HollowCrimson.copy(alpha = 0.10f)
        CombatEngine.ResultBand.CRITICAL_FAILURE -> HollowCrimson to HollowCrimson.copy(alpha = 0.18f)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Die face
        Card(
            modifier = Modifier.size(100.dp),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border   = BorderStroke(1.dp, EmberGold.copy(alpha = 0.6f))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.roll}", style = MaterialTheme.typography.displaySmall, color = EmberGold)
                    Text(
                        if (result.modifier >= 0) "+${result.modifier} mod = ${result.total}"
                        else "${result.modifier} mod = ${result.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DimAsh
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Band label
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bandBg, shape = MaterialTheme.shapes.medium)
                .padding(vertical = 10.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                result.band.label.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = bandColor
            )
        }

        Spacer(Modifier.height(14.dp))

        // Narrative
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, DimAsh.copy(alpha = 0.25f))
        ) {
            Text(
                text = result.narrative,
                style = MaterialTheme.typography.bodyLarge,
                color = FadedBone,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(containerColor = HollowCrimson)
        ) {
            Text(if (uiState.isComplete) "See outcome" else "Continue")
        }
    }
}

// ── Complete phase — victory or defeat ───────────────────────────────────────

@Composable
private fun CompletePhase(
    uiState: DuelUiState,
    onReturn: () -> Unit
) {
    val won         = uiState.playerWon == true
    val accentColor = if (won) EmberGold else HollowCrimson

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            if (won) "Victory" else "Defeat",
            style = MaterialTheme.typography.displaySmall,
            color = accentColor
        )

        Spacer(Modifier.height(12.dp))

        Text(
            if (won) "${uiState.opponentName} yields. The encounter is over."
            else "You were overcome. ${uiState.opponentName} prevails.",
            style = MaterialTheme.typography.bodyLarge,
            color = FadedBone,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        uiState.log.lastOrNull()?.let { last ->
            Text(last.narrative, style = MaterialTheme.typography.bodyMedium,
                color = DimAsh, textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick  = onReturn,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Return to scene")
        }
    }
}

// ── Resolve bar ───────────────────────────────────────────────────────────────

@Composable
private fun ResolveRow(
    label: String,
    current: Int,
    max: Int,
    activeColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = DimAsh,
            modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in 1..max) {
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 12.dp)
                        .background(
                            color = if (i <= current) activeColor else DimAsh.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraSmall
                        )
                )
            }
        }
    }
}
