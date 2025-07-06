package com.chimera.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.chimera.core.archetypes.SystemArchetypeEngine
import com.chimera.core.events.GameEventBus
import com.chimera.core.worldstate.WorldStateManagerService

@Module
@InstallIn(SingletonComponent::class)
object ChimeraModule {
    
    @Provides
    @Singleton
    fun provideSystemArchetypeEngine(): SystemArchetypeEngine {
        return SystemArchetypeEngine()
    }
    
    @Provides
    @Singleton
    fun provideGameEventBus(): GameEventBus {
        return GameEventBus()
    }
    
    @Provides
    @Singleton
    fun provideWorldStateManagerService(): WorldStateManagerService {
        return WorldStateManagerService()
    }
}