package com.chimera.ui.screens.saveslot

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.components.GothicButton
import com.chimera.ui.components.ManuscriptCard
import com.chimera.ui.components.ParchmentInputField
import com.chimera.model.SaveSlot
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SaveSlotSelectScreen(
    onSlotSelected: (Long) -> Unit,
    viewModel: SaveSlotSelectViewModel = hiltViewModel()
) {
    val slots by viewModel.saveSlots.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    var showNewGameDialog by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf<SaveSlot?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Choose Your Path",
                style = MaterialTheme.typography.headlineLarge,
                color = EmberGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(slots, key = { it.slotIndex }) { slot ->
                    SaveSlotCard(
                        slot = slot,
                        onClick = {
                            if (!isRestoring) {
                                if (slot.isEmpty) {
                                    showNewGameDialog = slot.slotIndex
                                } else {
                                    viewModel.selectSlot(slot.id, onSlotSelected)
                                }
                            }
                        },
                        onLongClick = {
                            if (!slot.isEmpty && !isRestoring) {
                                showDeleteDialog = slot
                            }
                        }
                    )
                }
            }
        }

        // Cloud restore overlay — shown while downloading newer save from cloud
        if (isRestoring) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = EmberGold, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Restoring from cloud…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FadedBone
                    )
                }
            }
        }
    }

    // New game dialog
    showNewGameDialog?.let { slotIndex ->
        NewGameDialog(
            onConfirm = { name ->
                viewModel.createNewGame(slotIndex, name, onSlotSelected)
                showNewGameDialog = null
            },
            onDismiss = { showNewGameDialog = null }
        )
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { slot ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text(
                    "Destroy this save forever?",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    "The journey of ${slot.playerName} will be lost.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSave(slot.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Destroy", color = HollowCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Keep", color = FadedBone)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaveSlotCard(
    slot: SaveSlot,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ManuscriptCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        fillColor = MaterialTheme.colorScheme.surface,
        borderColor = if (slot.isEmpty) {
            MaterialTheme.colorScheme.outlineVariant
        } else {
            EmberGold.copy(alpha = 0.4f)
        },
        borderWidth = 1.dp
    ) {
        if (slot.isEmpty) {
            Text(
                text = "New Journey",
                style = MaterialTheme.typography.titleLarge,
                color = FadedBone,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Slot ${slot.slotIndex + 1}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = slot.playerName,
                style = MaterialTheme.typography.titleLarge,
                color = EmberGold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = com.chimera.ui.util.ChapterDisplayStrings.tagToTitle(slot.chapterTag),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formatPlaytime(slot.playtimeSeconds),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun NewGameDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Enter your name",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            ParchmentInputField(
                value = name,
                onValueChange = { if (it.length <= 24) name = it },
                singleLine = true,
                placeholder = "Wanderer"
            )
        },
        confirmButton = {
            GothicButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Begin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FadedBone)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}

private fun formatPlaytime(seconds: Long): String {
    if (seconds < 60) return "< 1m"
    val days    = seconds / 86400
    val hours   = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        days  > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${minutes}m"
        else      -> "${minutes}m"
    }
}
