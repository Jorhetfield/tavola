package com.tavola.data.sample

import androidx.room.withTransaction
import com.tavola.data.TavolaDatabase
import com.tavola.data.calendar.CalendarDao
import com.tavola.data.calendar.toEntity
import com.tavola.data.recipe.MediterraneanCatalog
import com.tavola.data.recipe.RecipeDao
import com.tavola.data.recipe.toEntity
import com.tavola.domain.dispatcher.IoDispatcher
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.MealType
import com.tavola.domain.sample.SampleDataLoader
import com.tavola.domain.time.TodayProvider
import com.tavola.domain.time.plusWeeks
import com.tavola.domain.time.startOfWeek
import com.tavola.domain.time.weekDays
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class DebugSampleDataLoader @Inject constructor(
    private val database: TavolaDatabase,
    private val recipeDao: RecipeDao,
    private val calendarDao: CalendarDao,
    private val todayProvider: TodayProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SampleDataLoader {
    // One transaction, so observers see a single change instead of dozens of half-loaded states.
    override suspend fun loadSampleData() = withContext(ioDispatcher) {
        database.withTransaction {
            recipeDao.upsertAll(MediterraneanCatalog.recipes.map { recipe -> recipe.toEntity() })

            val today = todayProvider.today()
            val firstDay = today.startOfWeek().plusWeeks(-WEEKS_BEFORE)
            (0 until WEEKS_BEFORE + WEEKS_AFTER + 1).flatMap { week -> firstDay.plusWeeks(week).weekDays() }
                .map { date -> SampleDayGenerator.day(date, today) }
                .forEach { day -> save(day) }
        }
    }

    override suspend fun clearAllData() = withContext(ioDispatcher) {
        database.clearAllTables()
    }

    private suspend fun save(day: DayPlan) {
        day.lunch?.let { meal -> calendarDao.upsertMeal(meal.toEntity(day.date, MealType.Lunch)) }
        day.dinner?.let { meal -> calendarDao.upsertMeal(meal.toEntity(day.date, MealType.Dinner)) }
        calendarDao.upsertTasks(day.tasks.mapIndexed { position, task -> task.toEntity(day.date, position) })
    }

    private companion object {
        const val WEEKS_BEFORE = 1
        const val WEEKS_AFTER = 2
    }
}
