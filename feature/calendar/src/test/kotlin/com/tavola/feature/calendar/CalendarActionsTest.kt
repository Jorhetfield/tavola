package com.tavola.feature.calendar

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.core.testing.fake.FakeCalendarRepository
import com.tavola.core.testing.fake.FixedTodayProvider
import com.tavola.core.testing.rule.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CalendarActionsTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val today = LocalDate(2026, 10, 21)
    private val monday = MediterraneanSampleData.weekStart
    private val viewModel = CalendarViewModel(FakeCalendarRepository(), FixedTodayProvider(today))

    @Test
    fun `every action of the screen is wired to the ViewModel, none is left doing nothing`() {
        val defaults = CalendarActions()
        val wired = viewModel.toActions()
        val actionGetters = CalendarActions::class.java.methods.filter { method ->
            method.name.startsWith("get") && method.parameterCount == 0 && Function::class.java.isAssignableFrom(method.returnType)
        }

        assertTrue(actionGetters.isNotEmpty(), "No actions found: the check itself is broken")
        actionGetters.forEach { getter ->
            assertNotSame(getter.invoke(defaults), getter.invoke(wired), "${getter.name} is still the default action that does nothing")
        }
    }

    @Test
    fun `tapping a past day through the screen actions opens it and tapping it again closes it`() = runTest {
        val actions = viewModel.toActions()
        viewModel.uiState.test {
            val initial = awaitSuccess()
            assertFalse(initial.days.first { day -> day.date == monday }.expanded)

            actions.onDayToggle(monday)
            awaitSuccess { success -> success.days.first { day -> day.date == monday }.expanded }

            actions.onDayToggle(monday)
            awaitSuccess { success -> !success.days.first { day -> day.date == monday }.expanded }
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
