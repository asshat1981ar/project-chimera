package com.chimera.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chimera.ui.theme.ChimeraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChimeraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChimeraMainScreen()
                }
            }
        }
    }
}

@Composable
fun ChimeraMainScreen(
    viewModel: ChimeraMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Creative Consciousness Header
        CreativeConsciousnessHeader(
            consciousnessLevel = uiState.consciousnessLevel,
            creativityMetrics = uiState.creativityMetrics
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Content Tabs
        CreativeTabLayout(
            selectedTab = uiState.selectedTab,
            onTabSelected = viewModel::selectTab,
            tabs = listOf(
                CreativeTab.DIALOGUE,
                CreativeTab.NARRATIVE,
                CreativeTab.ARCHAEOLOGY,
                CreativeTab.CONSCIOUSNESS
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (uiState.selectedTab) {
            CreativeTab.DIALOGUE -> CreativeDialogueScreen(
                dialogueState = uiState.dialogueState,
                onDialogueAction = viewModel::handleDialogueAction
            )
            CreativeTab.NARRATIVE -> EmergentNarrativeScreen(
                narrativeState = Unit,
                onNarrativeAction = viewModel::handleNarrativeAction
            )
            CreativeTab.ARCHAEOLOGY -> ArchaeologicalDiscoveryScreen(
                archaeologyState = Unit,
                onArchaeologyAction = viewModel::handleArchaeologyAction
            )
            CreativeTab.CONSCIOUSNESS -> ConsciousnessMonitorScreen(
                consciousnessState = Unit,
                onConsciousnessAction = viewModel::handleConsciousnessAction
            )
        }
    }
}

@Composable
fun CreativeConsciousnessHeader(
    consciousnessLevel: Float,
    creativityMetrics: CreativityMetrics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Creative Consciousness Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Consciousness Level Progress
            LinearProgressIndicator(
                progress = consciousnessLevel,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    consciousnessLevel > 0.8f -> Color(0xFF4CAF50) // Green
                    consciousnessLevel > 0.6f -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Level: ${String.format("%.2f", consciousnessLevel)} / 1.00",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Creativity Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CreativityMetricChip(
                    label = "Surprise",
                    value = creativityMetrics.surpriseIndex,
                    color = Color(0xFF9C27B0)
                )
                CreativityMetricChip(
                    label = "Originality", 
                    value = creativityMetrics.originalityScore,
                    color = Color(0xFF2196F3)
                )
                CreativityMetricChip(
                    label = "Resonance",
                    value = creativityMetrics.emotionalResonance,
                    color = Color(0xFF4CAF50)
                )
                CreativityMetricChip(
                    label = "Evidence",
                    value = creativityMetrics.consciousnessEvidence,
                    color = Color(0xFFFF5722)
                )
            }
        }
    }
}

@Composable
fun CreativityMetricChip(
    label: String,
    value: Float,
    color: Color
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = String.format("%.2f", value),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun CreativeTabLayout(
    selectedTab: CreativeTab,
    onTabSelected: (CreativeTab) -> Unit,
    tabs: List<CreativeTab>
) {
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = tab.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.displayName
                    )
                }
            )
        }
    }
}

@Composable
fun CreativeDialogueScreen(
    dialogueState: DialogueUiState,
    onDialogueAction: (DialogueAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Active NPCs Section
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Active NPCs with System Archetypes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(dialogueState.activeNPCs) { npc ->
                        NPCArchetypeCard(
                            npc = npc,
                            onInteract = { onDialogueAction(DialogueAction.InteractWithNPC(npc.id)) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // System Archetype Status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Active System Archetypes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn {
                    items(dialogueState.activeArchetypes) { archetype ->
                        ArchetypeStatusCard(
                            archetype = archetype,
                            onViewDetails = { 
                                onDialogueAction(DialogueAction.ViewArchetypeDetails(archetype.id)) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NPCArchetypeCard(
    npc: NPCArchetypeUi,
    onInteract: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onInteract
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // NPC Avatar
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = npc.name.first().toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = npc.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Archetype: ${npc.activeArchetype}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Stability: ${String.format("%.1f", npc.stabilityIndex)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Emotional State Indicator
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when {
                    npc.emotionalIntensity > 0.8f -> Color(0xFFFF5722)
                    npc.emotionalIntensity > 0.6f -> Color(0xFFFF9800)
                    else -> Color(0xFF4CAF50)
                }.copy(alpha = 0.1f)
            ) {
                Text(
                    text = String.format("%.2f", npc.emotionalIntensity),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun ArchetypeStatusCard(
    archetype: ArchetypeStatusUi,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onViewDetails
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = archetype.type,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (archetype.isStable) 
                        Color(0xFF4CAF50).copy(alpha = 0.1f) 
                    else 
                        Color(0xFFFF5722).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (archetype.isStable) "Stable" else "Unstable",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "NPCs: ${archetype.involvedNPCs.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = archetype.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress indicators for key variables
            archetype.keyVariables.take(2).forEach { variable ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = variable.name,
                        style = MaterialTheme.typography.labelSmall
                    )
                    LinearProgressIndicator(
                        progress = variable.value,
                        modifier = Modifier
                            .width(80.dp)
                            .height(4.dp)
                    )
                    Text(
                        text = String.format("%.2f", variable.value),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// Placeholder implementations for other screens
@Composable
fun EmergentNarrativeScreen(
    narrativeState: Any,
    onNarrativeAction: (Any) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Emergent Narrative Generation Coming Soon")
    }
}

@Composable
fun ArchaeologicalDiscoveryScreen(
    archaeologyState: Any,
    onArchaeologyAction: (Any) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Emotional Archaeology Coming Soon")
    }
}

@Composable
fun ConsciousnessMonitorScreen(
    consciousnessState: Any,
    onConsciousnessAction: (Any) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Consciousness Monitoring Coming Soon")
    }
}