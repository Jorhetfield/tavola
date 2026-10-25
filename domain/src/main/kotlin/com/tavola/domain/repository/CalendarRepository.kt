package com.tavola.domain.repository

import com.tavola.domain.model.MealType
import com.tavola.domain.model.RecipeRef
import com.tavola.domain.model.WeekPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** Calendario del grupo actual. La implementación real (Firestore) vive en `:data`. */
interface CalendarRepository {
    /** Emite la semana que empieza en [weekStart] cada vez que cambia. */
    fun observeWeek(weekStart: LocalDate): Flow<WeekPlan>

    suspend fun setTaskDone(date: LocalDate, taskId: String, done: Boolean)

    /** Añade una tarea rápida (pendiente) al día [date]. */
    suspend fun addTask(date: LocalDate, text: String)

    /** Pone [recipe] en la comida o cena de [date]; con `null` deja el hueco vacío. */
    suspend fun setMeal(date: LocalDate, type: MealType, recipe: RecipeRef?)
}
