package com.tavola.data.di

import android.content.Context
import androidx.room.Room
import com.tavola.data.MIGRATION_1_2
import com.tavola.data.TavolaDatabase
import com.tavola.data.calendar.CalendarDao
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
    fun provideDatabase(@ApplicationContext context: Context, migrationPolicy: DatabaseMigrationPolicy): TavolaDatabase =
        Room.databaseBuilder(context, TavolaDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .apply { if (migrationPolicy.destructive) fallbackToDestructiveMigration(dropAllTables = true) }
            .build()

    @Provides
    fun provideCalendarDao(database: TavolaDatabase): CalendarDao = database.calendarDao()
}
