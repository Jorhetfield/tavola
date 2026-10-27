package com.tavola.data.sample

import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.time.plusWeeks
import com.tavola.domain.time.weekDays
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SampleDayGeneratorTest {
    private val today = LocalDate(2026, 10, 21)
    private val monday = LocalDate(2026, 10, 19)

    @Test
    fun `the same date always gives the same day`() {
        assertEquals(SampleDayGenerator.day(monday, today), SampleDayGenerator.day(monday, today))
    }

    @Test
    fun `past days have their tasks done and future days pending`() {
        val past = (-3..-1).flatMap { week ->
            monday.plusWeeks(week).weekDays()
        }.flatMap { date -> SampleDayGenerator.day(date, today).tasks }
        val future = (1..3).flatMap { week ->
            monday.plusWeeks(week).weekDays()
        }.flatMap { date -> SampleDayGenerator.day(date, today).tasks }

        assertTrue(past.isNotEmpty() && past.all { task -> task.done })
        assertTrue(future.isNotEmpty() && future.none { task -> task.done })
    }

    @Test
    fun `over several weeks there are both recipes and quick meals`() {
        val meals = (-8..8).flatMap { week -> monday.plusWeeks(week).weekDays() }
            .flatMap { date -> SampleDayGenerator.day(date, today).let { day -> listOfNotNull(day.lunch, day.dinner) } }

        assertTrue(meals.any { meal -> meal is PlannedMeal.FromRecipe })
        assertTrue(meals.any { meal -> meal is PlannedMeal.Quick })
    }
}
