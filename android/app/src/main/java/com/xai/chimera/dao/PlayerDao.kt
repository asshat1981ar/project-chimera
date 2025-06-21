package com.xai.chimera.dao

import androidx.room.*
import com.xai.chimera.domain.Player

/**
 * Enhanced Data Access Object for Player entities with Room integration
 */
@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayer(id: String): Player?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayer(player: Player)
    
    @Query("UPDATE players SET emotions = :emotions WHERE id = :id")
    suspend fun updatePlayerEmotions(id: String, emotions: Map<String, Float>)
    
    @Query("SELECT * FROM players")
    suspend fun getAllPlayers(): List<Player>
    
    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deletePlayer(id: String)
    
    @Update
    suspend fun updatePlayer(player: Player)
    
    @Query("SELECT * FROM players WHERE id IN (:playerIds)")
    suspend fun getPlayers(playerIds: List<String>): List<Player>
}
