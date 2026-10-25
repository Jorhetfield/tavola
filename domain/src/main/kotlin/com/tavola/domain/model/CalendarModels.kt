package com.tavola.domain.model

import kotlinx.datetime.LocalDate

enum class MealType { Lunch, Dinner }

/** Referencia ligera a una receta, lo justo para pintar un hueco del calendario. */
data class RecipeRef(val recipeId: String, val title: String, val prepTimeMinutes: Int? = null, val tags: List<String> = emptyList())

/** Lo que ocupa un hueco de comida o cena: una receta del recetario o una comida anotada a mano. */
sealed interface PlannedMeal {
    val title: String

    /** Receta del recetario. Guarda una copia del título y los datos de [recipe] para pintar el calendario sin cargar recetas. */
    data class FromRecipe(val recipe: RecipeRef) : PlannedMeal {
        override val title: String get() = recipe.title
    }

    /** Comida sin receta ("sobras", "comemos fuera", "bocadillo"), con una nota opcional. */
    data class Quick(override val title: String, val note: String? = null) : PlannedMeal
}

/** Tarea rápida de un día ("sacar a descongelar", "comprar pimientos"). */
data class DayTask(val id: String, val text: String, val done: Boolean = false)

data class DayPlan(
    val date: LocalDate,
    val lunch: PlannedMeal? = null,
    val dinner: PlannedMeal? = null,
    val tasks: List<DayTask> = emptyList(),
) {
    fun meal(type: MealType): PlannedMeal? = when (type) {
        MealType.Lunch -> lunch
        MealType.Dinner -> dinner
    }
}

/** Siete días consecutivos a partir de [weekStart], siempre completos (los días sin plan vienen vacíos). */
data class WeekPlan(val groupId: String, val weekStart: LocalDate, val days: List<DayPlan>)
