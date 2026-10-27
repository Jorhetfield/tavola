package com.tavola.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object MigrationPolicyModule {
    @Provides
    fun provideMigrationPolicy(): DatabaseMigrationPolicy = DatabaseMigrationPolicy(destructive = true)
}
