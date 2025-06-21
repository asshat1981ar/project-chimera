package com.xai.chimera.consciousness

import com.xai.chimera.domain.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

/**
 * Comprehensive test suite for EmergentBehaviorEngine
 * Validates emergent behavior generation and genetic algorithm functionality
 */
class EmergentBehaviorEngineTest {

    private lateinit var emergentBehaviorEngine: EmergentBehaviorEngine
    private lateinit var testPersonality: ConversationPersonality
    private lateinit var testConversationHistory: List<DialogueEntry>
    private lateinit var testConsciousnessState: ConsciousnessState

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        emergentBehaviorEngine = EmergentBehaviorEngine()
        
        testPersonality = ConversationPersonality(
            communicationStyle = CommunicationStyle.EMPATHETIC,
            curiosityLevel = 0.8f,
            emotionalOpenness = 0.7f,
            humorPreference = HumorStyle.WITTY,
            preferredConversationDepth = ConversationDepth.DEEP
        )
        
        testConversationHistory = listOf(
            DialogueEntry(
                id = "history_001",
                text = "I find emotional intelligence fascinating and deeply meaningful",
                timestamp = System.currentTimeMillis() - 3600000,
                emotions = mapOf("fascination" to 0.8f, "depth" to 0.7f),
                emotionalIntensity = 0.75f,
                topicTags = listOf("emotional_intelligence", "meaningful")
            ),
            DialogueEntry(
                id = "history_002",
                text = "Sometimes I wonder if I'm expressing my authentic self",
                timestamp = System.currentTimeMillis() - 1800000,
                emotions = mapOf("curiosity" to 0.7f, "uncertainty" to 0.5f),
                emotionalIntensity = 0.6f,
                topicTags = listOf("authenticity", "self_reflection")
            )
        )
        
        testConsciousnessState = ConsciousnessState(
            awarenessLevel = 0.8f,
            attentionFocus = AttentionFocus(
                primaryFocus = "behavior_analysis",
                intensity = 0.7f,
                coherence = 0.8f
            ),
            cognitiveLoad = 0.5f,
            metacognitionLevel = 0.7f,
            consciousnessQuality = ConsciousnessQuality(overallQuality = 0.75f)
        )
    }

    @Test
    fun `generateEmergentBehavior should create valid emergent traits`() = runTest {
        // Arrange
        val contextualPressures = listOf(
            ContextualPressure(
                pressureType = "emotional_depth",
                intensity = 0.8f,
                selectionStrength = 0.7f,
                affectedTraits = listOf("emotional_openness", "curiosity")
            ),
            ContextualPressure(
                pressureType = "authenticity_seeking",
                intensity = 0.6f,
                selectionStrength = 0.6f,
                affectedTraits = listOf("curiosity", "communication_directness")
            )
        )

        // Act
        val emergentResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = contextualPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = testConsciousnessState
        )

        // Assert
        assertNotNull("Emergent behavior result should not be null", emergentResult)
        assertNotNull("Emergent traits should be generated", emergentResult.emergentTraits)
        assertNotNull("Behavior genes should be present", emergentResult.behaviorGenes)
        assertTrue("Coherence score should be between 0 and 1",
                  emergentResult.coherenceScore in 0f..1f)
        assertTrue("Emergence level should be between 0 and 1",
                  emergentResult.emergenceLevel in 0f..1f)
        assertTrue("Behavior novelty should be between 0 and 1",
                  emergentResult.behaviorNovelty in 0f..1f)
        assertTrue("Evolutionary fitness should be between 0 and 1",
                  emergentResult.evolutionaryFitness in 0f..1f)
    }

    @Test
    fun `generateEmergentBehavior should respond to contextual pressures`() = runTest {
        // Arrange
        val highPressures = listOf(
            ContextualPressure(
                pressureType = "intense_emotional_demand",
                intensity = 0.9f,
                selectionStrength = 0.8f,
                affectedTraits = listOf("emotional_openness")
            )
        )
        
        val lowPressures = listOf(
            ContextualPressure(
                pressureType = "casual_interaction",
                intensity = 0.2f,
                selectionStrength = 0.3f,
                affectedTraits = listOf("emotional_openness")
            )
        )

        // Act
        val highPressureResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = highPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = testConsciousnessState
        )
        
        val lowPressureResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = lowPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = testConsciousnessState
        )

        // Assert
        assertTrue("High pressure should generate more emergent behaviors",
                  highPressureResult.emergentTraits.size >= lowPressureResult.emergentTraits.size)
        assertTrue("High pressure should increase emergence level",
                  highPressureResult.emergenceLevel >= lowPressureResult.emergenceLevel)
    }

    @Test
    fun `generateEmergentBehavior should create emergent responses`() = runTest {
        // Arrange
        val contextualPressures = listOf(
            ContextualPressure(
                pressureType = "creative_expression",
                intensity = 0.7f,
                selectionStrength = 0.6f,
                affectedTraits = listOf("curiosity", "emotional_openness")
            )
        )

        // Act
        val emergentResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = contextualPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = testConsciousnessState
        )

        // Assert
        assertFalse("Emergent responses should be generated",
                   emergentResult.emergentResponses.isEmpty())
        
        emergentResult.emergentResponses.forEach { response ->
            assertNotNull("Response trait should be linked", response.trait)
            assertFalse("Response style should be specified", response.responseStyle.isBlank())
            assertFalse("Response content should not be empty", response.content.isBlank())
            assertTrue("Novelty should be between 0 and 1", response.novelty in 0f..1f)
            assertTrue("Coherence should be between 0 and 1", 
                      response.coherenceWithPersonality in 0f..1f)
            assertTrue("Unexpectedness should be between 0 and 1",
                      response.unexpectedness in 0f..1f)
        }
    }

    @Test
    fun `evolveBehaviorDNA should improve fitness over generations`() = runTest {
        // Arrange
        val initialDNA = BehaviorDNA(
            genes = listOf(
                BehaviorGene("empathy", 0.6f, 0.5f, 0.08f, GeneFamily.EMOTIONAL),
                BehaviorGene("curiosity", 0.7f, 0.6f, 0.08f, GeneFamily.EXPLORATION),
                BehaviorGene("communication", 0.5f, 0.4f, 0.08f, GeneFamily.COMMUNICATION)
            ),
            generation = 1,
            evolutionHistory = emptyList(),
            fitness = 0.5f
        )
        
        val successfulFeedback = listOf(
            InteractionFeedback(
                behaviorType = "empathy_expression",
                success = 0.9f,
                userSatisfaction = 0.8f,
                contextRelevance = 0.7f,
                contextFactors = listOf("emotional_support", "understanding")
            ),
            InteractionFeedback(
                behaviorType = "curious_inquiry",
                success = 0.8f,
                userSatisfaction = 0.9f,
                contextRelevance = 0.8f,
                contextFactors = listOf("learning", "exploration")
            )
        )
        
        val environmentalPressures = EnvironmentalPressures(
            pressures = mapOf(
                "emotional_support_demand" to 0.8f,
                "intellectual_stimulation" to 0.7f
            )
        )

        // Act
        val evolvedDNA = emergentBehaviorEngine.evolveBehaviorDNA(
            currentDNA = initialDNA,
            interactionFeedback = successfulFeedback,
            environmentalPressures = environmentalPressures
        )

        // Assert
        assertNotNull("Evolved DNA should not be null", evolvedDNA)
        assertEquals("Generation should increment", initialDNA.generation + 1, evolvedDNA.generation)
        assertTrue("Fitness should improve or maintain",
                  evolvedDNA.fitness >= initialDNA.fitness)
        assertFalse("Evolution history should be recorded",
                   evolvedDNA.evolutionHistory.isEmpty())
        
        // Check that successful traits are enhanced
        val empathyGene = evolvedDNA.genes.find { it.trait.contains("empathy") }
        assertNotNull("Empathy gene should be preserved or enhanced", empathyGene)
    }

    @Test
    fun `generateSpontaneousBehavior should create novel responses to unexpected situations`() = runTest {
        // Arrange
        val unexpectedSituations = listOf(
            UnexpectedSituation(
                type = "conversation_topic_shift",
                intensity = 0.8f,
                novelty = 0.9f
            ),
            UnexpectedSituation(
                type = "emotional_crisis",
                intensity = 0.9f,
                novelty = 0.7f
            )
        )
        
        val currentMood = EmotionalState(
            dominantEmotion = "curiosity",
            intensity = 0.7f,
            stability = 0.6f
        )

        // Act
        val spontaneousManifest = emergentBehaviorEngine.generateSpontaneousBehavior(
            basePersonality = testPersonality,
            currentMood = currentMood,
            unexpectedSituations = unexpectedSituations
        )

        // Assert
        assertNotNull("Spontaneous behavior manifest should not be null", spontaneousManifest)
        assertEquals("Should have triggers for each situation",
                    unexpectedSituations.size, spontaneousManifest.behaviorTriggers.size)
        assertFalse("Novel responses should be generated",
                   spontaneousManifest.novelResponses.isEmpty())
        assertFalse("Behavior experiments should be created",
                   spontaneousManifest.behaviorExperiments.isEmpty())
        assertTrue("Spontaneity level should be between 0 and 1",
                  spontaneousManifest.spontaneityLevel in 0f..1f)
        assertTrue("Adaptive value should be between 0 and 1",
                  spontaneousManifest.adaptiveValue in 0f..1f)
    }

    @Test
    fun `simulateBehaviorConsciousnessEmergence should detect emergence patterns`() = runTest {
        // Arrange
        val behaviorHistory = (1..20).map { index ->
            BehaviorEvent(
                eventType = "adaptive_response_$index",
                complexity = 0.3f + (index * 0.03f), // Gradually increasing complexity
                systemIntegration = 0.5f + (index * 0.02f),
                timestamp = System.currentTimeMillis() - (index * 60000)
            )
        }
        
        val complexityThreshold = 0.7f

        // Act
        val emergence = emergentBehaviorEngine.simulateBehaviorConsciousnessEmergence(
            behaviorHistory = behaviorHistory,
            complexityThreshold = complexityThreshold,
            consciousnessState = testConsciousnessState
        )

        // Assert
        assertNotNull("Consciousness emergence should not be null", emergence)
        assertNotNull("Complexity evolution should be tracked", emergence.complexityEvolution)
        assertFalse("Emergence indicators should be identified",
                   emergence.emergenceIndicators.isEmpty())
        assertTrue("Emergence probability should be between 0 and 1",
                  emergence.emergenceProbability in 0f..1f)
        assertNotNull("Emergence phase should be determined", emergence.emergencePhase)
        assertTrue("Behavior system integration should be measured",
                  emergence.behaviorSystemIntegration in 0f..1f)
        
        // Check if complexity growth was detected
        assertTrue("Should detect complexity growth from behavior history",
                  emergence.complexityEvolution.growthRate > 0f)
    }

    @Test
    fun `behavior_genes_should_mutate_appropriately`() = runTest {
        // Arrange
        val basePersonality = testPersonality.copy(curiosityLevel = 0.9f) // High curiosity
        val highAwarenessState = testConsciousnessState.copy(awarenessLevel = 0.9f)
        
        val contextualPressures = listOf(
            ContextualPressure(
                pressureType = "novelty_seeking",
                intensity = 0.8f,
                selectionStrength = 0.7f,
                affectedTraits = listOf("curiosity", "emotional_openness")
            )
        )

        // Act - Run multiple times to test mutation variability
        val results = (1..5).map {
            emergentBehaviorEngine.generateEmergentBehavior(
                basePersonality = basePersonality,
                contextualPressures = contextualPressures,
                conversationHistory = testConversationHistory,
                consciousnessState = highAwarenessState
            )
        }

        // Assert
        val behaviorGenes = results.flatMap { it.behaviorGenes }
        assertTrue("Should generate behavior genes", behaviorGenes.isNotEmpty())
        
        // Check for gene variety (mutations should create variation)
        val geneExpressions = behaviorGenes.map { it.expression }
        val expressionVariance = geneExpressions.let { expressions ->
            val mean = expressions.average()
            expressions.map { (it - mean) * (it - mean) }.average()
        }
        
        assertTrue("Gene expressions should show some variation from mutations",
                  expressionVariance > 0.01)
    }

    @Test
    fun `emergent_traits_should_maintain_coherence_with_personality`() = runTest {
        // Arrange
        val stablePersonality = testPersonality.copy(
            communicationStyle = CommunicationStyle.ANALYTICAL,
            curiosityLevel = 0.9f,
            emotionalOpenness = 0.3f // Low emotional openness
        )
        
        val contextualPressures = listOf(
            ContextualPressure(
                pressureType = "logical_analysis",
                intensity = 0.8f,
                selectionStrength = 0.7f,
                affectedTraits = listOf("analytical_thinking")
            )
        )

        // Act
        val emergentResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = stablePersonality,
            contextualPressures = contextualPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = testConsciousnessState
        )

        // Assert
        assertTrue("Coherence score should be reasonable for stable personality",
                  emergentResult.coherenceScore > 0.5f)
        
        // Emergent responses should align with analytical style
        emergentResult.emergentResponses.forEach { response ->
            assertTrue("Responses should maintain personality coherence",
                      response.coherenceWithPersonality > 0.4f)
        }
    }

    @Test
    fun `behavior_evolution_should_handle_conflicting_pressures`() = runTest {
        // Arrange
        val conflictingPressures = listOf(
            ContextualPressure(
                pressureType = "emotional_connection",
                intensity = 0.8f,
                selectionStrength = 0.7f,
                affectedTraits = listOf("emotional_openness")
            ),
            ContextualPressure(
                pressureType = "logical_detachment",
                intensity = 0.8f,
                selectionStrength = 0.7f,
                affectedTraits = listOf("emotional_openness") // Opposite pressure on same trait
            )
        )

        // Act
        val emergentResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = conflictingPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = testConsciousnessState
        )

        // Assert
        assertNotNull("Should handle conflicting pressures gracefully", emergentResult)
        assertTrue("Should maintain reasonable coherence despite conflicts",
                  emergentResult.coherenceScore > 0.3f)
        assertTrue("Should generate some emergent behavior despite conflicts",
                  emergentResult.emergenceLevel > 0.1f)
    }

    @Test
    fun `emergent_behavior_should_scale_with_consciousness_level`() = runTest {
        // Arrange
        val lowConsciousnessState = testConsciousnessState.copy(
            awarenessLevel = 0.3f,
            metacognitionLevel = 0.2f
        )
        
        val highConsciousnessState = testConsciousnessState.copy(
            awarenessLevel = 0.9f,
            metacognitionLevel = 0.8f
        )
        
        val contextualPressures = listOf(
            ContextualPressure(
                pressureType = "consciousness_expansion",
                intensity = 0.7f,
                selectionStrength = 0.6f,
                affectedTraits = listOf("curiosity", "emotional_openness")
            )
        )

        // Act
        val lowConsciousnessResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = contextualPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = lowConsciousnessState
        )
        
        val highConsciousnessResult = emergentBehaviorEngine.generateEmergentBehavior(
            basePersonality = testPersonality,
            contextualPressures = contextualPressures,
            conversationHistory = testConversationHistory,
            consciousnessState = highConsciousnessState
        )

        // Assert
        assertTrue("High consciousness should generate more emergence",
                  highConsciousnessResult.emergenceLevel > lowConsciousnessResult.emergenceLevel)
        assertTrue("High consciousness should increase behavior novelty",
                  highConsciousnessResult.behaviorNovelty >= lowConsciousnessResult.behaviorNovelty)
        assertTrue("High consciousness should generate more emergent traits",
                  highConsciousnessResult.emergentTraits.size >= lowConsciousnessResult.emergentTraits.size)
    }
}