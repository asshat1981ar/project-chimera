package com.xai.chimera.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.xai.chimera.database.ChimeraDatabase
import com.xai.chimera.domain.Player
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerDaoTest {

    private lateinit var database: ChimeraDatabase
    private lateinit var playerDao: PlayerDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ChimeraDatabase::class.java
        ).build()
        playerDao = database.playerDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetPlayer() = runBlocking {
        val player = Player(
            id = "player1",
            name = "Test Player"
        )
        playerDao.savePlayer(player)

        val loadedPlayer = playerDao.getPlayer("player1")
        assertNotNull(loadedPlayer)
        assertEquals("Test Player", loadedPlayer?.name)
    }

    @Test
    fun updatePlayerEmotions() = runBlocking {
        val player = Player(
            id = "player2",
            name = "Emotion Player"
        )
        playerDao.savePlayer(player)

        val emotions = mapOf("happy" to 0.8f, "sad" to 0.2f)
        playerDao.updatePlayerEmotions("player2", emotions)

        val updatedPlayer = playerDao.getPlayer("player2")
        assertNotNull(updatedPlayer)
        assertEquals(emotions, updatedPlayer?.emotions)
    }

    @Test
    fun deletePlayer() = runBlocking {
        val player = Player(
            id = "player3",
            name = "Delete Player"
        )
        playerDao.savePlayer(player)

        playerDao.deletePlayer("player3")
        val deletedPlayer = playerDao.getPlayer("player3")
        assertNull(deletedPlayer)
    }
}
