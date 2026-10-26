package com.tavola.feature.calendar

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.core.testing.fake.FakeCalendarRepository
import com.tavola.core.testing.fake.FixedTodayProvider
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.MealType
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CalendarViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    // Hoy es miércoles 21 de octubre de 2026; la semana de ejemplo va del 19 al 25.
    private val today = LocalDate(2026, 10, 21)
    private val monday = MediterraneanSampleData.weekStart
    private val thursday = LocalDate(2026, 10, 22)
    private val calendarRepository = FakeCalendarRepository()
    private val todayProvider = FixedTodayProvider(today)

    private fun viewModel(repository: CalendarRepository = calendarRepository) = CalendarViewModel(repository, todayProvider)

    @Test
    fun `arranca en la semana actual marcando pasado, hoy y proximos`() = runTest {
        viewModel().uiState.test {
            val state = awaitSuccess()

            assertEquals(monday, state.weekStart)
            assertTrue(state.isCurrentWeek)
            assertEquals(
                listOf(
                    DayState.Past,
                    DayState.Past,
                    DayState.Today,
                    DayState.Upcoming,
                    DayState.Upcoming,
                    DayState.Upcoming,
                    DayState.Upcoming,
                ),
                state.days.map { day -> day.state },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `la semana siguiente es toda de proximos y la anterior toda de pasados`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onNextWeekClick()
            val next = awaitSuccess { success -> success.weekStart == LocalDate(2026, 10, 26) }
            assertFalse(next.isCurrentWeek)
            assertTrue(next.days.all { day -> day.state == DayState.Upcoming })

            viewModel.onPreviousWeekClick()
            viewModel.onPreviousWeekClick()
            val previous = awaitSuccess { success -> success.weekStart == LocalDate(2026, 10, 12) }
            assertTrue(previous.days.all { day -> day.state == DayState.Past })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hoy vuelve a la semana actual desde cualquier otra`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onNextWeekClick()
            viewModel.onNextWeekClick()
            awaitSuccess { success -> success.weekStart == LocalDate(2026, 11, 2) }

            viewModel.onTodayClick()

            val state = awaitSuccess { success -> success.isCurrentWeek }
            assertEquals(monday, state.weekStart)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `marcar una tarea la guarda en el repositorio`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onTaskCheckedChange(LocalDate(2026, 10, 20), "t-pollo", checked = true)

            val state = awaitSuccess { success -> success.days[1].plan.tasks.any { task -> task.id == "t-pollo" && task.done } }
            assertTrue(state.days[1].plan.tasks.first { task -> task.id == "t-pollo" }.done)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing a task takes it out and undo puts it back in place`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            val original = awaitSuccess().days[1].plan.tasks
            val removed = original.first { task -> task.id == "t-pollo" }

            viewModel.onTaskRemove(LocalDate(2026, 10, 20), "t-pollo")
            val afterRemove = awaitSuccess { success -> success.removedTask != null && success.days[1].plan.tasks.size < original.size }
            assertEquals(RemovedTask(LocalDate(2026, 10, 20), removed, original.indexOf(removed)), afterRemove.removedTask)

            viewModel.onTaskRemoveUndo()
            val afterUndo = awaitSuccess { success -> success.removedTask == null && success.days[1].plan.tasks.size == original.size }
            assertEquals(original, afterUndo.days[1].plan.tasks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when the notice ends without undo there is nothing left to restore`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onTaskRemove(LocalDate(2026, 10, 20), "t-pollo")
            awaitSuccess { success -> success.removedTask != null }

            viewModel.onTaskRemovedShown()
            assertNull(awaitSuccess { success -> success.removedTask == null }.removedTask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pulsar un hueco abre el selector con lo que hay ahora y descartar lo cierra`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            assertNull(awaitSuccess().mealPicker)

            viewModel.onMealClick(thursday, MealType.Lunch)
            assertEquals(
                MealPickerTarget(thursday, MealType.Lunch, current = null),
                awaitSuccess { success -> success.mealPicker != null }.mealPicker,
            )

            viewModel.onMealClick(today, MealType.Dinner)
            val risotto = PlannedMeal.FromRecipe(MediterraneanSampleData.risotto.toRef())
            assertEquals(
                MealPickerTarget(today, MealType.Dinner, current = risotto),
                awaitSuccess { success -> success.mealPicker?.date == today }.mealPicker,
            )

            viewModel.onMealPickerDismiss()
            assertNull(awaitSuccess { success -> success.mealPicker == null }.mealPicker)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el selector se actualiza si la comida cambia mientras esta abierto`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onMealClick(thursday, MealType.Lunch)
            awaitSuccess { success -> success.mealPicker != null }

            calendarRepository.setMeal(thursday, MealType.Lunch, PlannedMeal.Quick("Sobras"))

            assertEquals(PlannedMeal.Quick("Sobras"), awaitSuccess { success -> success.mealPicker?.current != null }.mealPicker?.current)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el dialogo de tarea se abre, ignora texto en blanco y anade la tarea recortada`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onAddTaskClick(thursday)
            awaitSuccess { success -> success.addTaskDate == thursday }

            viewModel.onAddTaskConfirm("   ")
            expectNoEvents()
            assertEquals(thursday, (viewModel.uiState.value as CalendarUiState.Success).addTaskDate)

            viewModel.onAddTaskConfirm("  Comprar limones ")
            val state = awaitSuccess { success -> success.days[3].plan.tasks.isNotEmpty() }
            assertNull(state.addTaskDate)
            assertEquals("Comprar limones", state.days[3].plan.tasks.last().text)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancelar el dialogo lo cierra sin anadir nada`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onAddTaskClick(thursday)
            awaitSuccess { success -> success.addTaskDate != null }

            viewModel.onAddTaskDismiss()

            val state = awaitSuccess { success -> success.addTaskDate == null }
            assertTrue(state.days[3].plan.tasks.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `si el repositorio falla muestra error y reintentar lo recupera`() = runTest {
        val flaky = FlakyCalendarRepository(calendarRepository)
        val viewModel = viewModel(flaky)
        viewModel.uiState.test {
            while (awaitItem() != CalendarUiState.Error) continue

            flaky.shouldFail = false
            viewModel.onRetryClick()

            assertEquals(monday, awaitSuccess().weekStart)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retrying after an error drops the stale undo`() = runTest {
        val flaky = FlakyCalendarRepository(calendarRepository).apply { shouldFail = false }
        val viewModel = viewModel(flaky)
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onTaskRemove(LocalDate(2026, 10, 20), "t-pollo")
            awaitSuccess { success -> success.removedTask != null }

            flaky.shouldFail = true
            viewModel.onNextWeekClick()
            while (awaitItem() != CalendarUiState.Error) continue

            flaky.shouldFail = false
            viewModel.onRetryClick()

            assertNull(awaitSuccess().removedTask)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Falla al observar la semana mientras [shouldFail] sea true. */
    private class FlakyCalendarRepository(private val delegate: CalendarRepository) : CalendarRepository by delegate {
        var shouldFail = true

        override fun observeWeek(weekStart: LocalDate): Flow<WeekPlan> = flow {
            check(!shouldFail) { "sin conexion" }
            emitAll(delegate.observeWeek(weekStart))
        }
    }

    /** Salta los estados intermedios hasta el primer `Success` que cumpla [predicate]. */
    private suspend fun ReceiveTurbine<CalendarUiState>.awaitSuccess(
        predicate: (CalendarUiState.Success) -> Boolean = { true },
    ): CalendarUiState.Success {
        while (true) {
            val item = awaitItem()
            if (item is CalendarUiState.Success && predicate(item)) return item
        }
    }
}
