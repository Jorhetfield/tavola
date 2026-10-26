package com.tavola.data.di

import com.tavola.data.calendar.RoomCalendarRepository
import com.tavola.data.recipe.RoomRecipeRepository
import com.tavola.data.time.SystemTodayProvider
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.repository.RecipeRepository
import com.tavola.domain.time.TodayProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: RoomCalendarRepository): CalendarRepository

    @Binds
    abstract fun bindRecipeRepository(impl: RoomRecipeRepository): RecipeRepository

    @Binds
    abstract fun bindTodayProvider(impl: SystemTodayProvider): TodayProvider
}
