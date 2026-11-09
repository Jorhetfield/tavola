package com.tavola.feature.widget

import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TodayWidgetStateTest {
    private val date = LocalDate(2026, 10, 21)

    @Test
    fun `the weekday is written in capitals in the language of the app`() {
        assertEquals("MIÉRCOLES", weekdayName(date, Locale.forLanguageTag("es-ES")))
        assertEquals("WEDNESDAY", weekdayName(date, Locale.forLanguageTag("en-GB")))
    }

    @Test
    fun `a recipe meal is described by its time and first tag, like in the day card`() {
        val recipe = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella", prepTimeMinutes = 50, tags = listOf("Español", "Arroz")))

        assertEquals("50 min · Español", mealDetail(recipe) { minutes -> "$minutes min" })
        assertEquals(
            "Español",
            mealDetail(PlannedMeal.FromRecipe(RecipeRef("p", "P", tags = listOf("Español")))) { minutes ->
                "$minutes min"
            },
        )
        assertEquals("30 min", mealDetail(PlannedMeal.FromRecipe(RecipeRef("p", "P", prepTimeMinutes = 30))) { minutes -> "$minutes min" })
        assertEquals(null, mealDetail(PlannedMeal.FromRecipe(RecipeRef("p", "P"))) { minutes -> "$minutes min" })
    }

    @Test
    fun `a quick meal is described by its note, and nothing is described by nothing`() {
        assertEquals("sobras de ayer", mealDetail(PlannedMeal.Quick("Sobras", note = " sobras de ayer ")) { minutes -> "$minutes min" })
        assertEquals(null, mealDetail(PlannedMeal.Quick("Sobras", note = "  ")) { minutes -> "$minutes min" })
        assertEquals(null, mealDetail(null) { minutes -> "$minutes min" })
    }

    @Test
    fun `the midnight refresh waits until five minutes after the next midnight`() {
        // From 23:55 that is 10 minutes: 5 until midnight and 5 more.
        assertEquals(10 * MINUTE_MILLIS, millisUntilAfterMidnight(LocalDateTime.of(2026, 10, 21, 23, 55)))
        assertEquals((24 * 60 + 5) * MINUTE_MILLIS, millisUntilAfterMidnight(LocalDateTime.of(2026, 10, 21, 0, 0)))
    }

    @Test
    fun `the midnight refresh crosses the end of a month`() {
        assertEquals(65 * MINUTE_MILLIS, millisUntilAfterMidnight(LocalDateTime.of(2026, 10, 31, 23, 0)))
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
    }
}
