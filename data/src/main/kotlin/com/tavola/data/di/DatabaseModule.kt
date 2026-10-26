package com.tavola.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tavola.data.TavolaDatabase
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
    /** [callbacks] is empty in release builds; debug builds contribute the sample-data seeding callback. */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, callbacks: Set<@JvmSuppressWildcards RoomDatabase.Callback>): TavolaDatabase =
        Room.databaseBuilder(context, TavolaDatabase::class.java, DATABASE_NAME)
            .apply { callbacks.forEach { callback -> addCallback(callback) } }
            .build()

    @Provides
    fun provideRecipeDao(database: TavolaDatabase): RecipeDao = database.recipeDao()
}
