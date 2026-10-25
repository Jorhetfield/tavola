package com.tavola.data.calendar

import app.cash.turbine.test
import com.tavola.core.testing.fake.FixedTodayProvider
import com.tavola.domain.model.MealType
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
            val current = awaitItem().days.map { it.lunch }
            cancelAndIgnoreRemainingEvents()
            repository.observeWeek(thisWeek.plusWeeks(1)).test {
                assertNotEquals(current, awaitItem().days.map { it.lunch })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `las tareas de dias pasados vienen hechas y las futuras pendientes`() = runTest {
        val past = repository.observeWeekTasks(thisWeek.plusWeeks(-2))
        val future = repository.observeWeekTasks(thisWeek.plusWeeks(2))

        assertTrue(past.isNotEmpty() && past.all { it.done })
        assertTrue(future.isNotEmpty() && future.none { it.done })
    }

    @Test
    fun `setTaskDone cambia la tarea y se mantiene`() = runTest {
        val date = thisWeek.plusWeeks(1).let { start -> start }
        val task = repository.observeWeekTasks(date).first()
        val taskDate = repository.dateOf(date, task.id)

        repository.observeWeek(date).test {
            awaitItem()

            repository.setTaskDone(taskDate, task.id, done = true)

            val updated = awaitItem().days.first { it.date == taskDate }.tasks.first { it.id == task.id }
            assertTrue(updated.done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTask anade una tarea pendiente y setMeal la comida`() = runTest {
        val date = thisWeek.plusWeeks(3)
        val recipe = RecipeRef("test", "Receta de prueba")

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

    private suspend fun InMemoryCalendarRepository.observeWeekTasks(start: LocalDate) = runCatching {
        var tasks = emptyList<com.tavola.domain.model.DayTask>()
        observeWeek(start).test {
            tasks = awaitItem().days.flatMap { it.tasks }
            cancelAndIgnoreRemainingEvents()
        }
        tasks
    }.getOrThrow()

    private suspend fun InMemoryCalendarRepository.dateOf(start: LocalDate, taskId: String): LocalDate {
        var found: LocalDate? = null
        observeWeek(start).test {
            found = awaitItem().days.first { day -> day.tasks.any { it.id == taskId } }.date
            cancelAndIgnoreRemainingEvents()
        }
        return found!!
    }
}
