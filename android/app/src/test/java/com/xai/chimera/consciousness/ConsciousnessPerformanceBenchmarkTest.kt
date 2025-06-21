package com.xai.chimera.consciousness

import com.xai.chimera.domain.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 * Performance benchmark test suite for consciousness systems
 * Validates that consciousness features meet mobile performance requirements
 */
class ConsciousnessPerformanceBenchmarkTest {

    private lateinit var selfAwarenessEngine: SelfAwarenessEngine
    private lateinit var consciousnessStateManager: ConsciousnessStateManager
    private lateinit var emergentBehaviorEngine: EmergentBehaviorEngine
    
    private lateinit var testPlayer: Player
    private lateinit var testConversations: List<DialogueEntry>
    private lateinit var testConsciousnessState: ConsciousnessState

    companion object {
        // Performance thresholds for mobile devices
        private const val MAX_SELF_AWARENESS_TIME_MS = 200L
        private const val MAX_CONSCIOUSNESS_UPDATE_TIME_MS = 150L
        private const val MAX_EMERGENT_BEHAVIOR_TIME_MS = 300L
        private const val MAX_MEMORY_USAGE_MB = 50
        private const val MAX_BATCH_PROCESSING_TIME_MS = 1000L
    }

    @Before
    fun setup() {
        selfAwarenessEngine = SelfAwarenessEngine()
        consciousnessStateManager = ConsciousnessStateManager()
        emergentBehaviorEngine = EmergentBehaviorEngine()
        
        testPlayer = Player(
            id = "perf_test_player",
            name = "Performance Test User",
            emotions = mapOf("curiosity" to 0.8f, "focus" to 0.7f),
            conversationPersonality = ConversationPersonality(
                communicationStyle = CommunicationStyle.BALANCED,
                curiosityLevel = 0.7f,
                emotionalOpenness = 0.6f
            ),
            emotionalProfile = EmotionalProfile(
                emotionalStability = 0.8f,
                empathyLevel = 0.7f
            )
        )
        
        // Create comprehensive conversation history for performance testing
        testConversations = (1..50).map { index ->
            DialogueEntry(
                id = "perf_conv_$index",
                text = "Performance test conversation $index with various emotional complexities and topics",
                timestamp = System.currentTimeMillis() - (index * 60000),
                emotions = mapOf(
                    "test_emotion_${index % 5}" to (0.5f + (index % 10) * 0.05f),
                    "complexity" to (0.3f + (index % 7) * 0.1f)
                ),
                emotionalIntensity = 0.5f + (index % 10) * 0.05f,
                topicTags = listOf("performance", "test_$index", "benchmark")
            )
        }
        
        testConsciousnessState = ConsciousnessState(
            awarenessLevel = 0.8f,
            attentionFocus = AttentionFocus(
                primaryFocus = "performance_testing",
                intensity = 0.7f,
                coherence = 0.8f
            ),
            cognitiveLoad = 0.5f,
            metacognitionLevel = 0.6f
        )
    }

    @Test
    fun `self_awareness_analysis_should_complete_within_performance_threshold`() = runTest {
        // Arrange
        val iterations = 10
        val processingTimes = mutableListOf<Long>()

        // Act - Measure multiple iterations
        repeat(iterations) {
            val processingTime = measureTimeMillis {
                selfAwarenessEngine.generateSelfAwarenessInsight(
                    player = testPlayer,
                    recentConversations = testConversations.take(10),
                    currentEmotionalState = testPlayer.emotions
                )
            }
            processingTimes.add(processingTime)
        }

        // Assert
        val averageTime = processingTimes.average()
        val maxTime = processingTimes.maxOrNull() ?: 0L
        
        println("Self-Awareness Performance:")
        println("  Average time: ${averageTime}ms")
        println("  Max time: ${maxTime}ms")
        println("  Threshold: ${MAX_SELF_AWARENESS_TIME_MS}ms")
        
        assertTrue("Self-awareness analysis should complete within ${MAX_SELF_AWARENESS_TIME_MS}ms on average", 
                  averageTime <= MAX_SELF_AWARENESS_TIME_MS)
        assertTrue("Self-awareness analysis should never exceed ${MAX_SELF_AWARENESS_TIME_MS * 2}ms",
                  maxTime <= MAX_SELF_AWARENESS_TIME_MS * 2)
    }

    @Test
    fun `consciousness_state_updates_should_be_real_time_responsive`() = runTest {
        // Arrange
        val conversationStimuli = ConversationStimuli(
            type = StimuliType.CONVERSATION_INPUT,
            primaryTopic = "performance_test",
            intensity = 0.7f,
            novelty = 0.5f,
            complexity = 0.6f,
            emotionalIntensity = 0.8f
        )
        
        val memoryActivation = MemoryActivation(0.6f, 0.7f, 0.4f)
        val emotions = mapOf("test" to 0.7f)
        
        val iterations = 20
        val processingTimes = mutableListOf<Long>()

        // Act - Measure consciousness state updates
        repeat(iterations) {
            val processingTime = measureTimeMillis {
                consciousnessStateManager.updateConsciousnessState(
                    conversationStimuli = conversationStimuli,
                    emotionalState = emotions,
                    memoryActivation = memoryActivation
                )
            }
            processingTimes.add(processingTime)
        }

        // Assert
        val averageTime = processingTimes.average()
        val maxTime = processingTimes.maxOrNull() ?: 0L
        
        println("Consciousness State Performance:")
        println("  Average time: ${averageTime}ms")
        println("  Max time: ${maxTime}ms")
        println("  Threshold: ${MAX_CONSCIOUSNESS_UPDATE_TIME_MS}ms")
        
        assertTrue("Consciousness updates should complete within ${MAX_CONSCIOUSNESS_UPDATE_TIME_MS}ms on average",
                  averageTime <= MAX_CONSCIOUSNESS_UPDATE_TIME_MS)
        assertTrue("Consciousness updates should be consistently fast",
                  processingTimes.count { it > MAX_CONSCIOUSNESS_UPDATE_TIME_MS } < iterations * 0.2) // Allow 20% tolerance
    }

    @Test
    fun `emergent_behavior_generation_should_meet_performance_requirements`() = runTest {
        // Arrange
        val contextualPressures = listOf(
            ContextualPressure(
                pressureType = "performance_pressure",
                intensity = 0.8f,
                selectionStrength = 0.7f,
                affectedTraits = listOf("efficiency", "speed")
            )
        )
        
        val iterations = 5 // Fewer iterations as this is more complex
        val processingTimes = mutableListOf<Long>()

        // Act - Measure emergent behavior generation
        repeat(iterations) {
            val processingTime = measureTimeMillis {
                emergentBehaviorEngine.generateEmergentBehavior(
                    basePersonality = testPlayer.conversationPersonality,
                    contextualPressures = contextualPressures,
                    conversationHistory = testConversations.take(15),
                    consciousnessState = testConsciousnessState
                )
            }
            processingTimes.add(processingTime)
        }

        // Assert
        val averageTime = processingTimes.average()
        val maxTime = processingTimes.maxOrNull() ?: 0L
        
        println("Emergent Behavior Performance:")
        println("  Average time: ${averageTime}ms")
        println("  Max time: ${maxTime}ms")
        println("  Threshold: ${MAX_EMERGENT_BEHAVIOR_TIME_MS}ms")
        
        assertTrue("Emergent behavior should complete within ${MAX_EMERGENT_BEHAVIOR_TIME_MS}ms on average",
                  averageTime <= MAX_EMERGENT_BEHAVIOR_TIME_MS)
        assertTrue("Emergent behavior should not exceed ${MAX_EMERGENT_BEHAVIOR_TIME_MS * 2}ms",
                  maxTime <= MAX_EMERGENT_BEHAVIOR_TIME_MS * 2)
    }

    @Test
    fun `consciousness_idle_simulation_should_be_lightweight`() = runTest {
        // Arrange
        val simulationDuration = 2000L
        val backgroundThoughts = listOf("Optimizing performance", "Processing efficiently")

        // Act
        val processingTime = measureTimeMillis {
            consciousnessStateManager.simulateConsciousnessIdle(
                duration = simulationDuration,
                backgroundThoughts = backgroundThoughts
            )
        }

        // Assert
        println("Consciousness Idle Simulation Performance:")
        println("  Processing time: ${processingTime}ms")
        println("  Simulation duration: ${simulationDuration}ms")
        println("  Overhead: ${processingTime - simulationDuration}ms")
        
        // Processing time should not significantly exceed simulation duration
        val overhead = processingTime - simulationDuration
        assertTrue("Idle simulation overhead should be minimal",
                  overhead <= 500L) // Allow 500ms overhead for processing
    }

    @Test
    fun `batch_consciousness_processing_should_scale_efficiently`() = runTest {
        // Arrange
        val batchSizes = listOf(5, 10, 20, 30)
        val performanceResults = mutableMapOf<Int, Double>()

        // Act - Test different batch sizes
        batchSizes.forEach { batchSize ->
            val conversationBatch = testConversations.take(batchSize)
            
            val processingTime = measureTimeMillis {
                repeat(3) { // Process multiple batches to get average
                    selfAwarenessEngine.generateSelfAwarenessInsight(
                        player = testPlayer,
                        recentConversations = conversationBatch,
                        currentEmotionalState = testPlayer.emotions
                    )
                }
            }
            
            val averageTimePerBatch = processingTime.toDouble() / 3
            val timePerItem = averageTimePerBatch / batchSize
            performanceResults[batchSize] = timePerItem
        }

        // Assert
        println("Batch Processing Performance:")
        performanceResults.forEach { (batchSize, timePerItem) ->
            println("  Batch size $batchSize: ${timePerItem}ms per item")
        }
        
        // Processing time per item should not increase dramatically with batch size
        val smallBatchTime = performanceResults[5] ?: 0.0
        val largeBatchTime = performanceResults[30] ?: 0.0
        val scalingFactor = largeBatchTime / smallBatchTime
        
        assertTrue("Batch processing should scale reasonably (scaling factor < 3)",
                  scalingFactor < 3.0)
        
        // Total batch time should be reasonable
        val maxBatchTime = performanceResults.values.maxOrNull() ?: 0.0
        assertTrue("Individual item processing should be under 50ms",
                  maxBatchTime <= 50.0)
    }

    @Test
    fun `consciousness_memory_usage_should_be_mobile_friendly`() = runTest {
        // Arrange
        val runtime = Runtime.getRuntime()
        
        // Measure initial memory
        System.gc() // Force garbage collection
        Thread.sleep(100) // Allow GC to complete
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        // Act - Create consciousness objects and process data
        val consciousnessObjects = mutableListOf<Any>()
        
        repeat(100) { index ->
            // Create various consciousness objects
            val insight = selfAwarenessEngine.generateSelfAwarenessInsight(
                player = testPlayer,
                recentConversations = testConversations.take(5),
                currentEmotionalState = testPlayer.emotions
            )
            consciousnessObjects.add(insight)
            
            val state = consciousnessStateManager.updateConsciousnessState(
                conversationStimuli = ConversationStimuli(
                    type = StimuliType.CONVERSATION_INPUT,
                    primaryTopic = "memory_test_$index",
                    intensity = 0.5f,
                    novelty = 0.3f,
                    complexity = 0.4f,
                    emotionalIntensity = 0.6f
                ),
                emotionalState = mapOf("test" to 0.5f),
                memoryActivation = MemoryActivation(0.5f, 0.5f, 0.3f)
            )
            consciousnessObjects.add(state)
        }
        
        // Measure final memory
        System.gc()
        Thread.sleep(100)
        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsageMB = (finalMemory - initialMemory) / (1024 * 1024)

        // Assert
        println("Memory Usage Performance:")
        println("  Initial memory: ${initialMemory / (1024 * 1024)}MB")
        println("  Final memory: ${finalMemory / (1024 * 1024)}MB")
        println("  Consciousness objects memory: ${memoryUsageMB}MB")
        println("  Threshold: ${MAX_MEMORY_USAGE_MB}MB")
        
        assertTrue("Consciousness systems should use less than ${MAX_MEMORY_USAGE_MB}MB",
                  memoryUsageMB <= MAX_MEMORY_USAGE_MB)
        
        // Verify objects are functional
        assertTrue("Should create consciousness objects", consciousnessObjects.size == 200)
    }

    @Test
    fun `consciousness_state_transitions_should_be_smooth`() = runTest {
        // Arrange
        val transitionCount = 50
        val stateTransitions = mutableListOf<ConsciousnessState>()
        val transitionTimes = mutableListOf<Long>()

        // Act - Generate smooth state transitions
        repeat(transitionCount) { index ->
            val stimuli = ConversationStimuli(
                type = StimuliType.CONVERSATION_INPUT,
                primaryTopic = "transition_$index",
                intensity = 0.4f + (index * 0.01f), // Gradually increase intensity
                novelty = 0.3f + (index * 0.005f),
                complexity = 0.5f,
                emotionalIntensity = 0.6f + (index * 0.008f)
            )
            
            val transitionTime = measureTimeMillis {
                val state = consciousnessStateManager.updateConsciousnessState(
                    conversationStimuli = stimuli,
                    emotionalState = mapOf("transition" to 0.5f + (index * 0.01f)),
                    memoryActivation = MemoryActivation(0.5f, 0.6f, 0.3f)
                )
                stateTransitions.add(state)
            }
            transitionTimes.add(transitionTime)
        }

        // Assert
        val averageTransitionTime = transitionTimes.average()
        val maxTransitionTime = transitionTimes.maxOrNull() ?: 0L
        
        println("State Transition Performance:")
        println("  Average transition time: ${averageTransitionTime}ms")
        println("  Max transition time: ${maxTransitionTime}ms")
        println("  Transitions: $transitionCount")
        
        assertTrue("State transitions should be consistently fast",
                  averageTransitionTime <= MAX_CONSCIOUSNESS_UPDATE_TIME_MS)
        
        // Verify smoothness - states should show gradual progression
        val awarenessProgression = stateTransitions.map { it.awarenessLevel }
        val maxAwarenessJump = awarenessProgression.zipWithNext { prev, next -> 
            kotlin.math.abs(next - prev) 
        }.maxOrNull() ?: 0f
        
        assertTrue("Awareness transitions should be smooth (max jump < 0.3)",
                  maxAwarenessJump < 0.3f)
    }

    @Test
    fun `concurrent_consciousness_processing_should_handle_load`() = runTest {
        // Arrange
        val concurrentTasks = 10
        val tasksPerType = 5

        // Act - Run consciousness operations concurrently
        val processingTime = measureTimeMillis {
            val jobs = (1..concurrentTasks).map { taskId ->
                kotlinx.coroutines.async {
                    repeat(tasksPerType) { iteration ->
                        // Mix different consciousness operations
                        when (taskId % 3) {
                            0 -> selfAwarenessEngine.generateSelfAwarenessInsight(
                                player = testPlayer,
                                recentConversations = testConversations.take(5),
                                currentEmotionalState = testPlayer.emotions
                            )
                            1 -> consciousnessStateManager.updateConsciousnessState(
                                conversationStimuli = ConversationStimuli(
                                    type = StimuliType.CONVERSATION_INPUT,
                                    primaryTopic = "concurrent_$taskId",
                                    intensity = 0.6f,
                                    novelty = 0.4f,
                                    complexity = 0.5f,
                                    emotionalIntensity = 0.7f
                                ),
                                emotionalState = mapOf("concurrent" to 0.6f),
                                memoryActivation = MemoryActivation(0.5f, 0.6f, 0.4f)
                            )
                            else -> emergentBehaviorEngine.generateEmergentBehavior(
                                basePersonality = testPlayer.conversationPersonality,
                                contextualPressures = listOf(
                                    ContextualPressure(
                                        pressureType = "concurrent_pressure",
                                        intensity = 0.6f,
                                        selectionStrength = 0.5f,
                                        affectedTraits = listOf("efficiency")
                                    )
                                ),
                                conversationHistory = testConversations.take(8),
                                consciousnessState = testConsciousnessState
                            )
                        }
                    }
                }
            }
            
            // Wait for all tasks to complete
            jobs.forEach { it.await() }
        }

        // Assert
        val totalOperations = concurrentTasks * tasksPerType
        val averageTimePerOperation = processingTime.toDouble() / totalOperations
        
        println("Concurrent Processing Performance:")
        println("  Total time: ${processingTime}ms")
        println("  Total operations: $totalOperations")
        println("  Average time per operation: ${averageTimePerOperation}ms")
        println("  Concurrent tasks: $concurrentTasks")
        
        assertTrue("Concurrent processing should complete within reasonable time",
                  processingTime <= MAX_BATCH_PROCESSING_TIME_MS)
        assertTrue("Average operation time should remain reasonable under load",
                  averageTimePerOperation <= 100.0) // 100ms per operation under load
    }

    @Test
    fun `consciousness_system_memory_leaks_should_be_minimal`() = runTest {
        // Arrange
        val runtime = Runtime.getRuntime()
        val initialMemoryReadings = mutableListOf<Long>()
        val finalMemoryReadings = mutableListOf<Long>()

        // Act - Multiple cycles of consciousness processing
        repeat(5) { cycle ->
            System.gc()
            Thread.sleep(100)
            initialMemoryReadings.add(runtime.totalMemory() - runtime.freeMemory())
            
            // Perform consciousness operations
            repeat(20) { iteration ->
                selfAwarenessEngine.generateSelfAwarenessInsight(
                    player = testPlayer,
                    recentConversations = testConversations.take(10),
                    currentEmotionalState = testPlayer.emotions
                )
                
                consciousnessStateManager.updateConsciousnessState(
                    conversationStimuli = ConversationStimuli(
                        type = StimuliType.CONVERSATION_INPUT,
                        primaryTopic = "leak_test_${cycle}_$iteration",
                        intensity = 0.6f,
                        novelty = 0.4f,
                        complexity = 0.5f,
                        emotionalIntensity = 0.7f
                    ),
                    emotionalState = mapOf("leak_test" to 0.5f),
                    memoryActivation = MemoryActivation(0.5f, 0.6f, 0.4f)
                )
            }
            
            System.gc()
            Thread.sleep(100)
            finalMemoryReadings.add(runtime.totalMemory() - runtime.freeMemory())
        }

        // Assert
        val memoryDeltas = initialMemoryReadings.zip(finalMemoryReadings) { initial, final ->
            (final - initial) / (1024 * 1024) // Convert to MB
        }
        
        val averageMemoryGrowth = memoryDeltas.average()
        val maxMemoryGrowth = memoryDeltas.maxOrNull() ?: 0L
        
        println("Memory Leak Analysis:")
        println("  Memory deltas (MB): $memoryDeltas")
        println("  Average memory growth: ${averageMemoryGrowth}MB per cycle")
        println("  Max memory growth: ${maxMemoryGrowth}MB")
        
        assertTrue("Average memory growth should be minimal (<5MB per cycle)",
                  averageMemoryGrowth < 5.0)
        assertTrue("Max memory growth should be reasonable (<10MB)",
                  maxMemoryGrowth < 10)
    }
}