package com.tavola.feature.widget

import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TodayWidgetStateTest {
    private val lunch = PlannedMeal.Quick("Gazpacho")
    private val dinner = PlannedMeal.Quick("Tortilla")
    private val date = LocalDate(2026, 10, 21)

    private fun content(lunch: PlannedMeal? = this.lunch, dinner: PlannedMeal? = this.dinner) =
        TodayWidgetState.Content(date, lunch, dinner, tasks = emptyList())

    @Test
    fun `in the morning the next meal is lunch and from the afternoon it is dinner`() {
        assertEquals(MealType.Lunch to lunch, content().nextMeal(hourOfDay = 9))
        assertEquals(MealType.Lunch to lunch, content().nextMeal(hourOfDay = TodayWidgetState.DINNER_HOUR - 1))
        assertEquals(MealType.Dinner to dinner, content().nextMeal(hourOfDay = TodayWidgetState.DINNER_HOUR))
        assertEquals(MealType.Dinner to dinner, content().nextMeal(hourOfDay = 21))
    }

    @Test
    fun `when the meal that comes next is not planned the other one is shown`() {
        assertEquals(MealType.Dinner to dinner, content(lunch = null).nextMeal(hourOfDay = 9))
        assertEquals(MealType.Lunch to lunch, content(dinner = null).nextMeal(hourOfDay = 21))
    }

    @Test
    fun `with nothing planned there is no next meal`() {
        assertEquals(null, content(lunch = null, dinner = null).nextMeal(hourOfDay = 12))
    }

    @Test
    fun `the date is written in the language of the app`() {
        assertEquals("mié 21 oct", formatWidgetDate(date, Locale.forLanguageTag("es-ES")).replace(".", ""))
        assertEquals("Wed 21 Oct", formatWidgetDate(date, Locale.forLanguageTag("en-GB")))
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
