package com.tavola.data.calendar

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Dates are ISO strings (`2026-10-26`), so `BETWEEN` on them is a chronological range. */
@Dao
internal abstract class CalendarDao {
    @Query("SELECT * FROM planned_meals WHERE date BETWEEN :from AND :to")
    abstract fun observeMeals(from: String, to: String): Flow<List<PlannedMealEntity>>

    @Query("SELECT * FROM day_tasks WHERE date BETWEEN :from AND :to ORDER BY date, position")
    abstract fun observeTasks(from: String, to: String): Flow<List<DayTaskEntity>>

    @Upsert
    abstract suspend fun upsertMeal(meal: PlannedMealEntity)

    @Query("DELETE FROM planned_meals WHERE date = :date AND type = :type")
    abstract suspend fun deleteMeal(date: String, type: String)

    @Query("SELECT * FROM day_tasks WHERE date = :date ORDER BY position")
    abstract suspend fun tasksOn(date: String): List<DayTaskEntity>

    @Upsert
    abstract suspend fun upsertTasks(tasks: List<DayTaskEntity>)

    @Query("UPDATE day_tasks SET done = :done WHERE id = :id")
    abstract suspend fun setTaskDone(id: String, done: Boolean)

    @Query("UPDATE day_tasks SET text = :text WHERE id = :id")
    abstract suspend fun setTaskText(id: String, text: String)

    @Query("DELETE FROM day_tasks WHERE id = :id")
    abstract suspend fun deleteTask(id: String)

    /** Appends a pending task after the last one of [date]. */
    @Transaction
    open suspend fun appendTask(id: String, date: String, text: String) {
        val nextPosition = (tasksOn(date).maxOfOrNull { task -> task.position } ?: -1) + 1
        upsertTasks(listOf(DayTaskEntity(id = id, date = date, position = nextPosition, text = text, done = false)))
    }

    /** Puts [task] back at [index] of its day and renumbers the day so the order is exact. */
    @Transaction
    open suspend fun insertTaskAt(task: DayTaskEntity, index: Int) {
        val ordered = tasksOn(task.date).filterNot { existing -> existing.id == task.id }.toMutableList()
        ordered.add(index.coerceIn(0, ordered.size), task)
        upsertTasks(ordered.mapIndexed { position, dayTask -> dayTask.copy(position = position) })
    }
}
