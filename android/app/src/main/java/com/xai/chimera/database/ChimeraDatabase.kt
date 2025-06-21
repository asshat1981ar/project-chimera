package com.xai.chimera.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.xai.chimera.dao.PlayerDao
import com.xai.chimera.domain.Player

/**
 * Room database for Project Chimera consciousness-aware dialogue system
 */
@Database(
    entities = [Player::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChimeraDatabase : RoomDatabase() {
    
    abstract fun playerDao(): PlayerDao
    
    companion object {
        @Volatile
        private var INSTANCE: ChimeraDatabase? = null
        
        fun getDatabase(context: Context): ChimeraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChimeraDatabase::class.java,
                    "chimera_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}