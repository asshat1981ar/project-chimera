package com.xai.chimera.dao

import androidx.room.*
import com.xai.chimera.domain.SelfOptMetrics

/**
 * Data Access Object for SelfOptMetrics entities
 */
@Dao
interface SelfOptMetricsDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetrics(metrics: SelfOptMetrics)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetricsBatch(metrics: List<SelfOptMetrics>)
    
    @Query("SELECT * FROM self_opt_metrics WHERE timestamp >= :fromTimestamp ORDER BY timestamp DESC")
    suspend fun getMetricsSince(fromTimestamp: Long): List<SelfOptMetrics>
    
    @Query("SELECT * FROM self_opt_metrics WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp ORDER BY timestamp DESC")
    suspend fun getMetricsInRange(fromTimestamp: Long, toTimestamp: Long): List<SelfOptMetrics>
    
    @Query("SELECT * FROM self_opt_metrics ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMetrics(limit: Int): List<SelfOptMetrics>
    
    @Query("SELECT * FROM self_opt_metrics WHERE runId = :runId")
    suspend fun getMetricsByRunId(runId: String): List<SelfOptMetrics>
    
    @Query("SELECT AVG(processingTimeMs) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getAverageProcessingTime(fromTimestamp: Long): Float?
    
    @Query("SELECT AVG(fetchErrors) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getAverageFetchErrors(fromTimestamp: Long): Float?
    
    @Query("SELECT AVG(chunkSize) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getAverageChunkSize(fromTimestamp: Long): Float?
    
    @Query("SELECT AVG(memoryUsageMb) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getAverageMemoryUsage(fromTimestamp: Long): Float?
    
    @Query("SELECT AVG(successRate) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getAverageSuccessRate(fromTimestamp: Long): Float?
    
    @Query("SELECT MAX(processingTimeMs) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getMaxProcessingTime(fromTimestamp: Long): Long?
    
    @Query("SELECT MIN(processingTimeMs) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getMinProcessingTime(fromTimestamp: Long): Long?
    
    @Query("SELECT COUNT(*) FROM self_opt_metrics WHERE timestamp >= :fromTimestamp")
    suspend fun getMetricsCount(fromTimestamp: Long): Int
    
    @Query("DELETE FROM self_opt_metrics WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldMetrics(beforeTimestamp: Long): Int
    
    @Query("DELETE FROM self_opt_metrics WHERE id = :id")
    suspend fun deleteMetrics(id: String)
    
    @Query("SELECT * FROM self_opt_metrics WHERE timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    suspend fun getMetricsForTrendAnalysis(fromTimestamp: Long): List<SelfOptMetrics>
}