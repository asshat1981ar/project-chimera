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
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.dao.QuestDao
import com.chimera.database.dao.RumorPacketDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.dao.VowDao
import com.chimera.database.entity.CharacterEntity
import com.chimera.database.entity.CharacterStateEntity
import com.chimera.database.entity.DialogueTurnEntity
import com.chimera.database.entity.FactionStateEntity
import com.chimera.database.entity.JournalEntryEntity
import com.chimera.database.entity.MemoryShardEntity
import com.chimera.database.entity.QuestEntity
import com.chimera.database.entity.RumorPacketEntity
import com.chimera.database.entity.SaveSlotEntity
import com.chimera.database.entity.SceneInstanceEntity
import com.chimera.database.entity.VowEntity

@Database(
    entities = [
        SaveSlotEntity::class,
        CharacterEntity::class,
        CharacterStateEntity::class,
        DialogueTurnEntity::class,
        MemoryShardEntity::class,
        SceneInstanceEntity::class,
        JournalEntryEntity::class,
        VowEntity::class,
        RumorPacketEntity::class,
        FactionStateEntity::class,
        QuestEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ChimeraGameDatabase : RoomDatabase() {

    abstract fun saveSlotDao(): SaveSlotDao
    abstract fun characterDao(): CharacterDao
    abstract fun characterStateDao(): CharacterStateDao
    abstract fun dialogueTurnDao(): DialogueTurnDao
    abstract fun memoryShardDao(): MemoryShardDao
    abstract fun sceneInstanceDao(): SceneInstanceDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun vowDao(): VowDao
    abstract fun questDao(): QuestDao
    abstract fun rumorPacketDao(): RumorPacketDao
    abstract fun factionStateDao(): FactionStateDao

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
            val now = System.currentTimeMillis()
            for (index in 0..2) {
                db.execSQL(
                    "INSERT INTO save_slots (slot_index, player_name, chapter_tag, " +
                    "playtime_seconds, last_played_at, created_at, is_empty) " +
                    "VALUES (?, '', 'prologue', 0, ?, ?, 1)",
                    arrayOf(index, now, now)
                )
            }
        }
    }
}
