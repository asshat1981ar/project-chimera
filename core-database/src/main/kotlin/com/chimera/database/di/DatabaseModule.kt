package com.chimera.database.di

import android.content.Context
import com.chimera.database.ChimeraGameDatabase
import com.chimera.database.dao.CharacterDao
import com.chimera.database.dao.CharacterStateDao
import com.chimera.database.dao.DialogueTurnDao
import com.chimera.database.dao.SaveSlotDao
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

    @Provides
    fun provideSaveSlotDao(db: ChimeraGameDatabase): SaveSlotDao = db.saveSlotDao()

    @Provides
    fun provideCharacterDao(db: ChimeraGameDatabase): CharacterDao = db.characterDao()

    @Provides
    fun provideCharacterStateDao(db: ChimeraGameDatabase): CharacterStateDao = db.characterStateDao()

    @Provides
    fun provideDialogueTurnDao(db: ChimeraGameDatabase): DialogueTurnDao = db.dialogueTurnDao()
}
