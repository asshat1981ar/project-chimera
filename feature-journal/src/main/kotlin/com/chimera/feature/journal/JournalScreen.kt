package com.chimera.feature.journal

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Badge
import com.chimera.ui.components.ManuscriptCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.chimera.ui.components.ParchmentInputField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.VowEntity
import com.chimera.ui.theme.DimAsh
import com.chimera.ui.theme.EmberGold
import com.chimera.ui.theme.FadedBone
import com.chimera.ui.theme.HollowCrimson
import com.chimera.ui.theme.VoidGreen

@Composable
fun JournalScreen(
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Journal",
            style = MaterialTheme.typography.headlineMedium,
            color = EmberGold,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
        )

        // Search bar
        ParchmentInputField(
            value = searchQuery,
            onValueChange = viewModel::setSearchQuery,
            placeholder = "Search entries…",
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        modifier = Modifier.testTag("btn_clear_search"),
                        onClick = viewModel::clearSearch
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search",
                            modifier = Modifier.size(18.dp), tint = DimAsh)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("field_search_journal")
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Tabs
        ScrollableTabRow(
            modifier = Modifier.testTag("tabRow_journal"),
            selectedTabIndex = JournalTab.values().indexOf(uiState.selectedTab),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmberGold,
            edgePadding = 16.dp
        ) {
            JournalTab.values().forEach { tab ->
                Tab(
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}"),
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab.label, style = MaterialTheme.typography.labelLarge)
                            if (tab == JournalTab.ALL && uiState.unreadCount > 0) {
                                Badge(
                                    containerColor = HollowCrimson,
                                    modifier = Modifier.padding(start = 6.dp)
                                ) {
                                    Text("${uiState.unreadCount}")
                                }
                            }
                        }
                    },
                    selectedContentColor = EmberGold,
                    unselectedContentColor = FadedBone
                )
            }
        }

        // Content
        if (uiState.selectedTab == JournalTab.VOWS) {
            VowList(vows = uiState.vows)
        } else {
            EntryList(
                entries = uiState.entries,
                onEntryClick = { viewModel.markRead(it.id) },
                searchQuery = uiState.searchQuery
            )
        }
    }
}

@Composable
private fun EntryList(
    entries: List<JournalEntryEntity>,
    onEntryClick: (JournalEntryEntity) -> Unit,
    searchQuery: String = ""
) {
    if (entries.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (searchQuery.isNotBlank()) {
                Text(
                    "No results for \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DimAsh
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Try a different word or clear the search.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DimAsh
                )
            } else {
                Text(
                    "No entries yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DimAsh
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Your deeds will be recorded here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DimAsh
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(entries, key = { it.id }) { entry ->
            JournalEntryCard(entry = entry, onClick = { onEntryClick(entry) })
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntryEntity,
    onClick: () -> Unit
) {
    val borderColor = when (entry.category) {
        "story" -> EmberGold.copy(alpha = 0.4f)
        "rumor" -> VoidGreen.copy(alpha = 0.4f)
        "companion" -> HollowCrimson.copy(alpha = 0.4f)
        else -> FadedBone.copy(alpha = 0.2f)
    }
    val categoryLabel = entry.category.replaceFirstChar { it.uppercase() }

    ManuscriptCard(
        modifier = Modifier
            .testTag("card_journal_entry_${entry.id}")
            .clickable { onClick() },
        fillColor = if (entry.isRead) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        borderColor = borderColor,
        borderWidth = 1.dp,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (entry.isRead) FadedBone else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = categoryLabel,
                style = MaterialTheme.typography.labelSmall,
                color = borderColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.body,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = FadedBone
        )
    }
}

@Composable
private fun VowList(vows: List<VowEntity>) {
    if (vows.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No vows sworn.", style = MaterialTheme.typography.bodyLarge, color = DimAsh)
            Text(
                "Your promises will bind you here.",
                style = MaterialTheme.typography.bodyMedium,
                color = DimAsh
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(vows, key = { it.id }) { vow ->
            VowCard(vow = vow)
        }
    }
}

@Composable
private fun VowCard(vow: VowEntity) {
    val statusColor = when (vow.status) {
        "active" -> EmberGold
        "fulfilled" -> VoidGreen
        "broken" -> HollowCrimson
        else -> FadedBone
    }

    ManuscriptCard(
        modifier = Modifier.testTag("card_vow_${vow.id}"),
        fillColor = MaterialTheme.colorScheme.surface,
        borderColor = statusColor.copy(alpha = 0.4f),
        borderWidth = 1.dp,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = vow.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = vow.status.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )
        }
        if (vow.swornTo != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sworn to: ${vow.swornTo}",
                style = MaterialTheme.typography.bodySmall,
                color = FadedBone
            )
        }
    }
}
