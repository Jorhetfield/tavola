package com.tavola.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tavola.data.calendar.CalendarDao
import com.tavola.data.calendar.DayTaskEntity
import com.tavola.data.calendar.PlannedMealEntity

/**
 * The app's local database: what is left of it while the calendar moves to Firestore (the recipes already live there). Schemas
 * are exported to `data/schemas` so every version change gets a migration.
 */
@Database(entities = [PlannedMealEntity::class, DayTaskEntity::class], version = 2, exportSchema = true)
internal abstract class TavolaDatabase : RoomDatabase() {
    abstract fun calendarDao(): CalendarDao
}

/** Version 2: the recipes moved to Firestore, so their table goes (what was in it was test data and is discarded). */
internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `recipes`")
    }
}
