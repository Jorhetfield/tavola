package com.tavola.data.calendar

import app.cash.turbine.test
import com.tavola.core.testing.fake.FixedTodayProvider
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import com.tavola.domain.time.plusWeeks
import com.tavola.domain.time.startOfWeek
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InMemoryCalendarRepositoryTest {
    private val today = LocalDate(2026, 10, 21)
    private val thisWeek = today.startOfWeek()
    private val repository = InMemoryCalendarRepository(FixedTodayProvider(today))

    @Test
    fun `cualquier semana trae siete dias consecutivos`() = runTest {
        listOf(-30, -1, 0, 1, 52).forEach { offset ->
            val start = thisWeek.plusWeeks(offset)
            repository.observeWeek(start).test {
                val week = awaitItem()

                assertEquals(7, week.days.size)
                assertEquals(start, week.days.first().date)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `la misma semana siempre genera los mismos datos`() = runTest {
        repository.observeWeek(thisWeek).test {
            val first = awaitItem()
            cancelAndIgnoreRemainingEvents()
            repository.observeWeek(thisWeek).test {
                assertEquals(first, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `semanas distintas generan comidas distintas`() = runTest {
        repository.observeWeek(thisWeek).test {
            val current = awaitItem().days.map { day -> day.lunch }
            cancelAndIgnoreRemainingEvents()
            repository.observeWeek(thisWeek.plusWeeks(1)).test {
                assertNotEquals(current, awaitItem().days.map { day -> day.lunch })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `las tareas de dias pasados vienen hechas y las futuras pendientes`() = runTest {
        val past = repository.observeWeekTasks(thisWeek.plusWeeks(-2))
        val future = repository.observeWeekTasks(thisWeek.plusWeeks(2))

        assertTrue(past.isNotEmpty() && past.all { task -> task.done })
        assertTrue(future.isNotEmpty() && future.none { task -> task.done })
    }

    @Test
    fun `setTaskDone cambia la tarea y se mantiene`() = runTest {
        val date = thisWeek.plusWeeks(1).let { start -> start }
        val task = repository.observeWeekTasks(date).first()
        val taskDate = repository.dateOf(date, task.id)

        repository.observeWeek(date).test {
            awaitItem()

            repository.setTaskDone(taskDate, task.id, done = true)

            val updated = awaitItem().days.first { day -> day.date == taskDate }.tasks.first { candidate -> candidate.id == task.id }
            assertTrue(updated.done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeTask removes the task and restoreTask puts it back in place`() = runTest {
        val date = thisWeek.plusWeeks(4)
        repository.addTask(date, "Comprar limones")

        repository.observeWeek(date).test {
            val original = awaitItem().days.first().tasks
            val removed = original.first()

            repository.removeTask(date, removed.id)
            assertEquals(original.drop(1), awaitItem().days.first().tasks)

            repository.restoreTask(date, removed, index = 0)
            assertEquals(original, awaitItem().days.first().tasks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTask anade una tarea pendiente y setMeal la comida`() = runTest {
        val date = thisWeek.plusWeeks(3)
        val recipe = PlannedMeal.FromRecipe(RecipeRef("test", "Receta de prueba"))

        repository.observeWeek(date).test {
            awaitItem()

            repository.addTask(date, "Comprar limones")
            repository.setMeal(date, MealType.Dinner, recipe)

            awaitItem()
            val day = awaitItem().days.first()
            assertEquals("Comprar limones", day.tasks.last().text)
            assertTrue(!day.tasks.last().done)
            assertEquals(recipe, day.dinner)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a lo largo de varias semanas salen recetas y comidas rapidas`() = runTest {
        val meals = (-8..8).flatMap { offset ->
            var week = emptyList<PlannedMeal>()
            repository.observeWeek(thisWeek.plusWeeks(offset)).test {
                week = awaitItem().days.flatMap { day -> listOfNotNull(day.lunch, day.dinner) }
                cancelAndIgnoreRemainingEvents()
            }
            week
        }

        assertTrue(meals.any { meal -> meal is PlannedMeal.FromRecipe })
        assertTrue(meals.any { meal -> meal is PlannedMeal.Quick })
    }

    private suspend fun InMemoryCalendarRepository.observeWeekTasks(start: LocalDate) = runCatching {
        var tasks = emptyList<DayTask>()
        observeWeek(start).test {
            tasks = awaitItem().days.flatMap { day -> day.tasks }
            cancelAndIgnoreRemainingEvents()
        }
        tasks
    }.getOrThrow()

    private suspend fun InMemoryCalendarRepository.dateOf(start: LocalDate, taskId: String): LocalDate {
        var found: LocalDate? = null
        observeWeek(start).test {
            found = awaitItem().days.first { day -> day.tasks.any { task -> task.id == taskId } }.date
            cancelAndIgnoreRemainingEvents()
        }
        return found!!
    }
}
