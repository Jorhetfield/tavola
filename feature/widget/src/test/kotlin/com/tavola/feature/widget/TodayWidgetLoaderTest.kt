package com.tavola.feature.widget

import com.tavola.core.testing.fake.FakeAuthRepository
import com.tavola.core.testing.fake.FakeCalendarRepository
import com.tavola.core.testing.fake.FakeGroupRepository
import com.tavola.core.testing.fake.FixedTodayProvider
import com.tavola.domain.model.DayPlan
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.PlannedMeal
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TodayWidgetLoaderTest {
    private val today = LocalDate(2026, 10, 21)
    private val user = FakeAuthRepository.SampleUser
    private val todayPlan = DayPlan(
        date = today,
        lunch = PlannedMeal.Quick("Gazpacho"),
        dinner = PlannedMeal.Quick("Tortilla de patatas"),
        tasks = listOf(DayTask("t1", "Sacar el pollo", createdAt = 1), DayTask("t2", "Comprar pan", done = true, createdAt = 2)),
    )

    private fun loader(
        auth: FakeAuthRepository = FakeAuthRepository(initialUser = user),
        groups: FakeGroupRepository = FakeGroupRepository().also { repository -> repository.seedGroup("Casa", listOf(user.id)) },
        calendar: FakeCalendarRepository =
            FakeCalendarRepository(listOf(todayPlan, DayPlan(today.plusDays(1), lunch = PlannedMeal.Quick("Mañana")))),
    ) = TodayWidgetLoader(auth, groups, calendar, FixedTodayProvider(today))

    @Test
    fun `with nobody signed in the widget only invites to open the app`() = runTest {
        assertEquals(TodayWidgetState.SignedOut, loader(auth = FakeAuthRepository(initialUser = null)).load())
    }

    @Test
    fun `signed in without a group it says so, once it has waited a little for the group`() = runTest {
        val state = loader(groups = FakeGroupRepository()).load()

        assertEquals(TodayWidgetState.NoGroup, state)
        assertTrue(testScheduler.currentTime > 0)
    }

    @Test
    fun `it shows the lunch, the dinner and the tasks of today and nothing of other days`() = runTest {
        val state = loader().load() as TodayWidgetState.Content

        assertEquals(today, state.date)
        assertEquals(PlannedMeal.Quick("Gazpacho"), state.lunch)
        assertEquals(PlannedMeal.Quick("Tortilla de patatas"), state.dinner)
        assertEquals(listOf("Sacar el pollo", "Comprar pan"), state.tasks.map { task -> task.text })
    }

    @Test
    fun `a day with nothing planned is still content, with empty slots and no tasks`() = runTest {
        val state = loader(calendar = FakeCalendarRepository(initialDays = emptyList())).load() as TodayWidgetState.Content

        assertEquals(null, state.lunch)
        assertEquals(null, state.dinner)
        assertTrue(state.tasks.isEmpty())
    }

    @Test
    fun `the day is the one the today provider says, even on the last day of a week`() = runTest {
        val sunday = LocalDate(2026, 10, 25)
        val calendar = FakeCalendarRepository(listOf(DayPlan(sunday, lunch = PlannedMeal.Quick("Paella"))))
        val groups = FakeGroupRepository().also { repository -> repository.seedGroup("Casa", listOf(user.id)) }
        val provider = FixedTodayProvider(sunday)

        val state = TodayWidgetLoader(FakeAuthRepository(initialUser = user), groups, calendar, provider).load() as TodayWidgetState.Content

        assertEquals(PlannedMeal.Quick("Paella"), state.lunch)
    }

    private fun LocalDate.plusDays(days: Int): LocalDate = LocalDate.fromEpochDays(toEpochDays() + days)
}
