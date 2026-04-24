package com.chimera.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.data.AiMode
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.testTag("btn_back_settings"),
                onClick = onBack
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = FadedBone)
            }
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = EmberGold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display section
        SettingsSection("Display") {
            SliderSetting(
                label = "Text Size",
                value = settings.textScale,
                valueLabel = "${(settings.textScale * 100).toInt()}%",
                range = 0.8f..1.5f,
                onValueChange = viewModel::setTextScale
            )
            ToggleSetting(
                label = "Reduce Motion",
                description = "Disable animations and transitions",
                checked = settings.reduceMotion,
                onToggle = viewModel::setReduceMotion
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI section
        SettingsSection("AI Dialogue") {
            ToggleSetting(
                label = "AI Mode",
                description = if (settings.aiMode == AiMode.AUTO) "Auto (cloud when available)" else "Offline only (authored templates)",
                checked = settings.aiMode == AiMode.OFFLINE_ONLY,
                onToggle = { viewModel.toggleAiMode() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Audio section
        SettingsSection("Audio") {
            ToggleSetting(
                label = "NPC Voice",
                description = "Speak NPC dialogue lines aloud using on-device TTS",
                checked = settings.voiceEnabled,
                onToggle = viewModel::setVoiceEnabled
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cloud section
        SettingsSection("Cloud") {
            ToggleSetting(
                label = "Cloud Save Sync",
                description = "Restore newer saves from cloud when loading a slot",
                checked = settings.cloudSyncEnabled,
                onToggle = viewModel::setCloudSyncEnabled
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Privacy section
        SettingsSection("Privacy") {
            ToggleSetting(
                label = "Analytics",
                description = "Help improve the game with anonymous usage data",
                checked = settings.analyticsOptIn,
                onToggle = viewModel::setAnalyticsOptIn
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Version info
        Text(
            "Chimera v1.0.0-alpha",
            style = MaterialTheme.typography.bodySmall,
            color = FadedBone,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = EmberGold)
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ToggleSetting(
    label: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall, color = FadedBone)
        }
        Switch(
            modifier = Modifier.testTag("switch_${label.lowercase().replace(" ", "_")}"),
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EmberGold,
                checkedTrackColor = HollowCrimson
            )
        )
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(valueLabel, style = MaterialTheme.typography.labelMedium, color = EmberGold)
        }
        Slider(
            modifier = Modifier.testTag("slider_${label.lowercase().replace(" ", "_")}"),
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = EmberGold,
                activeTrackColor = HollowCrimson
            )
        )
    }
}
