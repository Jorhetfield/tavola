package com.tavola.feature.widget

import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.time.TodayProvider
import com.tavola.domain.time.startOfWeek
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Reads what the widget shows from the same repositories as the app, once. A widget has no listener of its own: it is drawn again
 * when it is refreshed (see [TodayWidgetRefresh]). It reads from the local cache when there is no connection, so waits are short.
 */
internal class TodayWidgetLoader(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val calendarRepository: CalendarRepository,
    private val todayProvider: TodayProvider,
) {
    suspend fun load(): TodayWidgetState {
        if (authRepository.currentUser.first() == null) return TodayWidgetState.SignedOut
        // The group arrives a moment after the session; waiting for it tells "still loading" from "has no group".
        withTimeoutOrNull(GROUP_WAIT_MILLIS) { groupRepository.groupId.filterNotNull().first() } ?: return TodayWidgetState.NoGroup

        val today = todayProvider.today()
        val week = withTimeoutOrNull(WEEK_WAIT_MILLIS) { calendarRepository.observeWeek(today.startOfWeek()).first() }
            ?: return TodayWidgetState.Unavailable
        val day = week.days.firstOrNull { plan -> plan.date == today } ?: return TodayWidgetState.Unavailable
        return TodayWidgetState.Content(date = today, lunch = day.lunch, dinner = day.dinner, tasks = day.tasks)
    }

    private companion object {
        const val GROUP_WAIT_MILLIS = 3_000L
        const val WEEK_WAIT_MILLIS = 6_000L
    }
}
