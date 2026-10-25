package com.tavola.domain.model

import kotlinx.datetime.LocalDate

enum class MealType { Lunch, Dinner }

/** Referencia ligera a una receta, lo justo para pintar un hueco del calendario. */
data class RecipeRef(val recipeId: String, val title: String, val prepTimeMinutes: Int? = null, val tags: List<String> = emptyList())

/** Tarea rápida de un día ("sacar a descongelar", "comprar pimientos"). */
data class DayTask(val id: String, val text: String, val done: Boolean = false)

data class DayPlan(
    val date: LocalDate,
    val lunch: RecipeRef? = null,
    val dinner: RecipeRef? = null,
    val tasks: List<DayTask> = emptyList(),
) {
    fun meal(type: MealType): RecipeRef? = when (type) {
        MealType.Lunch -> lunch
        MealType.Dinner -> dinner
    }
}

/** Siete días consecutivos a partir de [weekStart], siempre completos (los días sin plan vienen vacíos). */
data class WeekPlan(val groupId: String, val weekStart: LocalDate, val days: List<DayPlan>)
