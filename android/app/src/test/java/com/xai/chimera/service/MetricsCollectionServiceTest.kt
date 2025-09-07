package com.xai.chimera.service

import com.xai.chimera.dao.SelfOptMetricsDao
import com.xai.chimera.domain.SelfOptMetrics
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for MetricsCollectionService
 */
class MetricsCollectionServiceTest {
    
    private lateinit var mockDao: SelfOptMetricsDao
    private lateinit var metricsService: MetricsCollectionService
    
    @Before
    fun setup() {
        mockDao = mock()
        metricsService = MetricsCollectionService(mockDao)
    }
    
    @Test
    fun `startRun should return unique run ID`() {
        // Act
        val runId1 = metricsService.startRun()
        val runId2 = metricsService.startRun()
        
        // Assert
        assertNotNull("Run ID should not be null", runId1)
        assertNotNull("Run ID should not be null", runId2)
        assertNotEquals("Run IDs should be unique", runId1, runId2)
        assertTrue("Run ID should be valid UUID format", runId1.contains("-"))
    }
    
    @Test
    fun `recordFetchError should track errors correctly`() = runTest {
        // Arrange
        metricsService.startRun()
        
        // Act
        metricsService.recordFetchError()
        metricsService.recordFetchError()
        metricsService.completeRun()
        
        // Assert
        argumentCaptor<SelfOptMetrics>().apply {
            verify(mockDao).insertMetrics(capture())
            assertEquals("Should record 2 fetch errors", 2, firstValue.fetchErrors)
        }
    }
    
    @Test
    fun `completeRun should store metrics with correct data`() = runTest {
        // Arrange
        val startTime = System.currentTimeMillis()
        metricsService.startRun()
        
        // Simulate some processing time
        Thread.sleep(10)
        
        // Act
        metricsService.completeRun(
            chunkSize = 2048,
            successRate = 0.95f,
            emotionProcessingTimeMs = 50L,
            consciousnessUpdateTimeMs = 75L,
            apiResponseTimeMs = 200L,
            playerEmotionVariance = 0.3f,
            conversationComplexity = 0.7f,
            additionalMetrics = mapOf("custom_metric" to 42.0f)
        )
        
        // Assert
        argumentCaptor<SelfOptMetrics>().apply {
            verify(mockDao).insertMetrics(capture())
            val metrics = firstValue
            
            assertNotNull("Metrics ID should not be null", metrics.id)
            assertTrue("Timestamp should be recent", 
                      metrics.timestamp >= startTime)
            assertTrue("Processing time should be positive", 
                      metrics.processingTimeMs > 0)
            assertEquals("Chunk size should match", 2048, metrics.chunkSize)
            assertEquals("Success rate should match", 0.95f, metrics.successRate, 0.01f)
            assertEquals("Emotion processing time should match", 50L, metrics.emotionProcessingTimeMs)
            assertEquals("Consciousness update time should match", 75L, metrics.consciousnessUpdateTimeMs)
            assertEquals("API response time should match", 200L, metrics.apiResponseTimeMs)
            assertEquals("Player emotion variance should match", 0.3f, metrics.playerEmotionVariance, 0.01f)
            assertEquals("Conversation complexity should match", 0.7f, metrics.conversationComplexity, 0.01f)
            assertTrue("Additional metrics should contain custom metric", 
                      metrics.additionalMetrics.containsKey("custom_metric"))
            assertEquals("Custom metric value should match", 42.0f, 
                        metrics.additionalMetrics["custom_metric"]!!, 0.01f)
        }
    }
    
    @Test
    fun `completeRun without startRun should handle gracefully`() = runTest {
        // Act - complete run without starting
        metricsService.completeRun()
        
        // Assert - should not call DAO
        verify(mockDao, never()).insertMetrics(any())
    }
    
    @Test
    fun `measureAndRecord should execute block and record timing`() = runTest {
        // Arrange
        metricsService.startRun()
        var blockExecuted = false
        
        // Act
        val result = metricsService.measureAndRecord("test_operation") {
            blockExecuted = true
            Thread.sleep(5) // Simulate some work
            "test_result"
        }
        
        // Assert
        assertEquals("Should return block result", "test_result", result)
        assertTrue("Block should have been executed", blockExecuted)
    }
    
    @Test
    fun `withMetrics should handle successful operations`() = runTest {
        // Arrange
        var operationExecuted = false
        
        // Act
        val result = metricsService.withMetrics("successful_op", chunkSize = 512) {
            operationExecuted = true
            "success"
        }
        
        // Assert
        assertEquals("Should return operation result", "success", result)
        assertTrue("Operation should have been executed", operationExecuted)
        
        // Verify metrics were stored
        argumentCaptor<SelfOptMetrics>().apply {
            verify(mockDao).insertMetrics(capture())
            val metrics = firstValue
            assertEquals("Chunk size should match", 512, metrics.chunkSize)
            assertEquals("Success rate should be 1.0", 1.0f, metrics.successRate, 0.01f)
            assertEquals("Fetch errors should be 0", 0, metrics.fetchErrors)
        }
    }
    
    @Test
    fun `withMetrics should handle failed operations`() = runTest {
        // Arrange
        val testException = RuntimeException("Test error")
        
        // Act & Assert
        try {
            metricsService.withMetrics("failing_op") {
                throw testException
            }
            fail("Should have thrown exception")
        } catch (e: RuntimeException) {
            assertEquals("Should preserve original exception", testException, e)
        }
        
        // Verify metrics were stored with failure
        argumentCaptor<SelfOptMetrics>().apply {
            verify(mockDao).insertMetrics(capture())
            val metrics = firstValue
            assertEquals("Success rate should be 0.0", 0.0f, metrics.successRate, 0.01f)
            assertEquals("Fetch errors should be 1", 1, metrics.fetchErrors)
        }
    }
    
    @Test
    fun `recordAdditionalMetric should log metric`() {
        // This test verifies the method doesn't crash and logs appropriately
        // In a real implementation, you might want to verify the logging output
        
        // Act
        metricsService.recordAdditionalMetric("test_metric", 123.45f)
        
        // Assert - no exception should be thrown
        // In practice, you might verify log output or storage
    }
    
    @Test
    fun `multiple runs should be independent`() = runTest {
        // Arrange & Act - First run
        val runId1 = metricsService.startRun()
        metricsService.recordFetchError()
        metricsService.completeRun(chunkSize = 1024, successRate = 0.8f)
        
        // Second run
        val runId2 = metricsService.startRun()
        metricsService.completeRun(chunkSize = 2048, successRate = 1.0f)
        
        // Assert
        assertNotEquals("Run IDs should be different", runId1, runId2)
        
        argumentCaptor<SelfOptMetrics>().apply {
            verify(mockDao, times(2)).insertMetrics(capture())
            val metrics1 = allValues[0]
            val metrics2 = allValues[1]
            
            // First run should have error and lower success rate
            assertEquals("First run should have fetch error", 1, metrics1.fetchErrors)
            assertEquals("First run success rate", 0.8f, metrics1.successRate, 0.01f)
            assertEquals("First run chunk size", 1024, metrics1.chunkSize)
            
            // Second run should be clean
            assertEquals("Second run should have no errors", 0, metrics2.fetchErrors)
            assertEquals("Second run success rate", 1.0f, metrics2.successRate, 0.01f)
            assertEquals("Second run chunk size", 2048, metrics2.chunkSize)
        }
    }
    
    @Test
    fun `memory tracking should work within reasonable bounds`() = runTest {
        // Arrange
        metricsService.startRun()
        
        // Act
        metricsService.completeRun()
        
        // Assert
        argumentCaptor<SelfOptMetrics>().apply {
            verify(mockDao).insertMetrics(capture())
            val metrics = firstValue
            
            assertTrue("Memory usage should be non-negative", metrics.memoryUsageMb >= 0)
            assertTrue("Memory usage should be reasonable (< 1GB)", metrics.memoryUsageMb < 1024)
            assertTrue("System load should be between 0 and 1", 
                      metrics.systemLoad >= 0 && metrics.systemLoad <= 1)
        }
    }
}