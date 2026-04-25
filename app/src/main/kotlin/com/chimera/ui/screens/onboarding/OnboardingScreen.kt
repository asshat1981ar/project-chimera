package com.chimera.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chimera.data.ChimeraPreferences
import com.chimera.ui.components.GothicButton
import com.chimera.ui.components.ManuscriptCard
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferences: ChimeraPreferences? = null,
    onComplete: () -> Unit
) {
    var textScale by remember { mutableStateOf(1.0f) }
    var reduceMotion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "CHIMERA",
            style = MaterialTheme.typography.displayLarge,
            color = EmberGold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Ashes of the Hollow King",
            style = MaterialTheme.typography.headlineSmall,
            color = FadedBone,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Your words shape the world. NPCs remember your choices, " +
            "reinterpret promises, and unlock or block future paths " +
            "based on trust, fear, and faction pressure.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        ManuscriptCard(
            fillColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            borderWidth = 1.dp,
            contentPadding = 20.dp
        ) {
            Text("Accessibility", style = MaterialTheme.typography.titleMedium, color = EmberGold)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Text Size: ${(textScale * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = textScale,
                onValueChange = { textScale = it },
                valueRange = 0.8f..1.5f,
                colors = SliderDefaults.colors(thumbColor = EmberGold, activeTrackColor = HollowCrimson)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reduce Motion", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = reduceMotion,
                    onCheckedChange = { reduceMotion = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmberGold, checkedTrackColor = HollowCrimson)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        GothicButton(
            onClick = {
                scope.launch {
                    preferences?.setTextScale(textScale)
                    preferences?.setReduceMotion(reduceMotion)
                    preferences?.setTutorialComplete(true)
                    onComplete()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Begin Your Journey", style = MaterialTheme.typography.labelLarge)
        }
    }
}
