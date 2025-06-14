package com.xai.chimera.dao

import com.xai.chimera.domain.Player

/**
 * Data Access Object interface for Player entity
 */
interface PlayerDao {
    suspend fun getPlayer(id: String): Player?
    suspend fun savePlayer(player: Player)
    suspend fun updatePlayerEmotions(id: String, emotions: Map<String, Float>)
    suspend fun getAllPlayers(): List<Player>
    suspend fun deletePlayer(id: String)
}
