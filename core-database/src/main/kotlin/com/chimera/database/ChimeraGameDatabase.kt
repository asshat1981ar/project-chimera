package com.chimera.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chimera.database.converter.Converters
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.DialogueTurnDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.DialogueTurnEntity
import com.chimera.database.entity.SaveSlotEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SaveSlotEntity::class,
        CharacterEntity::class,
        CharacterStateEntity::class,
        DialogueTurnEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ChimeraGameDatabase : RoomDatabase() {

    abstract fun saveSlotDao(): SaveSlotDao
    abstract fun characterDao(): CharacterDao
    abstract fun characterStateDao(): CharacterStateDao
    abstract fun dialogueTurnDao(): DialogueTurnDao

    companion object {
        const val DATABASE_NAME = "chimera_game.db"

        fun buildDatabase(context: Context): ChimeraGameDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ChimeraGameDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(PrepopulateCallback())
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Insert 3 empty save slots on first database creation
            for (index in 0..2) {
                db.execSQL(
                    "INSERT INTO save_slots (slot_index, player_name, chapter_tag, " +
                    "playtime_seconds, last_played_at, created_at, is_empty) " +
                    "VALUES ($index, '', 'prologue', 0, ${System.currentTimeMillis()}, " +
                    "${System.currentTimeMillis()}, 1)"
                )
            }
        }
    }
}
