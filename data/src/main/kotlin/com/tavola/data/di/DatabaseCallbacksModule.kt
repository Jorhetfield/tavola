package com.tavola.data.di

import androidx.room.RoomDatabase
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

/** Declares the (possibly empty) set of database callbacks so release builds, which contribute none, still compile. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DatabaseCallbacksModule {
    @Multibinds
    abstract fun databaseCallbacks(): Set<RoomDatabase.Callback>
}
