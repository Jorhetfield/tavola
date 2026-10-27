package com.tavola.feature.calendar

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.designsystem.calendar.DayState
import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.core.testing.fake.FakeCalendarRepository
import com.tavola.core.testing.fake.FixedTodayProvider
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.CalendarRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/** Past days start collapsed and can be opened one by one; today and the days to come are always shown in full. */
class CalendarViewModelPastDaysTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    // Today is Wednesday 21 October 2026; the sample week goes from the 19th to the 25th.
    private val today = LocalDate(2026, 10, 21)
    private val monday = MediterraneanSampleData.weekStart
    private val tuesday = LocalDate(2026, 10, 20)
    private val thursday = LocalDate(2026, 10, 22)
    private val calendarRepository = FakeCalendarRepository()

    private val todayProvider = FixedTodayProvider(today)

    private fun viewModel() = CalendarViewModel(calendarRepository, todayProvider)

    private fun CalendarUiState.Success.expandedDates() = days.filter { day -> day.expanded }.map { day -> day.date }

    @Test
    fun `past days start collapsed and the rest are in full`() = runTest {
        viewModel().uiState.test {
            val state = awaitSuccess()

            assertEquals(listOf(DayState.Past, DayState.Past), state.days.take(2).map { day -> day.state })
            assertEquals(listOf(false, false), state.days.take(2).map { day -> day.expanded })
            assertTrue(state.days.drop(2).all { day -> day.expanded })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a past day opens and closes with each toggle`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDayToggle(monday)
            val opened = awaitSuccess { success -> success.days.first { day -> day.date == monday }.expanded }
            assertFalse(opened.days.first { day -> day.date == tuesday }.expanded)

            viewModel.onDayToggle(monday)
            val closed = awaitSuccess { success -> !success.days.first { day -> day.date == monday }.expanded }
            assertFalse(closed.days.first { day -> day.date == monday }.expanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an open day stays open when its data changes`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onDayToggle(monday)
            awaitSuccess { success -> success.days.first { day -> day.date == monday }.expanded }

            calendarRepository.addTask(monday, "Wash the paella pan")

            val changed = awaitSuccess { success ->
                success.days.first { day -> day.date == monday }.plan.tasks.any { task ->
                    task.text ==
                        "Wash the paella pan"
                }
            }
            assertTrue(changed.days.first { day -> day.date == monday }.expanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing week closes the open days again`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onDayToggle(monday)
            awaitSuccess { success -> success.days.first { day -> day.date == monday }.expanded }

            viewModel.onNextWeekClick()
            awaitSuccess { success -> success.weekStart == LocalDate(2026, 10, 26) }
            viewModel.onTodayClick()
            val back = awaitSuccess { success -> success.weekStart == monday }

            assertFalse(back.days.first { day -> day.date == monday }.expanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `every day of a previous week is past, starts collapsed and can be opened`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onPreviousWeekClick()
            val previous = awaitSuccess { success -> success.weekStart == LocalDate(2026, 10, 12) }
            assertTrue(previous.days.all { day -> day.state == DayState.Past && !day.expanded })

            viewModel.onDayToggle(LocalDate(2026, 10, 14))
            val opened = awaitSuccess { success -> success.expandedDates() == listOf(LocalDate(2026, 10, 14)) }
            assertEquals(1, opened.expandedDates().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggling today or a day to come does nothing, not even once they are past`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDayToggle(today)
            viewModel.onDayToggle(thursday)
            // The next day arrives and the data changes, so the state is worked out again.
            todayProvider.date = thursday
            calendarRepository.addTask(monday, "Wash the paella pan")

            val nextDay = awaitSuccess { success -> success.days.first { day -> day.date == today }.state == DayState.Past }
            assertFalse(nextDay.days.first { day -> day.date == today }.expanded)
            assertEquals(DayState.Today, nextDay.days.first { day -> day.date == thursday }.state)
            assertTrue(nextDay.days.first { day -> day.date == thursday }.expanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `going to today while already on this week keeps the open days`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onDayToggle(monday)
            awaitSuccess { success -> success.days.first { day -> day.date == monday }.expanded }

            viewModel.onTodayClick()

            val current = viewModel.uiState.value as CalendarUiState.Success
            assertEquals(listOf(monday), current.expandedDates().filter { date -> date < today })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the week that slides out keeps its open days until the next week arrives`() = runTest {
        val gate = CompletableDeferred<Unit>()
        val slowRepository = object : CalendarRepository by calendarRepository {
            override fun observeWeek(weekStart: LocalDate): Flow<WeekPlan> = flow {
                if (weekStart != monday) gate.await()
                emitAll(calendarRepository.observeWeek(weekStart))
            }
        }
        val viewModel = CalendarViewModel(slowRepository, todayProvider)
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onDayToggle(monday)
            awaitSuccess { success -> success.days.first { day -> day.date == monday }.expanded }

            viewModel.onNextWeekClick()
            // The next week has not arrived: the week on screen must not collapse while it is waiting to slide out.
            val waiting = viewModel.uiState.value as CalendarUiState.Success
            assertEquals(monday, waiting.weekStart)
            assertTrue(waiting.days.first { day -> day.date == monday }.expanded)

            gate.complete(Unit)
            awaitSuccess { success -> success.weekStart == LocalDate(2026, 10, 26) }
            viewModel.onPreviousWeekClick()
            val back = awaitSuccess { success -> success.weekStart == monday }
            assertFalse(back.days.first { day -> day.date == monday }.expanded)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Skips the intermediate states until the first `Success` that satisfies [predicate]. */
    private suspend fun ReceiveTurbine<CalendarUiState>.awaitSuccess(
        predicate: (CalendarUiState.Success) -> Boolean = { true },
    ): CalendarUiState.Success {
        while (true) {
            val item = awaitItem()
            if (item is CalendarUiState.Success && predicate(item)) return item
        }
    }
}
