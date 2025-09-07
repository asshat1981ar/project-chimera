package com.xai.chimera.service

import com.xai.chimera.dao.SelfOptMetricsDao
import com.xai.chimera.domain.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.math.abs

/**
 * Unit tests for AnalysisService
 */
class AnalysisServiceTest {
    
    private lateinit var mockDao: SelfOptMetricsDao
    private lateinit var analysisService: AnalysisService
    
    @Before
    fun setup() {
        mockDao = mock()
        analysisService = AnalysisService(mockDao)
    }
    
    @Test
    fun `performAnalysis should handle empty metrics gracefully`() = runTest {
        // Arrange
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(emptyList())
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        assertNotNull("Analysis result should not be null", result)
        assertEquals("Total runs should be 0", 0, result.totalRuns)
        assertEquals("Performance score should be 0", 0f, result.overallPerformanceScore, 0.01f)
        assertTrue("Trends should be empty", result.trends.isEmpty())
        assertTrue("Anomalies should be empty", result.anomalies.isEmpty())
        assertTrue("Bottlenecks should be empty", result.bottlenecks.isEmpty())
        assertTrue("Should have default recommendation", result.recommendations.isNotEmpty())
    }
    
    @Test
    fun `performAnalysis should generate comprehensive insights with sufficient data`() = runTest {
        // Arrange
        val testMetrics = generateTestMetrics(20)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        assertNotNull("Analysis result should not be null", result)
        assertEquals("Total runs should match metrics count", 20, result.totalRuns)
        assertTrue("Performance score should be calculated", result.overallPerformanceScore > 0)
        assertTrue("Trends should be generated", result.trends.isNotEmpty())
        assertTrue("Analysis timestamp should be recent", 
                  System.currentTimeMillis() - result.analysisTimestamp < 5000)
        
        // Verify specific trend metrics are present
        val trendMetrics = result.trends.map { it.metricName }
        assertTrue("Should have processing time trend", 
                  trendMetrics.contains("processing_time"))
        assertTrue("Should have fetch errors trend", 
                  trendMetrics.contains("fetch_errors"))
        assertTrue("Should have memory usage trend", 
                  trendMetrics.contains("memory_usage"))
    }
    
    @Test
    fun `trend analysis should detect improving performance`() = runTest {
        // Arrange - metrics with improving processing time
        val testMetrics = generateImprovingMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        val processingTimeTrend = result.trends.find { it.metricName == "processing_time" }
        assertNotNull("Processing time trend should exist", processingTimeTrend)
        assertTrue("Trend should be improving or stable", 
                  processingTimeTrend!!.trend in listOf(TrendDirection.IMPROVING, TrendDirection.STABLE))
    }
    
    @Test
    fun `trend analysis should detect degrading performance`() = runTest {
        // Arrange - metrics with degrading processing time
        val testMetrics = generateDegradingMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        val processingTimeTrend = result.trends.find { it.metricName == "processing_time" }
        assertNotNull("Processing time trend should exist", processingTimeTrend)
        assertTrue("Trend should be degrading or volatile", 
                  processingTimeTrend!!.trend in listOf(TrendDirection.DEGRADING, TrendDirection.VOLATILE))
    }
    
    @Test
    fun `anomaly detection should identify spikes in processing time`() = runTest {
        // Arrange - metrics with a processing time spike
        val testMetrics = generateMetricsWithSpike(15)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        val processingAnomalies = result.anomalies.filter { it.metricName == "processing_time" }
        assertTrue("Should detect processing time anomalies", processingAnomalies.isNotEmpty())
        
        val spikes = processingAnomalies.filter { it.anomalyType == AnomalyType.SPIKE }
        assertTrue("Should detect spike anomalies", spikes.isNotEmpty())
    }
    
    @Test
    fun `bottleneck analysis should identify API latency issues`() = runTest {
        // Arrange - metrics with high API response times
        val testMetrics = generateHighApiLatencyMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        val apiBottlenecks = result.bottlenecks.filter { 
            it.bottleneckType == BottleneckType.API_LATENCY 
        }
        assertTrue("Should identify API latency bottleneck", apiBottlenecks.isNotEmpty())
    }
    
    @Test
    fun `bottleneck analysis should identify memory usage issues`() = runTest {
        // Arrange - metrics with high memory usage
        val testMetrics = generateHighMemoryMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        val memoryBottlenecks = result.bottlenecks.filter { 
            it.bottleneckType == BottleneckType.MEMORY_USAGE 
        }
        assertTrue("Should identify memory usage bottleneck", memoryBottlenecks.isNotEmpty())
    }
    
    @Test
    fun `recommendations should be generated for degrading trends`() = runTest {
        // Arrange
        val testMetrics = generateDegradingMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        assertTrue("Should generate recommendations", result.recommendations.isNotEmpty())
        val hasRelevantRecommendation = result.recommendations.any { 
            it.contains("degrading", ignoreCase = true) || 
            it.contains("optimize", ignoreCase = true) 
        }
        assertTrue("Should have relevant recommendation", hasRelevantRecommendation)
    }
    
    @Test
    fun `dynamic adjustments should be computed for volatile metrics`() = runTest {
        // Arrange
        val testMetrics = generateVolatileMetrics(15)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(testMetrics)
        
        // Act
        val result = analysisService.performAnalysis()
        
        // Assert
        assertTrue("Should compute dynamic adjustments", result.dynamicAdjustments.isNotEmpty())
    }
    
    @Test
    fun `performance score should reflect system health accurately`() = runTest {
        // Arrange - high-performing metrics
        val goodMetrics = generateHighPerformanceMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(goodMetrics)
        
        // Act
        val goodResult = analysisService.performAnalysis()
        
        // Assert
        assertTrue("Good metrics should have high performance score", 
                  goodResult.overallPerformanceScore > 0.7f)
        
        // Arrange - poor-performing metrics
        val poorMetrics = generatePoorPerformanceMetrics(10)
        whenever(mockDao.getMetricsInRange(any(), any())).thenReturn(poorMetrics)
        
        // Act
        val poorResult = analysisService.performAnalysis()
        
        // Assert
        assertTrue("Poor metrics should have low performance score", 
                  poorResult.overallPerformanceScore < 0.5f)
    }
    
    // Test data generation helpers
    private fun generateTestMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "metric_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 100L + (i * 5),
                fetchErrors = if (i % 5 == 0) 1 else 0,
                chunkSize = 1024,
                memoryUsageMb = 50f + (i * 2),
                successRate = if (i % 10 == 0) 0.8f else 1.0f,
                emotionProcessingTimeMs = 20L + i,
                consciousnessUpdateTimeMs = 30L + i,
                apiResponseTimeMs = 50L + (i * 2),
                playerEmotionVariance = 0.1f + (i * 0.01f),
                conversationComplexity = 0.5f + (i * 0.02f),
                systemLoad = 0.3f + (i * 0.01f)
            )
        }
    }
    
    private fun generateImprovingMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "improving_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 200L - (i * 10), // Improving (decreasing) time
                fetchErrors = 0,
                chunkSize = 1024,
                memoryUsageMb = 60f - (i * 2), // Improving (decreasing) memory
                successRate = 0.9f + (i * 0.01f), // Improving success rate
                emotionProcessingTimeMs = 30L - i,
                consciousnessUpdateTimeMs = 40L - i,
                apiResponseTimeMs = 80L - (i * 3),
                playerEmotionVariance = 0.2f - (i * 0.01f),
                conversationComplexity = 0.5f,
                systemLoad = 0.4f - (i * 0.02f)
            )
        }
    }
    
    private fun generateDegradingMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "degrading_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 100L + (i * 20), // Degrading (increasing) time
                fetchErrors = i / 3, // Increasing errors
                chunkSize = 1024,
                memoryUsageMb = 40f + (i * 5), // Degrading (increasing) memory
                successRate = 1.0f - (i * 0.05f), // Degrading success rate
                emotionProcessingTimeMs = 20L + (i * 3),
                consciousnessUpdateTimeMs = 30L + (i * 5),
                apiResponseTimeMs = 50L + (i * 10),
                playerEmotionVariance = 0.1f + (i * 0.03f),
                conversationComplexity = 0.5f,
                systemLoad = 0.3f + (i * 0.05f)
            )
        }
    }
    
    private fun generateMetricsWithSpike(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            val spikeValue = if (i == count / 2) 5000L else 100L // Spike in the middle
            SelfOptMetrics(
                id = "spike_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = spikeValue,
                fetchErrors = 0,
                chunkSize = 1024,
                memoryUsageMb = 50f,
                successRate = 1.0f,
                emotionProcessingTimeMs = 20L,
                consciousnessUpdateTimeMs = 30L,
                apiResponseTimeMs = 50L,
                playerEmotionVariance = 0.1f,
                conversationComplexity = 0.5f,
                systemLoad = 0.3f
            )
        }
    }
    
    private fun generateHighApiLatencyMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "high_api_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 1000L,
                fetchErrors = 0,
                chunkSize = 1024,
                memoryUsageMb = 50f,
                successRate = 1.0f,
                emotionProcessingTimeMs = 20L,
                consciousnessUpdateTimeMs = 30L,
                apiResponseTimeMs = 900L, // High API response time (90% of total)
                playerEmotionVariance = 0.1f,
                conversationComplexity = 0.5f,
                systemLoad = 0.3f
            )
        }
    }
    
    private fun generateHighMemoryMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "high_memory_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 200L,
                fetchErrors = 0,
                chunkSize = 1024,
                memoryUsageMb = 95f, // High memory usage
                successRate = 1.0f,
                emotionProcessingTimeMs = 20L,
                consciousnessUpdateTimeMs = 30L,
                apiResponseTimeMs = 50L,
                playerEmotionVariance = 0.1f,
                conversationComplexity = 0.5f,
                systemLoad = 0.8f
            )
        }
    }
    
    private fun generateVolatileMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            val volatileValue = if (i % 2 == 0) 1000 else 2000 // High volatility
            SelfOptMetrics(
                id = "volatile_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 200L,
                fetchErrors = 0,
                chunkSize = volatileValue, // Volatile chunk size
                memoryUsageMb = 50f,
                successRate = 1.0f,
                emotionProcessingTimeMs = 20L,
                consciousnessUpdateTimeMs = 30L,
                apiResponseTimeMs = 50L,
                playerEmotionVariance = 0.1f,
                conversationComplexity = 0.5f,
                systemLoad = 0.3f
            )
        }
    }
    
    private fun generateHighPerformanceMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "high_perf_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 50L, // Fast processing
                fetchErrors = 0, // No errors
                chunkSize = 1024,
                memoryUsageMb = 20f, // Low memory usage
                successRate = 1.0f, // Perfect success rate
                emotionProcessingTimeMs = 10L,
                consciousnessUpdateTimeMs = 15L,
                apiResponseTimeMs = 20L,
                playerEmotionVariance = 0.05f,
                conversationComplexity = 0.5f,
                systemLoad = 0.2f
            )
        }
    }
    
    private fun generatePoorPerformanceMetrics(count: Int): List<SelfOptMetrics> {
        return (0 until count).map { i ->
            SelfOptMetrics(
                id = "poor_perf_$i",
                timestamp = System.currentTimeMillis() - (count - i) * 60000L,
                runId = "run_$i",
                processingTimeMs = 5000L, // Slow processing
                fetchErrors = 3, // Many errors
                chunkSize = 1024,
                memoryUsageMb = 90f, // High memory usage
                successRate = 0.3f, // Poor success rate
                emotionProcessingTimeMs = 100L,
                consciousnessUpdateTimeMs = 200L,
                apiResponseTimeMs = 4000L,
                playerEmotionVariance = 0.8f,
                conversationComplexity = 0.9f,
                systemLoad = 0.9f
            )
        }
    }
}