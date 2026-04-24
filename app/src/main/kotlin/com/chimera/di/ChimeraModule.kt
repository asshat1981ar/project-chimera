package com.chimera.di

import com.chimera.core.engine.RelationshipArchetypeEngine
import com.chimera.core.events.GameEventBus
import com.chimera.core.simulation.GameStateMachine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Core game module. Provides deterministic simulation components.
 * No AI dependencies here -- AI is wired in AiAdapterModule.
 */
@Module
@InstallIn(SingletonComponent::class)
object ChimeraModule {

    @Provides
    @Singleton
    fun provideGameEventBus(): GameEventBus = GameEventBus()

    @Provides
    @Singleton
    fun provideGameStateMachine(): GameStateMachine = GameStateMachine()

    @Provides
    @Singleton
    fun provideRelationshipArchetypeEngine(): RelationshipArchetypeEngine = RelationshipArchetypeEngine()
}
