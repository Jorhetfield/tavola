package com.tavola.core.testing.fake

import app.cash.turbine.test
import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FakeCalendarRepositoryTest {
    private val weekStart = MediterraneanSampleData.weekStart

    @Test
    fun `observeWeek emite siete dias consecutivos desde el inicio de semana`() = runTest {
        FakeCalendarRepository().observeWeek(weekStart).test {
            val week = awaitItem()

            assertEquals(7, week.days.size)
            assertEquals(weekStart, week.days.first().date)
            assertEquals(LocalDate(2026, 10, 25), week.days.last().date)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `los dias sin plan vienen vacios en lugar de faltar`() = runTest {
        FakeCalendarRepository(initialDays = emptyList()).observeWeek(weekStart).test {
            val week = awaitItem()

            assertEquals(7, week.days.size)
            assertTrue(week.days.all { day -> day.lunch == null && day.dinner == null && day.tasks.isEmpty() })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setTaskDone marca la tarea y emite la semana actualizada`() = runTest {
        val repository = FakeCalendarRepository()
        val tuesday = weekStart.let { monday -> LocalDate(monday.year, monday.month, monday.day + 1) }

        repository.observeWeek(weekStart).test {
            awaitItem()

            repository.setTaskDone(tuesday, "t-pollo", done = true)

            val task = awaitItem().days[1].tasks.first { task -> task.id == "t-pollo" }
            assertTrue(task.done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTask anade una tarea pendiente con id propio`() = runTest {
        val repository = FakeCalendarRepository(initialDays = listOf(DayPlan(weekStart)))

        repository.observeWeek(weekStart).test {
            awaitItem()

            repository.addTask(weekStart, "Comprar pan")
            repository.addTask(weekStart, "Marinar el pescado")

            awaitItem()
            val tasks = awaitItem().days.first().tasks
            assertEquals(listOf("Comprar pan", "Marinar el pescado"), tasks.map { task -> task.text })
            assertTrue(tasks.none { task -> task.done })
            assertEquals(2, tasks.map { task -> task.id }.toSet().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setMeal rellena un hueco con una receta o una comida rapida y con null lo vacia`() = runTest {
        val repository = FakeCalendarRepository(initialDays = listOf(DayPlan(weekStart)))
        val risotto = PlannedMeal.FromRecipe(MediterraneanSampleData.risotto.toRef())
        val sobras = PlannedMeal.Quick("Sobras de ayer", note = "Con ensalada")

        repository.observeWeek(weekStart).test {
            awaitItem()

            repository.setMeal(weekStart, MealType.Dinner, risotto)
            assertEquals(risotto, awaitItem().days.first().dinner)

            repository.setMeal(weekStart, MealType.Lunch, sobras)
            assertEquals(sobras, awaitItem().days.first().lunch)

            repository.setMeal(weekStart, MealType.Dinner, null)
            assertNull(awaitItem().days.first().dinner)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
