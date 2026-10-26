package com.tavola.data.di

import android.content.Context
import androidx.room.Room
import com.tavola.data.TavolaDatabase
import com.tavola.data.calendar.CalendarDao
import com.tavola.data.recipe.RecipeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "tavola.db"

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TavolaDatabase =
        Room.databaseBuilder(context, TavolaDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideRecipeDao(database: TavolaDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideCalendarDao(database: TavolaDatabase): CalendarDao = database.calendarDao()
}
