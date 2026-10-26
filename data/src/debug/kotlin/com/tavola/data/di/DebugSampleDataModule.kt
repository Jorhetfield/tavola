package com.tavola.data.di

import com.tavola.data.sample.DebugSampleDataLoader
import com.tavola.domain.sample.SampleDataLoader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Debug builds can load and wipe sample data from the debug menu; release builds have no [SampleDataLoader]. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DebugSampleDataModule {
    @Binds
    abstract fun bindSampleDataLoader(impl: DebugSampleDataLoader): SampleDataLoader
}
