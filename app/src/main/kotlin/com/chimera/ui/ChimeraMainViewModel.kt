package com.chimera.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.chimera.core.archetypes.SystemArchetypeEngine
import com.chimera.core.events.GameEventBus
import com.chimera.core.worldstate.WorldStateManagerService

@HiltViewModel
class ChimeraMainViewModel @Inject constructor(
    private val systemArchetypeEngine: SystemArchetypeEngine,
    private val gameEventBus: GameEventBus,
    private val worldStateManager: WorldStateManagerService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChimeraUiState.INITIAL)
    val uiState: StateFlow<ChimeraUiState> = _uiState.asStateFlow()
    
    init {
        observeArchetypeEvents()
        observeGameEvents()
        initializeSampleData()
    }
    
    private fun observeArchetypeEvents() {
        viewModelScope.launch {
            systemArchetypeEngine.archetypeEvents.collect { event ->
                updateArchetypeStatus(event)
            }
        }
    }
    
    private fun observeGameEvents() {
        viewModelScope.launch {
            gameEventBus.eventFlow.collect { event ->
                handleGameEvent(event)
            }
        }
    }
    
    private fun initializeSampleData() {
        viewModelScope.launch {
            // Initialize sample NPCs with archetypes
            val sampleNPCs = listOf(
                NPCArchetypeUi(
                    id = "npc_1",
                    name = "Elena the Merchant",
                    activeArchetype = "Shifting the Burden",
                    stabilityIndex = 0.6f,
                    emotionalIntensity = 0.4f
                ),
                NPCArchetypeUi(
                    id = "npc_2", 
                    name = "Marcus the Guard",
                    activeArchetype = "Escalation",
                    stabilityIndex = 0.3f,
                    emotionalIntensity = 0.8f
                ),
                NPCArchetypeUi(
                    id = "npc_3",
                    name = "Aria the Scholar",
                    activeArchetype = "Growth and Underinvestment",
                    stabilityIndex = 0.7f,
                    emotionalIntensity = 0.2f
                )
            )
            
            val sampleArchetypes = listOf(
                ArchetypeStatusUi(
                    id = "archetype_1",
                    type = "Shifting the Burden",
                    description = "Elena relies on quick fixes instead of addressing root problems",
                    involvedNPCs = listOf("Elena"),
                    isStable = true,
                    keyVariables = listOf(
                        VariableStatus("dependency", 0.6f),
                        VariableStatus("symptom", 0.4f)
                    )
                ),
                ArchetypeStatusUi(
                    id = "archetype_2",
                    type = "Escalation",
                    description = "Conflict with Marcus spiraling out of control",
                    involvedNPCs = listOf("Marcus"),
                    isStable = false,
                    keyVariables = listOf(
                        VariableStatus("tension", 0.8f),
                        VariableStatus("aggression", 0.7f)
                    )
                )
            )
            
            _uiState.value = _uiState.value.copy(
                dialogueState = _uiState.value.dialogueState.copy(
                    activeNPCs = sampleNPCs,
                    activeArchetypes = sampleArchetypes
                ),
                consciousnessLevel = 0.75f,
                creativityMetrics = CreativityMetrics(
                    surpriseIndex = 0.8f,
                    originalityScore = 0.9f,
                    emotionalResonance = 0.7f,
                    consciousnessEvidence = 0.85f
                )
            )
        }
    }
    
    fun selectTab(tab: CreativeTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }
    
    fun handleDialogueAction(action: DialogueAction) {
        viewModelScope.launch {
            when (action) {
                is DialogueAction.InteractWithNPC -> {
                    interactWithNPC(action.npcId)
                }
                is DialogueAction.ViewArchetypeDetails -> {
                    viewArchetypeDetails(action.archetypeId)
                }
            }
        }
    }
    
    private suspend fun interactWithNPC(npcId: String) {
        // Find the NPC
        val npc = _uiState.value.dialogueState.activeNPCs.find { it.id == npcId }
            ?: return
        
        // Simulate interaction based on archetype
        val interaction = com.chimera.core.archetypes.NPCInteraction(
            type = com.chimera.core.archetypes.InteractionType.PLAYER_HELPS_NPC,
            intensity = 0.7f
        )
        
        // Process through archetype engine
        val impacts = systemArchetypeEngine.processInteraction(
            npcId = npcId,
            playerId = "player_1",
            interaction = interaction
        )
        
        // Update UI based on impacts
        impacts.forEach { impact ->
            updateNPCEmotionalState(npcId, impact)
        }
        
        // Get updated stability report
        val stabilityReport = systemArchetypeEngine.getStabilityReport()
        updateStabilityIndicators(stabilityReport)
    }
    
    private fun updateNPCEmotionalState(
        npcId: String, 
        impact: com.chimera.core.archetypes.EmotionalImpact
    ) {
        val currentNPCs = _uiState.value.dialogueState.activeNPCs.toMutableList()
        val npcIndex = currentNPCs.indexOfFirst { it.id == npcId }
        
        if (npcIndex != -1) {
            val currentNPC = currentNPCs[npcIndex]
            val emotionalIntensity = impact.emotions.values.maxOrNull() ?: 0.0f
            
            currentNPCs[npcIndex] = currentNPC.copy(
                emotionalIntensity = emotionalIntensity
            )
            
            _uiState.value = _uiState.value.copy(
                dialogueState = _uiState.value.dialogueState.copy(
                    activeNPCs = currentNPCs
                )
            )
        }
    }
    
    private fun updateStabilityIndicators(stabilityReport: Map<String, Float>) {
        val currentArchetypes = _uiState.value.dialogueState.activeArchetypes.toMutableList()
        
        stabilityReport.forEach { (archetypeKey, stability) ->
            val archetypeIndex = currentArchetypes.indexOfFirst { 
                archetypeKey.contains(it.type.replace(" ", "_").uppercase()) 
            }
            
            if (archetypeIndex != -1) {
                val currentArchetype = currentArchetypes[archetypeIndex]
                currentArchetypes[archetypeIndex] = currentArchetype.copy(
                    isStable = stability > 0.6f
                )
            }
        }
        
        _uiState.value = _uiState.value.copy(
            dialogueState = _uiState.value.dialogueState.copy(
                activeArchetypes = currentArchetypes
            )
        )
    }
    
    private fun updateArchetypeStatus(event: com.chimera.core.archetypes.ArchetypeEvent) {
        // Update consciousness level based on archetype events
        val consciousnessBoost = when (event.event) {
            "archetype_initialized" -> 0.05f
            "archetype_completed" -> 0.1f
            else -> 0.01f
        }
        
        _uiState.value = _uiState.value.copy(
            consciousnessLevel = (_uiState.value.consciousnessLevel + consciousnessBoost).coerceAtMost(1.0f)
        )
    }
    
    private fun handleGameEvent(event: com.chimera.core.events.GameEvent) {
        // Handle various game events and update UI accordingly
        viewModelScope.launch {
            when (event) {
                is com.chimera.core.events.EmotionalEvent -> {
                    updateCreativityMetrics(event)
                }
                // Handle other event types
            }
        }
    }
    
    private fun updateCreativityMetrics(event: com.chimera.core.events.EmotionalEvent) {
        val currentMetrics = _uiState.value.creativityMetrics
        
        // Simulate metrics update based on emotional events
        val updatedMetrics = currentMetrics.copy(
            surpriseIndex = (currentMetrics.surpriseIndex + 0.02f).coerceAtMost(1.0f),
            emotionalResonance = (currentMetrics.emotionalResonance + 0.01f).coerceAtMost(1.0f)
        )
        
        _uiState.value = _uiState.value.copy(
            creativityMetrics = updatedMetrics
        )
    }
    
    private suspend fun viewArchetypeDetails(archetypeId: String) {
        // Implementation for viewing detailed archetype information
    }
    
    // Placeholder for other action handlers
    fun handleNarrativeAction(action: Any) {}
    fun handleArchaeologyAction(action: Any) {}
    fun handleConsciousnessAction(action: Any) {}
}

// UI State data classes
data class ChimeraUiState(
    val selectedTab: CreativeTab,
    val consciousnessLevel: Float,
    val creativityMetrics: CreativityMetrics,
    val dialogueState: DialogueUiState
) {
    companion object {
        val INITIAL = ChimeraUiState(
            selectedTab = CreativeTab.DIALOGUE,
            consciousnessLevel = 0.3f,
            creativityMetrics = CreativityMetrics(0.0f, 0.0f, 0.0f, 0.0f),
            dialogueState = DialogueUiState.INITIAL
        )
    }
}

data class DialogueUiState(
    val activeNPCs: List<NPCArchetypeUi>,
    val activeArchetypes: List<ArchetypeStatusUi>,
    val isGeneratingDialogue: Boolean
) {
    companion object {
        val INITIAL = DialogueUiState(
            activeNPCs = emptyList(),
            activeArchetypes = emptyList(),
            isGeneratingDialogue = false
        )
    }
}

data class CreativityMetrics(
    val surpriseIndex: Float,
    val originalityScore: Float,
    val emotionalResonance: Float,
    val consciousnessEvidence: Float
)

enum class CreativeTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DIALOGUE("Archetypes", Icons.Default.Psychology),
    NARRATIVE("Emergent Stories", Icons.Default.AutoStories),
    ARCHAEOLOGY("Emotional Fossils", Icons.Default.Search),
    CONSCIOUSNESS("Consciousness", Icons.Default.Visibility)
}

data class NPCArchetypeUi(
    val id: String,
    val name: String,
    val activeArchetype: String,
    val stabilityIndex: Float,
    val emotionalIntensity: Float
)

data class ArchetypeStatusUi(
    val id: String,
    val type: String,
    val description: String,
    val involvedNPCs: List<String>,
    val isStable: Boolean,
    val keyVariables: List<VariableStatus>
)

data class VariableStatus(
    val name: String,
    val value: Float
)

// Action classes
sealed class DialogueAction {
    data class InteractWithNPC(val npcId: String) : DialogueAction()
    data class ViewArchetypeDetails(val archetypeId: String) : DialogueAction()
}