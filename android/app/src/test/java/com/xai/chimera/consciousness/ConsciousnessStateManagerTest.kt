package com.xai.chimera.consciousness

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

/**
 * Comprehensive test suite for ConsciousnessStateManager
 * Validates real-time consciousness state modeling and transitions
 */
class ConsciousnessStateManagerTest {

    private lateinit var consciousnessStateManager: ConsciousnessStateManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        consciousnessStateManager = ConsciousnessStateManager()
    }

    @Test
    fun `updateConsciousnessState should produce valid consciousness metrics`() = runTest {
        // Arrange
        val conversationStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "emotional_growth",
            secondaryTopics = listOf("self_awareness", "learning"),
            intensity = 0.7f,
            novelty = 0.6f,
            complexity = 0.5f,
            emotionalIntensity = 0.8f
        )
        
        val emotionalState = mapOf("curiosity" to 0.7f, "excitement" to 0.6f)
        
        val memoryActivation = MemoryActivation(
            activationLevel = 0.6f,
            relevance = 0.8f,
            processingLoad = 0.4f,
            activatedMemories = listOf("memory_001", "memory_002")
        )

        // Act
        val consciousnessState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = conversationStimuli,
            emotionalState = emotionalState,
            memoryActivation = memoryActivation
        )

        // Assert
        assertNotNull("Consciousness state should not be null", consciousnessState)
        assertTrue("Awareness level should be between 0 and 1",
                  consciousnessState.awarenessLevel in 0f..1f)
        assertTrue("Cognitive load should be between 0 and 1",
                  consciousnessState.cognitiveLoad in 0f..1f)
        assertTrue("Metacognition level should be between 0 and 1",
                  consciousnessState.metacognitionLevel in 0f..1f)
        assertTrue("State coherence should be between 0 and 1",
                  consciousnessState.stateCoherence in 0f..1f)
        assertNotNull("Attention focus should be set", consciousnessState.attentionFocus)
        assertNotNull("Consciousness quality should be calculated", consciousnessState.consciousnessQuality)
    }

    @Test
    fun `updateConsciousnessState should respond to stimuli intensity`() = runTest {
        // Arrange
        val highIntensityStimuli = ConversationStimuli(
            type = StimuliType.EMOTIONAL_TRIGGER,
            primaryTopic = "crisis_situation",
            intensity = 0.9f,
            novelty = 0.8f,
            complexity = 0.7f,
            emotionalIntensity = 0.9f
        )
        
        val lowIntensityStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "casual_chat",
            intensity = 0.2f,
            novelty = 0.1f,
            complexity = 0.2f,
            emotionalIntensity = 0.3f
        )
        
        val neutralMemoryActivation = MemoryActivation(0.5f, 0.5f, 0.3f)
        val neutralEmotions = mapOf("neutral" to 0.5f)

        // Act
        val highIntensityState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = highIntensityStimuli,
            emotionalState = neutralEmotions,
            memoryActivation = neutralMemoryActivation
        )
        
        val lowIntensityState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = lowIntensityStimuli,
            emotionalState = neutralEmotions,
            memoryActivation = neutralMemoryActivation
        )

        // Assert
        assertTrue("High intensity should increase attention focus",
                  highIntensityState.attentionFocus.intensity > lowIntensityState.attentionFocus.intensity)
        assertTrue("High intensity should increase cognitive load",
                  highIntensityState.cognitiveLoad > lowIntensityState.cognitiveLoad)
        assertEquals("Primary focus should match stimuli topic",
                    highIntensityState.attentionFocus.primaryFocus, "crisis_situation")
    }

    @Test
    fun `updateConsciousnessState should generate consciousness events`() = runTest {
        // Arrange
        val novelStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "novel_concept",
            intensity = 0.8f,
            novelty = 0.9f, // High novelty should trigger events
            complexity = 0.6f,
            emotionalIntensity = 0.5f
        )
        
        val memoryActivation = MemoryActivation(0.6f, 0.7f, 0.4f)
        val emotions = mapOf("curiosity" to 0.8f)

        // Act
        val consciousnessState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = novelStimuli,
            emotionalState = emotions,
            memoryActivation = memoryActivation
        )

        // Assert
        assertFalse("Consciousness events should be generated for novel stimuli",
                   consciousnessState.consciousnessStream.isEmpty())
        
        // Verify event types
        val eventTypes = consciousnessState.consciousnessStream.map { it.type }
        assertTrue("Should contain attention shift events",
                  eventTypes.contains(ConsciousnessEventType.ATTENTION_SHIFT))
    }

    @Test
    fun `simulateConsciousnessIdle should generate background activity`() = runTest {
        // Arrange
        val idleDuration = 2000L
        val backgroundThoughts = listOf(
            "Reflecting on recent conversations",
            "Processing emotional patterns",
            "Consolidating memories"
        )

        // Act
        val idleSimulation = consciousnessStateManager.simulateConsciousnessIdle(
            duration = idleDuration,
            backgroundThoughts = backgroundThoughts
        )

        // Assert
        assertNotNull("Idle simulation should not be null", idleSimulation)
        assertEquals("Duration should match requested", idleDuration, idleSimulation.duration)
        assertFalse("Idle events should be generated", idleSimulation.idleEvents.isEmpty())
        assertTrue("Should have consciousness shift", idleSimulation.consciousnessShift >= 0f)
        assertNotNull("Background insights should be generated", idleSimulation.backgroundProcessingInsights)
        
        // Verify idle event types
        val idleEventTypes = idleSimulation.idleEvents.map { it.type }
        assertTrue("Should contain background processing events",
                  idleEventTypes.any { it == ConsciousnessEventType.BACKGROUND_PROCESSING })
    }

    @Test
    fun `generateConsciousnessCommentary should provide meaningful insights`() = runTest {
        // Arrange
        val consciousnessState = ConsciousnessState(
            awarenessLevel = 0.8f,
            attentionFocus = AttentionFocus(
                primaryFocus = "deep_conversation",
                intensity = 0.7f,
                coherence = 0.8f
            ),
            cognitiveLoad = 0.6f,
            metacognitionLevel = 0.7f,
            consciousnessQuality = ConsciousnessQuality(
                clarity = 0.8f,
                efficiency = 0.7f,
                depth = 0.9f,
                coherence = 0.8f,
                overallQuality = 0.8f
            )
        )
        
        val conversationContext = EnhancedConversationContext(
            primaryTopics = listOf("consciousness", "self_awareness"),
            emotionalTone = "contemplative",
            relationshipDepth = 0.7f,
            conversationGoals = listOf("deep_understanding"),
            contextualReferences = emptyList(),
            suggestedResponseStyle = ResponseStyle.DETAILED_ANALYTICAL
        )

        // Act
        val commentary = consciousnessStateManager.generateConsciousnessCommentary(
            currentState = consciousnessState,
            conversationContext = conversationContext
        )

        // Assert
        assertNotNull("Commentary should not be null", commentary)
        assertFalse("Awareness commentary should not be empty", 
                   commentary.awarenessCommentary.isBlank())
        assertFalse("Attention commentary should not be empty",
                   commentary.attentionCommentary.isBlank())
        assertFalse("Cognitive load commentary should not be empty",
                   commentary.cognitiveLoadCommentary.isBlank())
        assertFalse("Metacognitive commentary should not be empty",
                   commentary.metacognitiveCommentary.isBlank())
        assertFalse("Overall narrative should not be empty",
                   commentary.overallConsciousnessNarrative.isBlank())
        assertFalse("Consciousness insights should not be empty",
                   commentary.consciousnessInsights.isEmpty())
    }

    @Test
    fun `detectConsciousnessTransitions should identify significant changes`() = runTest {
        // Arrange
        val previousState = ConsciousnessState(
            awarenessLevel = 0.5f,
            cognitiveLoad = 0.4f,
            metacognitionLevel = 0.3f
        )
        
        val currentState = ConsciousnessState(
            awarenessLevel = 0.8f, // Significant increase
            cognitiveLoad = 0.4f,
            metacognitionLevel = 0.3f
        )

        // Act
        val transition = consciousnessStateManager.detectConsciousnessTransitions(
            previousState = previousState,
            currentState = currentState
        )

        // Assert
        assertNotNull("Transition should be detected for significant awareness change", transition)
        assertEquals("Should detect awareness shift", TransitionType.AWARENESS_SHIFT, transition!!.type)
        assertTrue("Magnitude should reflect the change", transition.magnitude > 0.2f)
        assertEquals("Direction should be increasing", "increasing", transition.direction)
        assertFalse("Description should not be empty", transition.description.isBlank())
        assertFalse("Triggers should be identified", transition.triggers.isEmpty())
    }

    @Test
    fun `getConsciousnessStateFlow should provide real_time_updates`() = runTest {
        // Act
        val stateFlow = consciousnessStateManager.getConsciousnessStateFlow()
        val initialState = stateFlow.first()

        // Assert
        assertNotNull("State flow should provide initial state", initialState)
        assertTrue("Initial awareness should be reasonable", 
                  initialState.awarenessLevel in 0.2f..1f)
        assertTrue("Initial cognitive load should be reasonable",
                  initialState.cognitiveLoad in 0f..1f)
    }

    @Test
    fun `getConsciousnessEventsFlow should stream events`() = runTest {
        // Arrange
        val stimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "test_topic",
            intensity = 0.8f,
            novelty = 0.7f,
            complexity = 0.5f,
            emotionalIntensity = 0.6f
        )

        // Act - Update state to trigger events
        consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = stimuli,
            emotionalState = mapOf("test" to 0.5f),
            memoryActivation = MemoryActivation(0.5f, 0.5f, 0.3f)
        )

        val eventsFlow = consciousnessStateManager.getConsciousnessEventsFlow()
        
        // Note: In a real test, we would collect events over time
        // This test verifies the flow is available
        assertNotNull("Events flow should be available", eventsFlow)
    }

    @Test
    fun `consciousness_state_should_handle_extreme_cognitive_load`() = runTest {
        // Arrange
        val extremeStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "complex_crisis",
            intensity = 1.0f,
            novelty = 1.0f,
            complexity = 1.0f,
            emotionalIntensity = 1.0f
        )
        
        val highMemoryLoad = MemoryActivation(
            activationLevel = 1.0f,
            relevance = 1.0f,
            processingLoad = 1.0f
        )
        
        val intenseEmotions = mapOf(
            "overwhelm" to 1.0f,
            "stress" to 0.9f,
            "confusion" to 0.8f
        )

        // Act
        val consciousnessState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = extremeStimuli,
            emotionalState = intenseEmotions,
            memoryActivation = highMemoryLoad
        )

        // Assert
        assertNotNull("Should handle extreme cognitive load gracefully", consciousnessState)
        assertTrue("Cognitive load should be capped at maximum", consciousnessState.cognitiveLoad <= 1.0f)
        assertTrue("Awareness should remain functional under load", consciousnessState.awarenessLevel > 0.1f)
        
        // Should generate cognitive overload events
        val hasOverloadEvent = consciousnessState.consciousnessStream.any { 
            it.type == ConsciousnessEventType.COGNITIVE_OVERLOAD 
        }
        assertTrue("Should detect cognitive overload condition", hasOverloadEvent)
    }

    @Test
    fun `consciousness_state_should_maintain_coherence_over_time`() = runTest {
        // Arrange
        val baseStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "coherence_test",
            intensity = 0.6f,
            novelty = 0.4f,
            complexity = 0.5f,
            emotionalIntensity = 0.5f
        )
        
        val memoryActivation = MemoryActivation(0.5f, 0.6f, 0.4f)
        val emotions = mapOf("stability" to 0.7f)

        // Act - Multiple state updates to test coherence over time
        val states = mutableListOf<ConsciousnessState>()
        repeat(5) { iteration ->
            val stimuli = baseStimuli.copy(
                intensity = 0.6f + (iteration * 0.05f),
                novelty = 0.4f + (iteration * 0.02f)
            )
            
            val state = consciousnessStateManager.updateConsciousnessState(
                conversationStimuli = stimuli,
                emotionalState = emotions,
                memoryActivation = memoryActivation
            )
            states.add(state)
        }

        // Assert
        assertTrue("Should generate multiple coherent states", states.size == 5)
        
        // Check coherence trends
        val coherenceValues = states.map { it.stateCoherence }
        assertTrue("All coherence values should be reasonable",
                  coherenceValues.all { it in 0.3f..1.0f })
        
        // Check awareness trends
        val awarenessValues = states.map { it.awarenessLevel }
        assertTrue("Awareness should show gradual adaptation",
                  awarenessValues.zipWithNext().all { (prev, next) -> 
                      kotlin.math.abs(next - prev) < 0.3f 
                  })
    }

    @Test
    fun `attention_focus_should_adapt_to_conversation_topics`() = runTest {
        // Arrange
        val emotionalStimuli = ConversationStimuli(
            type = StimuliType.EMOTIONAL_TRIGGER,
            primaryTopic = "emotional_support",
            intensity = 0.7f,
            novelty = 0.5f,
            complexity = 0.4f,
            emotionalIntensity = 0.8f
        )
        
        val analyticalStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "logical_analysis",
            intensity = 0.6f,
            novelty = 0.3f,
            complexity = 0.9f,
            emotionalIntensity = 0.2f
        )

        // Act
        val emotionalState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = emotionalStimuli,
            emotionalState = mapOf("empathy" to 0.8f),
            memoryActivation = MemoryActivation(0.5f, 0.6f, 0.3f)
        )
        
        val analyticalState = consciousnessStateManager.updateConsciousnessState(
            conversationStimuli = analyticalStimuli,
            emotionalState = mapOf("focus" to 0.7f),
            memoryActivation = MemoryActivation(0.6f, 0.7f, 0.5f)
        )

        // Assert
        assertEquals("Emotional focus should match topic", 
                    "emotional_support", emotionalState.attentionFocus.primaryFocus)
        assertEquals("Analytical focus should match topic",
                    "logical_analysis", analyticalState.attentionFocus.primaryFocus)
        
        assertTrue("Emotional state should have high attention intensity",
                  emotionalState.attentionFocus.intensity > 0.6f)
        assertTrue("Analytical state should reflect complexity in cognitive load",
                  analyticalState.cognitiveLoad > emotionalState.cognitiveLoad)
    }
}