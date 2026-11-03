package com.tavola.data.calendar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [CalendarDao] with the same filtering and ordering as the real queries, so [RoomCalendarRepository]
 * can be tested on the JVM. The `@Transaction` methods are inherited and run on these primitives.
 */
internal class FakeCalendarDao : CalendarDao() {
    private val meals = MutableStateFlow<List<PlannedMealEntity>>(emptyList())
    private val tasks = MutableStateFlow<List<DayTaskEntity>>(emptyList())

    override fun observeMeals(from: String, to: String): Flow<List<PlannedMealEntity>> = meals.map { stored ->
        stored.filter { meal -> meal.date in from..to }
    }

    override fun observeTasks(from: String, to: String): Flow<List<DayTaskEntity>> = tasks.map { stored ->
        stored.filter { task -> task.date in from..to }.sortedWith(compareBy({ task -> task.date }, { task -> task.position }))
    }

    override suspend fun upsertMeal(meal: PlannedMealEntity) {
        meals.update { stored -> stored.filterNot { existing -> existing.date == meal.date && existing.type == meal.type } + meal }
    }

    override suspend fun deleteMeal(date: String, type: String) {
        meals.update { stored -> stored.filterNot { meal -> meal.date == date && meal.type == type } }
    }

    override suspend fun tasksOn(date: String): List<DayTaskEntity> =
        tasks.value.filter { task -> task.date == date }.sortedBy { task -> task.position }

    override suspend fun upsertTasks(tasks: List<DayTaskEntity>) {
        this.tasks.update { stored -> stored.filterNot { existing -> tasks.any { task -> task.id == existing.id } } + tasks }
    }

    override suspend fun setTaskDone(id: String, done: Boolean) {
        tasks.update { stored -> stored.map { task -> if (task.id == id) task.copy(done = done) else task } }
    }

    override suspend fun setTaskText(id: String, text: String) {
        tasks.update { stored -> stored.map { task -> if (task.id == id) task.copy(text = text) else task } }
    }

    override suspend fun deleteTask(id: String) {
        tasks.update { stored -> stored.filterNot { task -> task.id == id } }
    }
}
