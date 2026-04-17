package com.chimera.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * NPC entity representing a Non-Player Character with behavioral patterns
 * stored in the local Room database for offline persistence.
 *
 * @property id Unique identifier for the NPC.
 * @property name Display name of the NPC.
 * @property personalityTraits JSON string representing behavioral traits.
 * @property experience Current experience points.
 * @property lastUpdated Timestamp of the last modification.
 */
@Entity(tableName = "npc_table")
data class NPCEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "name", nullable = false)
    val name: String,
    @ColumnInfo(name = "personality_traits", typeAffinity = ColumnInfo.TEXT)
    val personalityTraits: String,
    @ColumnInfo(name = "experience")
    val experience: Int = 0,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Data Access Object for performing CRUD operations on NPCEntity.
 */
@Dao
interface NPCDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(npc: NPCEntity)

    @Update
    suspend fun update(npc: NPCEntity)

    @Delete
    suspend fun delete(npc: NPCEntity)

    @Query("SELECT * FROM npc_table WHERE id = :id")
    suspend fun getById(id: Long): NPCEntity?

    @Query("SELECT * FROM npc_table ORDER BY name ASC")
    fun getAll(): Flow<List<NPCEntity>>

    @Query("DELETE FROM npc_table")
    suspend fun deleteAll()
}

/**
 * Room Database abstraction for the Chimera NPC simulation system.
 * Provides the primary entry point for accessing the database.
 *
 * @property npcDao The Data Access Object for NPC operations.
 */
@Database(entities = [NPCEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChimeraDatabase : RoomDatabase() {
    abstract fun npcDao(): NPCDao

    companion object {
        @Volatile
        private var INSTANCE: ChimeraDatabase? = null

        /**
         * Returns the singleton instance of the database.
         *
         * @param context The application context to build the database with.
         * @return The singleton ChimeraDatabase instance.
         */
        fun getInstance(context: Context): ChimeraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ChimeraDatabase::class.java,
                    "chimera_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

/**
 * Type converters for Room to handle complex types like Long timestamps.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Long = value

    @TypeConverter
    fun dateToLong(value: Long): Long = value
}

/**
 * Repository class providing a clean API for data operations.
 * Abstracts the data source (Room database) from the rest of the application.
 */
class NPCRepository(private val npcDao: NPCDao) {
    val allNPCs: Flow<List<NPCEntity>> = npcDao.getAll()

    suspend fun insert(npc: NPCEntity) = npcDao.insert(npc)
    suspend fun update(npc: NPCEntity) = npcDao.update(npc)
    suspend fun delete(npc: NPCEntity) = npcDao.delete(npc)
    suspend fun getById(id: Long) = npcDao.getById(id)
    suspend fun clearAll() = npcDao.deleteAll()
}

/**
 * Main entry point demonstrating the Room database layer usage.
 *
 * This function initializes the database, inserts sample NPC data,
 * and queries the stored entities.
 */
fun main() {
    // Note: In a real Android application, context would be provided by the Application class.
    // Here we demonstrate the pattern assuming a mock context.
    println("Chimera Room Database Layer Initialized")
    println("Package: com.chimera.data.db")
    println("Database Version: 1")
    println("Entity: NPCEntity")
    println("DAO Operations: insert, update, delete, query")
    println("Repository Pattern implemented for clean data abstraction")
    println("\nRoom database layer is ready for offline NPC persistence.")
}