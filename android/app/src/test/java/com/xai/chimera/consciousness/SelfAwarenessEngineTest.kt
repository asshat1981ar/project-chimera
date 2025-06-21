package com.xai.chimera.consciousness

import com.xai.chimera.domain.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

/**
 * Comprehensive test suite for SelfAwarenessEngine
 * Validates consciousness-inspired self-awareness behaviors
 */
class SelfAwarenessEngineTest {

    private lateinit var selfAwarenessEngine: SelfAwarenessEngine
    private lateinit var testPlayer: Player
    private lateinit var testConversations: List<DialogueEntry>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        selfAwarenessEngine = SelfAwarenessEngine()
        
        // Create test player with consciousness profiles
        testPlayer = Player(
            id = "test_player_001",
            name = "Test User",
            emotions = mapOf("curiosity" to 0.7f, "openness" to 0.6f),
            conversationPersonality = ConversationPersonality(
                communicationStyle = CommunicationStyle.EMPATHETIC,
                curiosityLevel = 0.8f,
                emotionalOpenness = 0.7f
            ),
            emotionalProfile = EmotionalProfile(
                emotionalStability = 0.6f,
                empathyLevel = 0.8f,
                emotionalGrowthRate = 0.1f
            )
        )
        
        // Create test conversation history
        testConversations = listOf(
            DialogueEntry(
                id = "conv_001",
                text = "I'm feeling curious about learning new things today",
                timestamp = System.currentTimeMillis() - 3600000,
                emotions = mapOf("curiosity" to 0.8f, "excitement" to 0.6f),
                emotionalIntensity = 0.7f,
                topicTags = listOf("learning", "personal_growth")
            ),
            DialogueEntry(
                id = "conv_002", 
                text = "That's a fascinating perspective on emotional intelligence",
                timestamp = System.currentTimeMillis() - 1800000,
                emotions = mapOf("interest" to 0.7f, "contemplation" to 0.8f),
                emotionalIntensity = 0.6f,
                topicTags = listOf("emotions", "psychology")
            ),
            DialogueEntry(
                id = "conv_003",
                text = "I wonder if I'm being consistent in my communication style",
                timestamp = System.currentTimeMillis() - 900000,
                emotions = mapOf("uncertainty" to 0.5f, "self_reflection" to 0.9f),
                emotionalIntensity = 0.7f,
                topicTags = listOf("self_awareness", "communication")
            )
        )
    }

    @Test
    fun `generateSelfAwarenessInsight should produce valid awareness metrics`() = runTest {
        // Act
        val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
            player = testPlayer,
            recentConversations = testConversations,
            currentEmotionalState = testPlayer.emotions
        )

        // Assert
        assertNotNull("Self-awareness insight should not be null", insight)
        assertTrue("Self-reflection level should be between 0 and 1", 
                  insight.selfReflectionLevel in 0f..1f)
        assertTrue("Consciousness coherence should be between 0 and 1",
                  insight.consciousnessCoherence in 0f..1f)
        assertNotNull("Behavior self-analysis should not be null", 
                     insight.behaviorSelfAnalysis)
        assertNotNull("Emotional self-awareness should not be null",
                     insight.emotionalSelfAwareness)
        assertTrue("Processing should complete within time limit",
                  insight.processingMetrics.efficiency >= 0f)
    }

    @Test
    fun `generateSelfAwarenessInsight should identify behavior patterns`() = runTest {
        // Act
        val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
            player = testPlayer,
            recentConversations = testConversations,
            currentEmotionalState = testPlayer.emotions
        )

        // Assert
        val behaviorAnalysis = insight.behaviorSelfAnalysis
        assertFalse("Response patterns should be identified", 
                   behaviorAnalysis.responsePatterns.isEmpty())
        assertTrue("Behavior consistency should be calculated",
                  behaviorAnalysis.behaviorConsistency >= 0f)
        assertTrue("Personality expression should be measured",
                  behaviorAnalysis.personalityExpression >= 0f)
        assertTrue("Behavioral self-recognition should be present",
                  behaviorAnalysis.behavioralSelfRecognition >= 0f)
    }

    @Test
    fun `generateSelfAwarenessInsight should generate self-directed questions`() = runTest {
        // Act
        val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
            player = testPlayer,
            recentConversations = testConversations,
            currentEmotionalState = testPlayer.emotions
        )

        // Assert
        assertFalse("Self-directed questions should be generated",
                   insight.selfDirectedQuestions.isEmpty())
        assertTrue("Should generate reasonable number of questions",
                  insight.selfDirectedQuestions.size in 1..10)
        
        // Verify questions are actually questions
        insight.selfDirectedQuestions.forEach { question ->
            assertTrue("Generated questions should end with '?'", 
                      question.trim().endsWith('?'))
            assertTrue("Questions should be meaningful length",
                      question.length > 10)
        }
    }

    @Test
    fun `generateSelfAwarenessInsight should detect metacognitive insights`() = runTest {
        // Act
        val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
            player = testPlayer,
            recentConversations = testConversations,
            currentEmotionalState = testPlayer.emotions
        )

        // Assert
        assertNotNull("Metacognitive insights should be present",
                     insight.metacognitiveInsights)
        
        insight.metacognitiveInsights.forEach { metacognitiveInsight ->
            assertNotNull("Insight type should be specified", metacognitiveInsight.type)
            assertNotNull("Insight description should exist", metacognitiveInsight.description)
            assertTrue("Confidence should be between 0 and 1",
                      metacognitiveInsight.confidence in 0f..1f)
            assertFalse("Actionable insight should not be empty",
                       metacognitiveInsight.actionableInsight.isBlank())
        }
    }

    @Test
    fun `performSelfExamination should analyze response consistency`() = runTest {
        // Act
        val examination = selfAwarenessEngine.performSelfExamination(
            conversationHistory = testConversations,
            personalityEvolution = testPlayer.conversationPersonality
        )

        // Assert
        assertNotNull("Self-examination result should not be null", examination)
        
        val patternAnalysis = examination.responsePatternAnalysis
        assertNotNull("Response pattern analysis should exist", patternAnalysis)
        assertTrue("Overall consistency should be measured",
                  patternAnalysis.overallConsistency in 0f..1f)
        assertTrue("Adaptability index should be calculated",
                  patternAnalysis.adaptabilityIndex in 0f..1f)
        
        val consistencyAnalysis = examination.personalityConsistencyAnalysis
        assertTrue("Personality consistency should be measured",
                  consistencyAnalysis.overallConsistency in 0f..1f)
        assertTrue("Authenticity score should be calculated",
                  consistencyAnalysis.authenticityScore in 0f..1f)
    }

    @Test
    fun `performSelfExamination should identify uncertainty patterns`() = runTest {
        // Act
        val examination = selfAwarenessEngine.performSelfExamination(
            conversationHistory = testConversations,
            personalityEvolution = testPlayer.conversationPersonality
        )

        // Assert
        val uncertaintyAnalysis = examination.uncertaintyIdentification
        assertNotNull("Uncertainty identification should exist", uncertaintyAnalysis)
        assertTrue("Overall uncertainty level should be measured",
                  uncertaintyAnalysis.overallUncertaintyLevel in 0f..1f)
        assertNotNull("Uncertainty trends should be identified",
                     uncertaintyAnalysis.uncertaintyTrends)
        assertNotNull("Confidence areas should be identified",
                     uncertaintyAnalysis.confidenceAreas)
    }

    @Test
    fun `performSelfExamination should generate improvement insights`() = runTest {
        // Act
        val examination = selfAwarenessEngine.performSelfExamination(
            conversationHistory = testConversations,
            personalityEvolution = testPlayer.conversationPersonality
        )

        // Assert
        assertNotNull("Self-improvement insights should exist",
                     examination.selfImprovementInsights)
        
        examination.selfImprovementInsights.forEach { insight ->
            assertFalse("Improvement area should be specified", insight.area.isBlank())
            assertTrue("Current level should be between 0 and 1",
                      insight.currentLevel in 0f..1f)
            assertTrue("Target level should be between 0 and 1",
                      insight.targetLevel in 0f..1f)
            assertFalse("Recommended actions should not be empty",
                       insight.recommendedActions.isEmpty())
            assertFalse("Improvement strategy should be specified",
                       insight.improvementStrategy.isBlank())
        }
    }

    @Test
    fun `generateInternalMonologue should create consciousness commentary`() = runTest {
        // Arrange
        val currentConversation = testConversations.last()
        val conversationContext = EnhancedConversationContext(
            primaryTopics = listOf("self_awareness"),
            emotionalTone = "contemplative",
            relationshipDepth = 0.6f,
            conversationGoals = listOf("understanding"),
            contextualReferences = emptyList(),
            suggestedResponseStyle = ResponseStyle.BALANCED_CONVERSATIONAL
        )
        val consciousnessState = ConsciousnessState(
            awarenessLevel = 0.8f,
            metacognitionLevel = 0.7f
        )

        // Act
        val monologue = selfAwarenessEngine.generateInternalMonologue(
            currentConversation = currentConversation,
            conversationContext = conversationContext,
            consciousnessState = consciousnessState
        )

        // Assert
        assertNotNull("Internal monologue should be generated", monologue)
        assertFalse("Conversation reflection should not be empty",
                   monologue.conversationReflection.isBlank())
        assertFalse("Interaction thoughts should not be empty",
                   monologue.interactionThoughts.isEmpty())
        assertFalse("Emotional commentary should not be empty",
                   monologue.emotionalCommentary.isBlank())
        assertFalse("Consciousness narrative should not be empty",
                   monologue.consciousnessNarrative.isBlank())
    }

    @Test
    fun `trackSelfAwarenessEvolution should measure growth over time`() = runTest {
        // Arrange
        val historicalInsights = listOf(
            createMockSelfAwarenessInsight(0.5f, System.currentTimeMillis() - 86400000),
            createMockSelfAwarenessInsight(0.6f, System.currentTimeMillis() - 43200000),
            createMockSelfAwarenessInsight(0.7f, System.currentTimeMillis())
        )

        // Act
        val evolution = selfAwarenessEngine.trackSelfAwarenessEvolution(
            player = testPlayer,
            historicalInsights = historicalInsights
        )

        // Assert
        assertNotNull("Self-awareness evolution should be tracked", evolution)
        assertNotNull("Awareness trajectory should be calculated", evolution.awarenessTrajectory)
        assertTrue("Metacognitive growth should be measured",
                  evolution.metacognitiveGrowth >= 0f)
        assertTrue("Consciousness maturation should be calculated",
                  evolution.consciousnessMaturation >= 0f)
        assertTrue("Self-awareness acceleration should be measured",
                  evolution.selfAwarenessAcceleration >= 0f)
        assertFalse("Emergent awareness capabilities should be identified",
                   evolution.emergentAwarenessCapabilities.isEmpty())
    }

    @Test
    fun `self_awareness_engine_should_handle_empty_conversation_history`() = runTest {
        // Act
        val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
            player = testPlayer,
            recentConversations = emptyList(),
            currentEmotionalState = testPlayer.emotions
        )

        // Assert
        assertNotNull("Should handle empty conversation history gracefully", insight)
        assertTrue("Self-reflection level should still be calculated",
                  insight.selfReflectionLevel >= 0f)
        assertNotNull("Behavior analysis should still work", insight.behaviorSelfAnalysis)
    }

    @Test
    fun `self_awareness_engine_should_handle_high_cognitive_load`() = runTest {
        // Arrange
        val intensiveConversations = (1..50).map { index ->
            DialogueEntry(
                id = "intensive_$index",
                text = "Complex conversation with multiple emotional layers and philosophical depth requiring significant cognitive processing",
                timestamp = System.currentTimeMillis() - (index * 60000),
                emotions = mapOf("complexity" to 0.9f, "intensity" to 0.8f),
                emotionalIntensity = 0.9f,
                topicTags = listOf("complex", "philosophical", "demanding")
            )
        }

        // Act
        val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
            player = testPlayer,
            recentConversations = intensiveConversations,
            currentEmotionalState = mapOf("cognitive_load" to 0.9f)
        )

        // Assert
        assertNotNull("Should handle high cognitive load scenarios", insight)
        assertTrue("Should complete processing within reasonable time",
                  insight.processingMetrics.processingTime < 5000L)
        assertTrue("Should maintain consciousness coherence under load",
                  insight.consciousnessCoherence > 0.3f)
    }

    @Test
    fun `self_awareness_engine_should_detect_personality_inconsistencies`() = runTest {
        // Arrange
        val inconsistentConversations = listOf(
            DialogueEntry(
                id = "consistent_1",
                text = "I really empathize with your emotional situation",
                timestamp = System.currentTimeMillis() - 3600000,
                emotions = mapOf("empathy" to 0.9f),
                emotionalIntensity = 0.8f,
                topicTags = listOf("empathetic", "emotional")
            ),
            DialogueEntry(
                id = "inconsistent_1",
                text = "That's completely illogical and irrelevant",
                timestamp = System.currentTimeMillis() - 1800000,
                emotions = mapOf("coldness" to 0.8f),
                emotionalIntensity = 0.3f,
                topicTags = listOf("analytical", "dismissive")
            )
        )

        // Act
        val examination = selfAwarenessEngine.performSelfExamination(
            conversationHistory = inconsistentConversations,
            personalityEvolution = testPlayer.conversationPersonality
        )

        // Assert
        assertTrue("Should detect personality inconsistencies",
                  examination.personalityConsistencyAnalysis.consistencyVariance > 0.1f)
        assertTrue("Should identify improvement opportunities",
                  examination.selfImprovementInsights.isNotEmpty())
        assertTrue("Consciousness integrity should reflect inconsistency",
                  examination.consciousnessIntegrity < 0.9f)
    }

    // Helper method to create mock insights for testing evolution
    private fun createMockSelfAwarenessInsight(reflectionLevel: Float, timestamp: Long): SelfAwarenessInsight {
        return SelfAwarenessInsight(
            selfReflectionLevel = reflectionLevel,
            behaviorSelfAnalysis = BehaviorSelfAnalysis(
                responsePatterns = emptyList(),
                behaviorConsistency = reflectionLevel,
                personalityExpression = reflectionLevel,
                adaptationPatterns = emptyList(),
                behavioralSelfRecognition = reflectionLevel
            ),
            emotionalSelfAwareness = EmotionalSelfAwareness(
                emotionalClarity = reflectionLevel,
                emotionalAccuracy = reflectionLevel,
                emotionalMetaAwareness = reflectionLevel,
                emotionalSelfRegulation = reflectionLevel,
                emotionalInsight = listOf("Growing awareness")
            ),
            metacognitiveInsights = emptyList(),
            consciousnessCoherence = reflectionLevel,
            selfDirectedQuestions = listOf("Am I growing in self-awareness?"),
            processingMetrics = ProcessingMetrics(100L, 200L),
            awarenessEvolution = AwarenessEvolutionTracking(0.05f, 0.1f, 0.08f, 0.03f)
        )
    }
}