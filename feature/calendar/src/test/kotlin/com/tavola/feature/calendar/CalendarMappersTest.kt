package com.tavola.feature.calendar

import com.tavola.core.designsystem.calendar.DayState
import com.tavola.core.designsystem.calendar.MealType
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CalendarMappersTest {
    private val day = DayItem(
        plan = DayPlan(
            date = LocalDate(2026, 10, 19),
            lunch = PlannedMeal.FromRecipe(RecipeRef("gazpacho-id", "Gazpacho")),
            dinner = PlannedMeal.Quick("Comemos fuera"),
        ),
        state = DayState.Past,
    )

    @Test
    fun `a slot with a recipe gives the id of that recipe`() {
        assertEquals("gazpacho-id", day.recipeIdOf(MealType.Lunch))
    }

    @Test
    fun `a quick meal has no recipe to open`() {
        assertNull(day.recipeIdOf(MealType.Dinner))
    }

    @Test
    fun `an empty slot has no recipe to open`() {
        val empty = DayItem(DayPlan(LocalDate(2026, 10, 20)), DayState.Past)

        assertNull(empty.recipeIdOf(MealType.Lunch))
        assertNull(empty.recipeIdOf(MealType.Dinner))
    }
}
