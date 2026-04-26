package com.chimera.database.di

import android.content.Context
import com.chimera.database.ChimeraGameDatabase
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.CraftingRecipeDao
import com.chimera.database.dao.DialogueTurnDao
import com.chimera.database.dao.InventoryDao
import com.chimera.database.dao.FactionStateDao
import com.chimera.database.dao.JournalEntryDao
import com.chimera.database.dao.MemoryShardDao
import com.chimera.database.dao.QuestDao
import com.chimera.database.dao.QuestObjectiveDao
import com.chimera.database.dao.RumorPacketDao
import com.chimera.database.dao.SaveSlotDao
import com.chimera.database.dao.SceneInstanceDao
import com.chimera.database.dao.VowDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChimeraGameDatabase {
        return ChimeraGameDatabase.buildDatabase(context)
    }

    @Provides fun provideSaveSlotDao(db: ChimeraGameDatabase): SaveSlotDao = db.saveSlotDao()
    @Provides fun provideCharacterDao(db: ChimeraGameDatabase): CharacterDao = db.characterDao()
    @Provides fun provideCharacterStateDao(db: ChimeraGameDatabase): CharacterStateDao = db.characterStateDao()
    @Provides fun provideDialogueTurnDao(db: ChimeraGameDatabase): DialogueTurnDao = db.dialogueTurnDao()
    @Provides fun provideMemoryShardDao(db: ChimeraGameDatabase): MemoryShardDao = db.memoryShardDao()
    @Provides fun provideSceneInstanceDao(db: ChimeraGameDatabase): SceneInstanceDao = db.sceneInstanceDao()
    @Provides fun provideJournalEntryDao(db: ChimeraGameDatabase): JournalEntryDao = db.journalEntryDao()
    @Provides fun provideVowDao(db: ChimeraGameDatabase): VowDao = db.vowDao()
    @Provides fun provideRumorPacketDao(db: ChimeraGameDatabase): RumorPacketDao = db.rumorPacketDao()
    @Provides fun provideFactionStateDao(db: ChimeraGameDatabase): FactionStateDao = db.factionStateDao()
    @Provides fun provideQuestDao(db: ChimeraGameDatabase): QuestDao = db.questDao()
    @Provides fun provideQuestObjectiveDao(db: ChimeraGameDatabase): QuestObjectiveDao = db.questObjectiveDao()
    @Provides fun provideInventoryDao(db: ChimeraGameDatabase): InventoryDao = db.inventoryDao()
    @Provides fun provideCraftingRecipeDao(db: ChimeraGameDatabase): CraftingRecipeDao = db.craftingRecipeDao()
}
