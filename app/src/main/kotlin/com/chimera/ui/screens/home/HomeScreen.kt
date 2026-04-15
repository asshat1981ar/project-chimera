package com.chimera.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson

@Composable
fun HomeScreen(
    onEnterScene: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (uiState.playerName.isNotBlank()) {
                "Welcome back, ${uiState.playerName}"
            } else {
                "Welcome, Wanderer"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = EmberGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Chapter: ${uiState.chapterTag.replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodyLarge,
            color = FadedBone
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Main story CTA
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, HollowCrimson.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Continue Your Journey",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The Hollow awaits. Shadows stir where the king once sat.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onEnterScene("prologue_scene_1") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HollowCrimson
                    )
                ) {
                    Text("Enter the Hollow")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Active vows reminder (empty for Milestone A)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Active Vows",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmberGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No vows sworn yet. Your choices will forge them.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FadedBone
                )
            }
        }
    }
}
