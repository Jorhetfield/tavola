package com.tavola.data.di

import com.tavola.data.calendar.InMemoryCalendarRepository
import com.tavola.data.recipe.InMemoryRecipeRepository
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
    abstract fun bindCalendarRepository(impl: InMemoryCalendarRepository): CalendarRepository

    @Binds
    abstract fun bindRecipeRepository(impl: InMemoryRecipeRepository): RecipeRepository

    @Binds
    abstract fun bindTodayProvider(impl: SystemTodayProvider): TodayProvider
}
