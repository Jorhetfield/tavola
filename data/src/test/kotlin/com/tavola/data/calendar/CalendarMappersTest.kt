package com.tavola.data.calendar

import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalendarMappersTest {
    private val date = LocalDate(2026, 10, 20)

    @Test
    fun `a recipe meal round trips with its reference data`() {
        val meal = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella", 50, listOf("Español", "Arroz")))

        val entity = meal.toEntity(date, MealType.Lunch)

        assertEquals("2026-10-20", entity.date)
        assertEquals("Lunch", entity.type)
        assertEquals(meal, entity.toDomain())
    }

    @Test
    fun `a quick meal round trips with and without a note`() {
        listOf(PlannedMeal.Quick("Sobras"), PlannedMeal.Quick("Bocadillo", note = "Para llevar")).forEach { meal ->
            assertEquals(meal, meal.toEntity(date, MealType.Dinner).toDomain())
        }
    }

    @Test
    fun `a recipe row without a recipe id is read as a quick meal instead of failing`() {
        val corrupt = PlannedMeal.FromRecipe(RecipeRef("x", "Receta rota")).toEntity(date, MealType.Lunch).copy(recipeId = null)

        assertEquals(PlannedMeal.Quick("Receta rota"), corrupt.toDomain())
    }

    @Test
    fun `malformed tags are read as empty and the meal keeps the rest of its data`() {
        val meal = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella", 50, listOf("Español")))
        val damaged = meal.toEntity(date, MealType.Lunch).copy(tagsJson = "[broken")

        assertEquals(PlannedMeal.FromRecipe(RecipeRef("paella", "Paella", 50, emptyList())), damaged.toDomain())
    }

    @Test
    fun `a task round trips keeping its done state`() {
        val task = DayTask("t1", "Comprar pan", done = true)

        assertEquals(task, task.toEntity(date, position = 3).toDomain())
        assertEquals(3, task.toEntity(date, position = 3).position)
    }
}
