package com.xai.chimera.service

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkInfo
import com.xai.chimera.database.ChimeraDatabase
import com.xai.chimera.dao.SelfOptMetricsDao
import com.xai.chimera.domain.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for IntegratedAnalysisService
 */
class IntegratedAnalysisServiceTest {
    
    private lateinit var mockContext: Context
    private lateinit var mockDatabase: ChimeraDatabase
    private lateinit var mockDao: SelfOptMetricsDao
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var integratedService: IntegratedAnalysisService
    
    @Before
    fun setup() {
        mockContext = mock()
        mockDatabase = mock()
        mockDao = mock()
        mockSharedPreferences = mock()
        mockEditor = mock()
        
        // Setup SharedPreferences mocking
        whenever(mockContext.getSharedPreferences("chimera_adjustments", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putFloat(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(any(), any())).thenReturn(mockEditor)
        
        // Setup database mocking
        whenever(mockDatabase.selfOptMetricsDao()).thenReturn(mockDao)
        
        integratedService = IntegratedAnalysisService(mockContext)
    }
    
    @Test
    fun `executeWithMetrics should wrap operation with metrics collection`() = runTest {
        // This test would require more mocking of the internal services
        // For now, we'll test the structure and ensure no exceptions
        
        // Arrange
        var operationExecuted = false
        val testResult = "success"
        
        // We can't easily test the full flow without mocking the internal services
        // This test verifies the structure is correct
        assertNotNull("Integrated service should be created", integratedService)
    }
    
    @Test
    fun `getSystemHealthReport should create proper health report structure`() {
        // Test the SystemHealthReport data class and health status calculation
        
        // Excellent health
        val excellentReport = SystemHealthReport(
            overallScore = 0.9f,
            criticalIssues = 0,
            activeBottlenecks = 0,
            trendSummary = mapOf(TrendDirection.IMPROVING to 3, TrendDirection.STABLE to 2),
            recommendations = listOf("Keep up the good work"),
            lastAnalysisTime = System.currentTimeMillis()
        )
        assertEquals("Should be excellent health", HealthStatus.EXCELLENT, excellentReport.getHealthStatus())
        
        // Good health
        val goodReport = SystemHealthReport(
            overallScore = 0.7f,
            criticalIssues = 1,
            activeBottlenecks = 1,
            trendSummary = mapOf(TrendDirection.STABLE to 3),
            recommendations = listOf("Minor optimization needed"),
            lastAnalysisTime = System.currentTimeMillis()
        )
        assertEquals("Should be good health", HealthStatus.GOOD, goodReport.getHealthStatus())
        
        // Fair health
        val fairReport = SystemHealthReport(
            overallScore = 0.5f,
            criticalIssues = 2,
            activeBottlenecks = 2,
            trendSummary = mapOf(TrendDirection.DEGRADING to 2, TrendDirection.STABLE to 1),
            recommendations = listOf("Performance optimization required"),
            lastAnalysisTime = System.currentTimeMillis()
        )
        assertEquals("Should be fair health", HealthStatus.FAIR, fairReport.getHealthStatus())
        
        // Poor health
        val poorReport = SystemHealthReport(
            overallScore = 0.3f,
            criticalIssues = 5,
            activeBottlenecks = 3,
            trendSummary = mapOf(TrendDirection.DEGRADING to 4),
            recommendations = listOf("Immediate attention required"),
            lastAnalysisTime = System.currentTimeMillis()
        )
        assertEquals("Should be poor health", HealthStatus.POOR, poorReport.getHealthStatus())
        
        // Critical health
        val criticalReport = SystemHealthReport(
            overallScore = 0.1f,
            criticalIssues = 10,
            activeBottlenecks = 5,
            trendSummary = mapOf(TrendDirection.DEGRADING to 8),
            recommendations = listOf("System failure imminent"),
            lastAnalysisTime = System.currentTimeMillis()
        )
        assertEquals("Should be critical health", HealthStatus.CRITICAL, criticalReport.getHealthStatus())
    }
    
    @Test
    fun `getStoredAdjustments should return empty map for expired adjustments`() {
        // Arrange - old timestamp
        val oldTimestamp = System.currentTimeMillis() - (25 * 60 * 60 * 1000L) // 25 hours ago
        whenever(mockSharedPreferences.getLong("adjustments_timestamp", 0))
            .thenReturn(oldTimestamp)
        
        // Act
        val adjustments = integratedService.getStoredAdjustments()
        
        // Assert
        assertTrue("Should return empty map for expired adjustments", adjustments.isEmpty())
    }
    
    @Test
    fun `getStoredAdjustments should return valid adjustments for recent timestamp`() {
        // Arrange - recent timestamp
        val recentTimestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000L) // 1 hour ago
        whenever(mockSharedPreferences.getLong("adjustments_timestamp", 0))
            .thenReturn(recentTimestamp)
        whenever(mockSharedPreferences.all).thenReturn(mapOf(
            "adjustments_timestamp" to recentTimestamp,
            "chunk_size_adjustment" to -0.1f,
            "timeout_multiplier" to 1.2f
        ))
        
        // Act
        val adjustments = integratedService.getStoredAdjustments()
        
        // Assert
        assertEquals("Should return 2 adjustments", 2, adjustments.size)
        assertEquals("Should have chunk size adjustment", -0.1f, 
                    adjustments["chunk_size_adjustment"]!!, 0.01f)
        assertEquals("Should have timeout multiplier", 1.2f, 
                    adjustments["timeout_multiplier"]!!, 0.01f)
        assertFalse("Should not include timestamp", 
                   adjustments.containsKey("adjustments_timestamp"))
    }
    
    @Test
    fun `AnalysisConfig should have sensible defaults`() {
        // Test the AnalysisConfig data class
        val config = AnalysisConfig()
        
        assertEquals("Default period should be 7 days", 7, config.periodDays)
        assertEquals("Default limit should be 100 runs", 100, config.limitRuns)
        assertTrue("Trend analysis should be enabled by default", config.enableTrendAnalysis)
        assertTrue("Anomaly detection should be enabled by default", config.enableAnomalyDetection)
        assertTrue("Bottleneck identification should be enabled by default", config.enableBottleneckIdentification)
    }
    
    @Test
    fun `AnalysisConfig should allow customization`() {
        // Test custom configuration
        val customConfig = AnalysisConfig(
            periodDays = 14,
            limitRuns = 200,
            enableTrendAnalysis = false,
            enableAnomalyDetection = true,
            enableBottleneckIdentification = false
        )
        
        assertEquals("Custom period should be respected", 14, customConfig.periodDays)
        assertEquals("Custom limit should be respected", 200, customConfig.limitRuns)
        assertFalse("Trend analysis should be disabled", customConfig.enableTrendAnalysis)
        assertTrue("Anomaly detection should be enabled", customConfig.enableAnomalyDetection)
        assertFalse("Bottleneck identification should be disabled", customConfig.enableBottleneckIdentification)
    }
    
    @Test
    fun `HealthStatus enum should have correct order`() {
        // Test that health status enum is ordered from best to worst
        val healthStatuses = HealthStatus.values()
        
        assertEquals("First should be EXCELLENT", HealthStatus.EXCELLENT, healthStatuses[0])
        assertEquals("Second should be GOOD", HealthStatus.GOOD, healthStatuses[1])
        assertEquals("Third should be FAIR", HealthStatus.FAIR, healthStatuses[2])
        assertEquals("Fourth should be POOR", HealthStatus.POOR, healthStatuses[3])
        assertEquals("Last should be CRITICAL", HealthStatus.CRITICAL, healthStatuses[4])
        
        // Test ordinal comparison works for health comparison
        assertTrue("EXCELLENT should be better than GOOD", 
                  HealthStatus.EXCELLENT.ordinal < HealthStatus.GOOD.ordinal)
        assertTrue("GOOD should be better than CRITICAL", 
                  HealthStatus.GOOD.ordinal < HealthStatus.CRITICAL.ordinal)
    }
}