package com.tavola.data.calendar

import app.cash.turbine.test
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import com.tavola.domain.time.plusWeeks
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RoomCalendarRepositoryTest {
    private val repository = RoomCalendarRepository(FakeCalendarDao())
    private val monday = LocalDate(2026, 10, 19)
    private val tuesday = LocalDate(2026, 10, 20)
    private val recipe = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella valenciana", 50, listOf("Español")))

    @Test
    fun `a new calendar has seven empty days`() = runTest {
        repository.observeWeek(monday).test {
            val week = awaitItem()

            assertEquals(7, week.days.size)
            assertEquals(monday, week.days.first().date)
            assertTrue(week.days.all { day -> day.lunch == null && day.dinner == null && day.tasks.isEmpty() })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a recipe and a quick meal are stored in their slots and read back equal`() = runTest {
        val quick = PlannedMeal.Quick("Sobras", note = "Del domingo")

        repository.observeWeek(monday).test {
            awaitItem()

            repository.setMeal(tuesday, MealType.Lunch, recipe)
            awaitItem()
            repository.setMeal(tuesday, MealType.Dinner, quick)

            val day = awaitItem().days[1]
            assertEquals(recipe, day.lunch)
            assertEquals(quick, day.dinner)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setting a meal replaces the previous one and null empties the slot`() = runTest {
        repository.observeWeek(monday).test {
            awaitItem()

            repository.setMeal(tuesday, MealType.Lunch, recipe)
            awaitItem()
            repository.setMeal(tuesday, MealType.Lunch, PlannedMeal.Quick("Comemos fuera"))
            assertEquals(PlannedMeal.Quick("Comemos fuera"), awaitItem().days[1].lunch)

            repository.setMeal(tuesday, MealType.Lunch, null)
            assertNull(awaitItem().days[1].lunch)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a week only shows its own days`() = runTest {
        repository.setMeal(monday.plusWeeks(1), MealType.Dinner, recipe)
        repository.addTask(monday.plusWeeks(-1), "Otra semana")

        repository.observeWeek(monday).test {
            val week = awaitItem()

            assertTrue(week.days.all { day -> day.dinner == null && day.tasks.isEmpty() })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `added tasks are pending, ordered and have unique ids`() = runTest {
        repository.addTask(tuesday, "Comprar pan")
        repository.addTask(tuesday, "Marinar el pescado")

        repository.observeWeek(monday).test {
            val tasks = awaitItem().days[1].tasks

            assertEquals(listOf("Comprar pan", "Marinar el pescado"), tasks.map { task -> task.text })
            assertTrue(tasks.none { task -> task.done })
            assertEquals(2, tasks.map { task -> task.id }.toSet().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `editing the text of a task keeps its position and whether it is done`() = runTest {
        repository.addTask(tuesday, "Una")
        repository.addTask(tuesday, "Dos")

        repository.observeWeek(monday).test {
            val first = awaitItem().days[1].tasks.first()
            repository.setTaskDone(tuesday, first.id, done = true)
            awaitItem()

            repository.updateTaskText(tuesday, first.id, "Uno corregido")

            val tasks = awaitItem().days[1].tasks
            assertEquals(listOf("Uno corregido", "Dos"), tasks.map { task -> task.text })
            assertEquals(first.id, tasks.first().id)
            assertTrue(tasks.first().done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checking a task keeps it in place`() = runTest {
        repository.addTask(tuesday, "Una")
        repository.addTask(tuesday, "Dos")

        repository.observeWeek(monday).test {
            val first = awaitItem().days[1].tasks.first()

            repository.setTaskDone(tuesday, first.id, done = true)

            val tasks = awaitItem().days[1].tasks
            assertEquals(listOf("Una", "Dos"), tasks.map { task -> task.text })
            assertTrue(tasks.first().done)
            assertFalse(tasks.last().done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing a task and restoring it puts it back at its position`() = runTest {
        listOf("Una", "Dos", "Tres").forEach { text -> repository.addTask(tuesday, text) }

        repository.observeWeek(monday).test {
            val original = awaitItem().days[1].tasks
            val middle = original[1]

            repository.removeTask(tuesday, middle.id)
            assertEquals(original.filterNot { task -> task.id == middle.id }, awaitItem().days[1].tasks)

            repository.restoreTask(tuesday, middle, index = 1)
            assertEquals(original, awaitItem().days[1].tasks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restoring with an out of range index puts the task last and keeps its state`() = runTest {
        repository.addTask(tuesday, "Una")

        repository.observeWeek(monday).test {
            val original = awaitItem().days[1].tasks

            repository.restoreTask(tuesday, DayTask("done-task", "Hecha", done = true), index = 99)

            val tasks = awaitItem().days[1].tasks
            assertEquals(original + DayTask("done-task", "Hecha", done = true), tasks)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
