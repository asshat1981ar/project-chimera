package com.chimera.core.engine

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.maxOf

/**
 * RelationshipArchetypeEngine -- NPC relationship dynamics for Chimera RPG.
 *
 * Implements Systems Thinking behavioral patterns for NPC emotional evolution.
 * Feedback loops drive NPC disposition changes based on player interaction patterns.
 *
 * Adapted from the original SystemArchetypeEngine with RPG-specific interaction types.
 */
class RelationshipArchetypeEngine {

    private val activeArchetypes = ConcurrentHashMap<String, SystemArchetype>()
    private val _archetypeEvents = MutableSharedFlow<ArchetypeEvent>()
    val archetypeEvents: SharedFlow<ArchetypeEvent> = _archetypeEvents.asSharedFlow()

    enum class ArchetypeType(val description: String) {
        SHIFTING_THE_BURDEN("NPC relies on quick fixes instead of addressing root problems"),
        ESCALATION("Competitive dynamics that spiral out of control"),
        GROWTH_AND_UNDERINVESTMENT("Growth limited by inadequate investment"),
        FIXES_THAT_FAIL("Solutions that create new problems")
    }

    abstract class SystemArchetype(
        val npcId: String,
        val playerId: String,
        val type: ArchetypeType
    ) {
        protected val systemVariables = mutableMapOf<String, Variable>()
        protected val feedbackLoops = mutableListOf<FeedbackLoop>()
        var isActive = true
            internal set

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
            feedbackLoops.add(FeedbackLoop("quick_fix_relief", quickFix, symptom, -0.8f))
            feedbackLoops.add(FeedbackLoop("dependency_increase", quickFix, dependency, 0.3f))
            feedbackLoops.add(FeedbackLoop("capability_erosion", dependency, rootCause, -0.2f))
            feedbackLoops.add(
                DelayedFeedbackLoop("root_cause_manifestation", rootCause, symptom, 0.6f, 5.0f)
            )
        }

        override suspend fun processInteraction(interaction: NPCInteraction): EmotionalImpact {
            when (interaction.type) {
                InteractionType.GIFT, InteractionType.HELP -> {
                    quickFix.addValue(0.3f)
                }
                InteractionType.PERSUADE, InteractionType.TEACH -> {
                    rootCause.subtractValue(0.4f)
                    dependency.subtractValue(0.1f)
                }
                InteractionType.IGNORE, InteractionType.REFUSE -> {
                    if (dependency.value < 0.5f) {
                        rootCause.subtractValue(0.2f)
                    } else {
                        symptom.addValue(0.4f)
                    }
                }
                InteractionType.THREATEN -> {
                    symptom.addValue(0.3f)
                    dependency.subtractValue(0.2f)
                }
                InteractionType.BARGAIN -> {
                    quickFix.addValue(0.15f)
                    rootCause.subtractValue(0.1f)
                }
                else -> {}
            }
            updateSystemState(interaction.deltaTime)
            return calculateEmotionalImpact()
        }

        private fun calculateEmotionalImpact(): EmotionalImpact {
            return when {
                symptom.value > 0.7f && dependency.value > 0.6f -> EmotionalImpact(
                    emotions = mapOf("frustration" to 0.8f, "helplessness" to 0.6f),
                    dialogueHint = "I can't seem to do anything right without you...",
                    dispositionDelta = -0.05f
                )
                rootCause.value < 0.3f && dependency.value < 0.3f -> EmotionalImpact(
                    emotions = mapOf("confidence" to 0.7f, "gratitude" to 0.5f),
                    dialogueHint = "Thanks for teaching me to stand on my own!",
                    dispositionDelta = 0.15f
                )
                dependency.value > 0.8f -> EmotionalImpact(
                    emotions = mapOf("anxiety" to 0.7f, "attachment" to 0.9f),
                    dialogueHint = "Please don't leave me, I need you!",
                    dispositionDelta = 0.02f
                )
                else -> EmotionalImpact.NEUTRAL
            }
        }

        override fun calculateStabilityIndex(): Float =
            1.0f - abs(symptom.value - 0.5f) * 2.0f

        override fun shouldTerminate(): Boolean =
            rootCause.value < 0.1f && dependency.value < 0.1f
    }

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
            feedbackLoops.add(FeedbackLoop("retaliation", playerAggression, npcAggression, 1.2f))
            feedbackLoops.add(FeedbackLoop("tension_buildup", npcAggression, tensionLevel, 0.8f))
            feedbackLoops.add(FeedbackLoop("relationship_erosion", tensionLevel, relationshipDamage, 0.3f))
        }

        override suspend fun processInteraction(interaction: NPCInteraction): EmotionalImpact {
            escalationRound++
            when (interaction.type) {
                InteractionType.THREATEN -> {
                    playerAggression.setValue(interaction.intensity)
                }
                InteractionType.PERSUADE, InteractionType.GIFT -> {
                    playerAggression.setValue(0.0f)
                    if (tensionLevel.value > 0.8f && kotlin.random.Random.nextFloat() < 0.3f) {
                        npcAggression.addValue(0.2f)
                    } else {
                        npcAggression.multiplyValue(0.8f)
                    }
                }
                InteractionType.IGNORE -> {
                    if (tensionLevel.value > 0.6f) {
                        npcAggression.addValue(0.1f)
                    }
                }
                else -> {}
            }
            updateSystemState(interaction.deltaTime)
            peakTension = maxOf(peakTension, tensionLevel.value)
            return calculateEmotionalImpact()
        }

        private fun calculateEmotionalImpact(): EmotionalImpact {
            return when {
                tensionLevel.value > 0.9f -> EmotionalImpact(
                    emotions = mapOf("rage" to 0.9f, "hurt" to 0.7f),
                    dialogueHint = "This has gone too far! I can't take it anymore!",
                    dispositionDelta = -0.2f
                )
                tensionLevel.value > 0.6f -> EmotionalImpact(
                    emotions = mapOf("anger" to 0.7f, "frustration" to 0.6f),
                    dialogueHint = "Why do we always end up fighting?",
                    dispositionDelta = -0.1f
                )
                tensionLevel.value < 0.4f && peakTension > 0.8f -> EmotionalImpact(
                    emotions = mapOf("regret" to 0.6f, "hope" to 0.4f),
                    dialogueHint = "Maybe we can work this out...",
                    dispositionDelta = 0.05f
                )
                else -> EmotionalImpact.NEUTRAL
            }
        }

        override fun calculateStabilityIndex(): Float =
            1.0f - tensionLevel.value

        override fun shouldTerminate(): Boolean =
            relationshipDamage.value > 0.95f || (tensionLevel.value < 0.1f && escalationRound > 5)
    }

    // Supporting types

    data class Variable(
        val name: String,
        @Volatile private var _value: Float,
        private val min: Float = 0.0f,
        private val max: Float = 1.0f
    ) {
        val value: Float get() = _value
        @Synchronized fun setValue(v: Float) { _value = v.coerceIn(min, max) }
        @Synchronized fun addValue(d: Float) { setValue(_value + d) }
        @Synchronized fun subtractValue(d: Float) { setValue(_value - d) }
        @Synchronized fun multiplyValue(f: Float) { setValue(_value * f) }
    }

    open class FeedbackLoop(
        val name: String,
        val sourceVariable: Variable,
        val targetVariable: Variable,
        val strength: Float
    ) {
        open fun calculate(variables: Map<String, Variable>, deltaTime: Float): Float =
            sourceVariable.value * strength
    }

    class DelayedFeedbackLoop(
        name: String,
        sourceVariable: Variable,
        targetVariable: Variable,
        strength: Float,
        private val delaySeconds: Float,
        private val maxQueueSize: Int = 64
    ) : FeedbackLoop(name, sourceVariable, targetVariable, strength) {

        private data class DelayedValue(val value: Float, var timeRemaining: Float)
        private val delayedValues = mutableListOf<DelayedValue>()

        override fun calculate(variables: Map<String, Variable>, deltaTime: Float): Float {
            // Add current value to queue (bounded to prevent memory leak)
            if (delayedValues.size < maxQueueSize) {
                delayedValues.add(DelayedValue(sourceVariable.value * strength, delaySeconds))
            }
            // Decrement timers and collect ready values
            var total = 0f
            val iterator = delayedValues.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.timeRemaining -= deltaTime
                if (entry.timeRemaining <= 0f) {
                    total += entry.value
                    iterator.remove()
                }
            }
            return total
        }
    }

    data class NPCInteraction(
        val type: InteractionType,
        val intensity: Float = 0.5f,
        val deltaTime: Float = 0.016f
    )

    enum class InteractionType {
        HELP,
        TEACH,
        GIFT,
        THREATEN,
        PERSUADE,
        BARGAIN,
        IGNORE,
        REFUSE,
        NEUTRAL
    }

    data class EmotionalImpact(
        val emotions: Map<String, Float> = emptyMap(),
        val dialogueHint: String = "",
        val dispositionDelta: Float = 0.0f,
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

    // Public API

    suspend fun initializeArchetype(
        type: ArchetypeType,
        npcId: String,
        playerId: String
    ): String {
        val archetype = when (type) {
            ArchetypeType.SHIFTING_THE_BURDEN -> ShiftingTheBurdenArchetype(npcId, playerId)
            ArchetypeType.ESCALATION -> EscalationArchetype(npcId, playerId)
            else -> throw IllegalArgumentException("Archetype $type not yet implemented")
        }
        val key = "${npcId}_${playerId}_${type.name}"
        activeArchetypes[key] = archetype
        _archetypeEvents.emit(ArchetypeEvent(npcId, type, "archetype_initialized", 0.8f))
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
                if (archetype.shouldTerminate()) {
                    archetype.isActive = false
                    _archetypeEvents.emit(
                        ArchetypeEvent(npcId, archetype.type, "archetype_completed", 0.9f)
                    )
                }
                impact
            }
    }

    fun getActiveArchetypes(): Map<String, ArchetypeType> =
        activeArchetypes.filterValues { it.isActive }.mapValues { it.value.type }

    fun getStabilityReport(): Map<String, Float> =
        activeArchetypes.filterValues { it.isActive }.mapValues { it.value.calculateStabilityIndex() }
}
