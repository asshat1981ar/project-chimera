package com.xai.chimera.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xai.chimera.database.ChimeraDatabase
import com.xai.chimera.domain.SelfOptMetrics
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for SelfOptMetricsDao
 */
@RunWith(AndroidJUnit4::class)
class SelfOptMetricsDaoTest {
    
    private lateinit var database: ChimeraDatabase
    private lateinit var dao: SelfOptMetricsDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChimeraDatabase::class.java
        ).allowMainThreadQueries().build()
        
        dao = database.selfOptMetricsDao()
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun insertAndGetMetrics() = runTest {
        // Arrange
        val metrics = createTestMetrics("test1", System.currentTimeMillis())
        
        // Act
        dao.insertMetrics(metrics)
        val retrieved = dao.getMetricsByRunId("run_test1")
        
        // Assert
        assertEquals("Should retrieve inserted metrics", 1, retrieved.size)
        assertEquals("ID should match", metrics.id, retrieved[0].id)
        assertEquals("Run ID should match", metrics.runId, retrieved[0].runId)
        assertEquals("Processing time should match", metrics.processingTimeMs, retrieved[0].processingTimeMs)
    }
    
    @Test
    fun insertMetricsBatch() = runTest {
        // Arrange
        val metrics = listOf(
            createTestMetrics("test1", System.currentTimeMillis()),
            createTestMetrics("test2", System.currentTimeMillis() - 60000),
            createTestMetrics("test3", System.currentTimeMillis() - 120000)
        )
        
        // Act
        dao.insertMetricsBatch(metrics)
        val retrieved = dao.getRecentMetrics(5)
        
        // Assert
        assertEquals("Should retrieve all metrics", 3, retrieved.size)
        // Should be ordered by timestamp DESC
        assertTrue("Should be ordered by timestamp", 
                  retrieved[0].timestamp >= retrieved[1].timestamp)
    }
    
    @Test
    fun getMetricsSince() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val oldMetrics = createTestMetrics("old", baseTime - 3600000) // 1 hour ago
        val recentMetrics1 = createTestMetrics("recent1", baseTime - 1800000) // 30 min ago
        val recentMetrics2 = createTestMetrics("recent2", baseTime - 900000) // 15 min ago
        
        dao.insertMetrics(oldMetrics)
        dao.insertMetrics(recentMetrics1)
        dao.insertMetrics(recentMetrics2)
        
        // Act
        val sinceTwoHours = dao.getMetricsSince(baseTime - 2400000) // 40 min ago
        
        // Assert
        assertEquals("Should get recent metrics only", 2, sinceTwoHours.size)
        assertTrue("Should include recent1", 
                  sinceTwoHours.any { it.id == "recent1" })
        assertTrue("Should include recent2", 
                  sinceTwoHours.any { it.id == "recent2" })
        assertFalse("Should not include old", 
                   sinceTwoHours.any { it.id == "old" })
    }
    
    @Test
    fun getMetricsInRange() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val beforeRange = createTestMetrics("before", baseTime - 3600000)
        val inRange1 = createTestMetrics("in1", baseTime - 1800000)
        val inRange2 = createTestMetrics("in2", baseTime - 900000)
        val afterRange = createTestMetrics("after", baseTime - 300000)
        
        dao.insertMetrics(beforeRange)
        dao.insertMetrics(inRange1)
        dao.insertMetrics(inRange2)
        dao.insertMetrics(afterRange)
        
        // Act
        val rangeResults = dao.getMetricsInRange(
            baseTime - 2400000, // 40 min ago
            baseTime - 600000   // 10 min ago
        )
        
        // Assert
        assertEquals("Should get metrics in range only", 2, rangeResults.size)
        assertTrue("Should include in1", 
                  rangeResults.any { it.id == "in1" })
        assertTrue("Should include in2", 
                  rangeResults.any { it.id == "in2" })
    }
    
    @Test
    fun getRecentMetrics() = runTest {
        // Arrange
        val metrics = (1..10).map { i ->
            createTestMetrics("test$i", System.currentTimeMillis() - (i * 60000))
        }
        dao.insertMetricsBatch(metrics)
        
        // Act
        val recent5 = dao.getRecentMetrics(5)
        
        // Assert
        assertEquals("Should limit to 5 results", 5, recent5.size)
        // Should be ordered by timestamp DESC (most recent first)
        assertEquals("First should be most recent", "test1", recent5[0].id)
        assertEquals("Last should be 5th most recent", "test5", recent5[4].id)
    }
    
    @Test
    fun getAverageProcessingTime() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val metrics = listOf(
            createTestMetrics("test1", baseTime, processingTime = 100L),
            createTestMetrics("test2", baseTime - 60000, processingTime = 200L),
            createTestMetrics("test3", baseTime - 120000, processingTime = 300L)
        )
        dao.insertMetricsBatch(metrics)
        
        // Act
        val average = dao.getAverageProcessingTime(baseTime - 180000)
        
        // Assert
        assertNotNull("Average should not be null", average)
        assertEquals("Average should be 200", 200f, average!!, 0.1f)
    }
    
    @Test
    fun getAverageFetchErrors() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val metrics = listOf(
            createTestMetrics("test1", baseTime, fetchErrors = 0),
            createTestMetrics("test2", baseTime - 60000, fetchErrors = 2),
            createTestMetrics("test3", baseTime - 120000, fetchErrors = 1)
        )
        dao.insertMetricsBatch(metrics)
        
        // Act
        val average = dao.getAverageFetchErrors(baseTime - 180000)
        
        // Assert
        assertNotNull("Average should not be null", average)
        assertEquals("Average should be 1", 1f, average!!, 0.1f)
    }
    
    @Test
    fun getMaxMinProcessingTime() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val metrics = listOf(
            createTestMetrics("test1", baseTime, processingTime = 150L),
            createTestMetrics("test2", baseTime - 60000, processingTime = 500L),
            createTestMetrics("test3", baseTime - 120000, processingTime = 75L)
        )
        dao.insertMetricsBatch(metrics)
        
        // Act
        val max = dao.getMaxProcessingTime(baseTime - 180000)
        val min = dao.getMinProcessingTime(baseTime - 180000)
        
        // Assert
        assertNotNull("Max should not be null", max)
        assertNotNull("Min should not be null", min)
        assertEquals("Max should be 500", 500L, max!!)
        assertEquals("Min should be 75", 75L, min!!)
    }
    
    @Test
    fun getMetricsCount() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val metrics = (1..7).map { i ->
            createTestMetrics("test$i", baseTime - (i * 60000))
        }
        dao.insertMetricsBatch(metrics)
        
        // Act
        val totalCount = dao.getMetricsCount(baseTime - 600000) // Last 10 minutes
        val recentCount = dao.getMetricsCount(baseTime - 240000) // Last 4 minutes
        
        // Assert
        assertEquals("Total count should be 7", 7, totalCount)
        assertEquals("Recent count should be 4", 4, recentCount)
    }
    
    @Test
    fun deleteOldMetrics() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val oldMetrics = listOf(
            createTestMetrics("old1", baseTime - 3600000),
            createTestMetrics("old2", baseTime - 7200000)
        )
        val recentMetrics = listOf(
            createTestMetrics("recent1", baseTime - 1800000),
            createTestMetrics("recent2", baseTime - 900000)
        )
        
        dao.insertMetricsBatch(oldMetrics + recentMetrics)
        
        // Act
        val deletedCount = dao.deleteOldMetrics(baseTime - 3000000) // 50 min ago
        val remaining = dao.getRecentMetrics(10)
        
        // Assert
        assertEquals("Should delete 2 old metrics", 2, deletedCount)
        assertEquals("Should have 2 remaining metrics", 2, remaining.size)
        assertTrue("Should only have recent metrics", 
                  remaining.all { it.id.startsWith("recent") })
    }
    
    @Test
    fun deleteSpecificMetrics() = runTest {
        // Arrange
        val metrics = createTestMetrics("to_delete", System.currentTimeMillis())
        dao.insertMetrics(metrics)
        
        // Verify insertion
        val beforeDelete = dao.getRecentMetrics(5)
        assertEquals("Should have 1 metric before delete", 1, beforeDelete.size)
        
        // Act
        dao.deleteMetrics("to_delete")
        val afterDelete = dao.getRecentMetrics(5)
        
        // Assert
        assertEquals("Should have 0 metrics after delete", 0, afterDelete.size)
    }
    
    @Test
    fun getMetricsForTrendAnalysis() = runTest {
        // Arrange
        val baseTime = System.currentTimeMillis()
        val metrics = (1..5).map { i ->
            createTestMetrics("trend$i", baseTime - (i * 60000))
        }
        dao.insertMetricsBatch(metrics)
        
        // Act
        val trendMetrics = dao.getMetricsForTrendAnalysis(baseTime - 360000) // Last 6 minutes
        
        // Assert
        assertEquals("Should get all metrics for trend analysis", 5, trendMetrics.size)
        // Should be ordered by timestamp ASC for trend analysis
        assertTrue("Should be ordered ascending for trends", 
                  trendMetrics[0].timestamp <= trendMetrics[1].timestamp)
    }
    
    // Helper function to create test metrics
    private fun createTestMetrics(
        id: String, 
        timestamp: Long, 
        processingTime: Long = 100L,
        fetchErrors: Int = 0
    ): SelfOptMetrics {
        return SelfOptMetrics(
            id = id,
            timestamp = timestamp,
            runId = "run_$id",
            processingTimeMs = processingTime,
            fetchErrors = fetchErrors,
            chunkSize = 1024,
            memoryUsageMb = 50f,
            successRate = 1.0f,
            emotionProcessingTimeMs = 20L,
            consciousnessUpdateTimeMs = 30L,
            apiResponseTimeMs = 50L,
            playerEmotionVariance = 0.1f,
            conversationComplexity = 0.5f,
            systemLoad = 0.3f,
            additionalMetrics = mapOf("test_metric" to 42.0f)
        )
    }
}