package com.chimera.core.archetypes

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

/**
 * SystemArchetypeEngine - Core Innovation of Project Chimera
 * 
 * Implements Systems Thinking behavioral patterns for NPC emotional evolution.
 * This is the breakthrough innovation that makes NPCs exhibit genuine behavioral complexity.
 */
class SystemArchetypeEngine {
    
    private val activeArchetypes = ConcurrentHashMap<String, SystemArchetype>()
    private val emergentBehaviorDetector = EmergentBehaviorDetector()
    private val _archetypeEvents = MutableSharedFlow<ArchetypeEvent>()
    val archetypeEvents: SharedFlow<ArchetypeEvent> = _archetypeEvents.asSharedFlow()
    
    /**
     * Core System Archetypes based on Systems Thinking principles
     */
    enum class ArchetypeType(val description: String) {
        SHIFTING_THE_BURDEN("Quick fixes that prevent real solutions"),
        ESCALATION("Competitive dynamics that spiral out of control"),
        GROWTH_AND_UNDERINVESTMENT("Growth limited by inadequate investment"),
        FIXES_THAT_FAIL("Solutions that create new problems")
    }
    
    /**
     * Base class for all system archetypes
     */
    abstract class SystemArchetype(
        val npcId: String,
        val playerId: String,
        val type: ArchetypeType
    ) {
        protected val systemVariables = mutableMapOf<String, Variable>()
        protected val feedbackLoops = mutableListOf<FeedbackLoop>()
        protected var isActive = true
        
        abstract suspend fun processInteraction(interaction: NPCInteraction): EmotionalImpact
        abstract fun calculateStabilityIndex(): Float
        abstract fun shouldTerminate(): Boolean
        
        protected fun updateSystemState(deltaTime: Float) {
            feedbackLoops.forEach { loop ->
                val output = loop.calculate(systemVariables, deltaTime)
                loop.targetVariable.addValue(output * deltaTime)
            }
        }
    }
    
    /**
     * Shifting the Burden - NPCs rely on quick fixes instead of addressing root causes
     * Example: NPC always asks player for help instead of developing own skills
     */
    class ShiftingTheBurdenArchetype(
        npcId: String,
        playerId: String
    ) : SystemArchetype(npcId, playerId, ArchetypeType.SHIFTING_THE_BURDEN) {
        
        private val symptom = Variable("symptom", 0.5f)
        private val quickFix = Variable("quick_fix", 0.0f)
        private val rootCause = Variable("root_cause", 0.8f)
        private val dependency = Variable("dependency", 0.1f)
        
        init {
            systemVariables["symptom"] = symptom
            systemVariables["quick_fix"] = quickFix
            systemVariables["root_cause"] = rootCause
            systemVariables["dependency"] = dependency
            
            createFeedbackLoops()
        }
        
        private fun createFeedbackLoops() {
            // Quick fix reduces symptom temporarily
            feedbackLoops.add(FeedbackLoop(
                "quick_fix_relief",
                quickFix, symptom, -0.8f
            ))
            
            // But increases dependency
            feedbackLoops.add(FeedbackLoop(
                "dependency_increase",
                quickFix, dependency, 0.3f
            ))
            
            // Dependency weakens ability to address root cause
            feedbackLoops.add(FeedbackLoop(
                "capability_erosion",
                dependency, rootCause, -0.2f
            ))
            
            // Root cause drives symptom (with delay)
            feedbackLoops.add(DelayedFeedbackLoop(
                "root_cause_manifestation",
                rootCause, symptom, 0.6f, delaySeconds = 5.0f
            ))
        }
        
        override suspend fun processInteraction(interaction: NPCInteraction): EmotionalImpact {
            when (interaction.type) {
                InteractionType.PLAYER_HELPS_NPC -> {
                    quickFix.addValue(0.3f)
                }
                InteractionType.PLAYER_TEACHES_NPC -> {
                    rootCause.subtractValue(0.4f)
                    dependency.subtractValue(0.1f)
                }
                InteractionType.PLAYER_REFUSES_HELP -> {
                    if (dependency.value < 0.5f) {
                        rootCause.subtractValue(0.2f) // Forces self-reliance
                    } else {
                        symptom.addValue(0.4f) // Dependency crisis
                    }
                }
            }
            
            updateSystemState(interaction.deltaTime)
            return calculateEmotionalImpact()
        }
        
        private fun calculateEmotionalImpact(): EmotionalImpact {
            return when {
                symptom.value > 0.7f && dependency.value > 0.6f -> EmotionalImpact(
                    emotions = mapOf(
                        "frustration" to 0.8f,
                        "helplessness" to 0.6f
                    ),
                    dialogueHint = "I can't seem to do anything right without you..."
                )
                
                rootCause.value < 0.3f && dependency.value < 0.3f -> EmotionalImpact(
                    emotions = mapOf(
                        "confidence" to 0.7f,
                        "gratitude" to 0.5f
                    ),
                    dialogueHint = "Thanks for teaching me to stand on my own!"
                )
                
                dependency.value > 0.8f -> EmotionalImpact(
                    emotions = mapOf(
                        "anxiety" to 0.7f,
                        "attachment" to 0.9f
                    ),
                    dialogueHint = "Please don't leave me, I need you!"
                )
                
                else -> EmotionalImpact.NEUTRAL
            }
        }
        
        override fun calculateStabilityIndex(): Float {
            return 1.0f - abs(symptom.value - 0.5f) * 2.0f
        }
        
        override fun shouldTerminate(): Boolean {
            return rootCause.value < 0.1f && dependency.value < 0.1f
        }
    }
    
    /**
     * Escalation Archetype - Competition that spirals out of control
     */
    class EscalationArchetype(
        npcId: String,
        playerId: String
    ) : SystemArchetype(npcId, playerId, ArchetypeType.ESCALATION) {
        
        private val npcAggression = Variable("npc_aggression", 0.3f)
        private val playerAggression = Variable("player_aggression", 0.0f)
        private val tensionLevel = Variable("tension", 0.3f)
        private val relationshipDamage = Variable("damage", 0.0f)
        
        private var escalationRound = 0
        private var peakTension = 0.0f
        
        init {
            systemVariables["npc_aggression"] = npcAggression
            systemVariables["player_aggression"] = playerAggression
            systemVariables["tension"] = tensionLevel
            systemVariables["damage"] = relationshipDamage
            
            createEscalationLoops()
        }
        
        private fun createEscalationLoops() {
            // Player aggression triggers NPC retaliation (with amplification)
            feedbackLoops.add(FeedbackLoop(
                "retaliation_amplification",
                playerAggression, npcAggression, 1.2f
            ))
            
            // Aggression builds tension
            feedbackLoops.add(FeedbackLoop(
                "tension_buildup",
                npcAggression, tensionLevel, 0.8f
            ))
            
            // High tension damages relationship
            feedbackLoops.add(FeedbackLoop(
                "relationship_erosion",
                tensionLevel, relationshipDamage, 0.3f
            ))
        }
        
        override suspend fun processInteraction(interaction: NPCInteraction): EmotionalImpact {
            escalationRound++
            
            when (interaction.type) {
                InteractionType.PLAYER_AGGRESSIVE -> {
                    playerAggression.setValue(interaction.intensity)
                }
                InteractionType.PLAYER_DEESCALATION -> {
                    playerAggression.setValue(0.0f)
                    // NPC may not immediately accept deescalation
                    if (tensionLevel.value > 0.8f && Math.random() < 0.3) {
                        npcAggression.addValue(0.2f) // Rejection
                    } else {
                        npcAggression.multiplyValue(0.8f) // Cooling down
                    }
                }
                InteractionType.PLAYER_NEUTRAL -> {
                    if (tensionLevel.value > 0.6f) {
                        // NPC interprets neutrality as weakness during high tension
                        npcAggression.addValue(0.1f)
                    }
                }
            }
            
            updateSystemState(interaction.deltaTime)
            peakTension = maxOf(peakTension, tensionLevel.value)
            
            return calculateEmotionalImpact()
        }
        
        private fun calculateEmotionalImpact(): EmotionalImpact {
            return when {
                tensionLevel.value > 0.9f -> EmotionalImpact(
                    emotions = mapOf(
                        "rage" to 0.9f,
                        "hurt" to 0.7f
                    ),
                    dialogueHint = "This has gone too far! I can't take it anymore!"
                )
                
                tensionLevel.value > 0.6f -> EmotionalImpact(
                    emotions = mapOf(
                        "anger" to 0.7f,
                        "frustration" to 0.6f
                    ),
                    dialogueHint = "Why do we always end up fighting?"
                )
                
                tensionLevel.value < 0.4f && peakTension > 0.8f -> EmotionalImpact(
                    emotions = mapOf(
                        "regret" to 0.6f,
                        "hope" to 0.4f
                    ),
                    dialogueHint = "Maybe we can work this out..."
                )
                
                else -> EmotionalImpact.NEUTRAL
            }
        }
        
        override fun calculateStabilityIndex(): Float {
            return 1.0f - tensionLevel.value
        }
        
        override fun shouldTerminate(): Boolean {
            return relationshipDamage.value > 0.95f || 
                   (tensionLevel.value < 0.1f && escalationRound > 5)
        }
    }
    
    /**
     * Supporting classes for the archetype system
     */
    data class Variable(
        val name: String,
        private var _value: Float,
        private val min: Float = 0.0f,
        private val max: Float = 1.0f
    ) {
        val value: Float get() = _value
        
        fun setValue(newValue: Float) {
            _value = newValue.coerceIn(min, max)
        }
        
        fun addValue(delta: Float) {
            setValue(_value + delta)
        }
        
        fun subtractValue(delta: Float) {
            setValue(_value - delta)
        }
        
        fun multiplyValue(factor: Float) {
            setValue(_value * factor)
        }
    }
    
    class FeedbackLoop(
        val name: String,
        val sourceVariable: Variable,
        val targetVariable: Variable,
        val strength: Float
    ) {
        open fun calculate(variables: Map<String, Variable>, deltaTime: Float): Float {
            return sourceVariable.value * strength
        }
    }
    
    class DelayedFeedbackLoop(
        name: String,
        sourceVariable: Variable,
        targetVariable: Variable,
        strength: Float,
        private val delaySeconds: Float
    ) : FeedbackLoop(name, sourceVariable, targetVariable, strength) {
        
        private val delayedValues = mutableListOf<Pair<Float, Float>>() // (value, timeRemaining)
        
        override fun calculate(variables: Map<String, Variable>, deltaTime: Float): Float {
            // Add current value to delay queue
            delayedValues.add(sourceVariable.value * strength to delaySeconds)
            
            // Update delay timers and extract ready values
            val readyValues = mutableListOf<Float>()
            val iterator = delayedValues.iterator()
            
            while (iterator.hasNext()) {
                val (value, timeRemaining) = iterator.next()
                val newTime = timeRemaining - deltaTime
                
                if (newTime <= 0) {
                    readyValues.add(value)
                    iterator.remove()
                }
            }
            
            return readyValues.sum()
        }
    }
    
    data class NPCInteraction(
        val type: InteractionType,
        val intensity: Float = 0.5f,
        val deltaTime: Float = 0.016f // 60 FPS default
    )
    
    enum class InteractionType {
        PLAYER_HELPS_NPC,
        PLAYER_TEACHES_NPC,
        PLAYER_REFUSES_HELP,
        PLAYER_AGGRESSIVE,
        PLAYER_DEESCALATION,
        PLAYER_NEUTRAL
    }
    
    data class EmotionalImpact(
        val emotions: Map<String, Float> = emptyMap(),
        val dialogueHint: String = "",
        val behaviorModifiers: Map<String, Float> = emptyMap()
    ) {
        companion object {
            val NEUTRAL = EmotionalImpact()
        }
    }
    
    data class ArchetypeEvent(
        val npcId: String,
        val type: ArchetypeType,
        val event: String,
        val significance: Float
    )
    
    class EmergentBehaviorDetector {
        fun recordPattern(patternType: String, npcId: String, description: String) {
            // Implementation for detecting emergent behavioral patterns
        }
    }
    
    /**
     * Main interface methods
     */
    suspend fun initializeArchetype(
        type: ArchetypeType,
        npcId: String,
        playerId: String,
        initialConditions: Map<String, Float> = emptyMap()
    ): String {
        val archetype = when (type) {
            ArchetypeType.SHIFTING_THE_BURDEN -> 
                ShiftingTheBurdenArchetype(npcId, playerId)
            ArchetypeType.ESCALATION -> 
                EscalationArchetype(npcId, playerId)
            else -> throw IllegalArgumentException("Archetype $type not yet implemented")
        }
        
        val key = "${npcId}_${playerId}_${type.name}"
        activeArchetypes[key] = archetype
        
        _archetypeEvents.emit(ArchetypeEvent(
            npcId, type, "archetype_initialized", 0.8f
        ))
        
        return key
    }
    
    suspend fun processInteraction(
        npcId: String,
        playerId: String,
        interaction: NPCInteraction
    ): List<EmotionalImpact> {
        return activeArchetypes.values
            .filter { it.npcId == npcId && it.playerId == playerId && it.isActive }
            .map { archetype ->
                val impact = archetype.processInteraction(interaction)
                
                // Check for termination
                if (archetype.shouldTerminate()) {
                    archetype.isActive = false
                    _archetypeEvents.emit(ArchetypeEvent(
                        npcId, archetype.type, "archetype_completed", 0.9f
                    ))
                }
                
                impact
            }
    }
    
    fun getActiveArchetypes(): Map<String, ArchetypeType> {
        return activeArchetypes
            .filterValues { it.isActive }
            .mapValues { it.value.type }
    }
    
    suspend fun getStabilityReport(): Map<String, Float> {
        return activeArchetypes
            .filterValues { it.isActive }
            .mapValues { it.value.calculateStabilityIndex() }
    }
}