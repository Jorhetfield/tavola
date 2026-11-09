package com.tavola.feature.widget

import com.tavola.domain.model.WeekPlan
import com.tavola.domain.repository.AuthRepository
import com.tavola.domain.repository.CalendarRepository
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.time.TodayProvider
import com.tavola.domain.time.startOfWeek
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    suspend fun load(settle: Boolean = true): TodayWidgetState {
        if (authRepository.currentUser.first() == null) return TodayWidgetState.SignedOut
        // The group arrives a moment after the session; waiting for it tells "still loading" from "has no group".
        withTimeoutOrNull(GROUP_WAIT_MILLIS) { groupRepository.groupId.filterNotNull().first() } ?: return TodayWidgetState.NoGroup

        val today = todayProvider.today()
        val week =
            latestWeek(calendarRepository.observeWeek(today.startOfWeek()), if (settle) SETTLE_MILLIS else 0L)
                ?: return TodayWidgetState.Unavailable
        val day = week.days.firstOrNull { plan -> plan.date == today } ?: return TodayWidgetState.Unavailable
        return TodayWidgetState.Content(date = today, lunch = day.lunch, dinner = day.dinner, tasks = day.tasks)
    }

    /**
     * The week as it is after the first answer has had a moment to be followed by a newer one. The first answer of a cold start can
     * come from a cache that has not caught up (with no tasks, say), and the server's one arrives right after it, so the last
     * answer within [SETTLE_MILLIS] wins. With no connection there is nothing newer and the cached one is used.
     */
    private suspend fun latestWeek(weeks: Flow<WeekPlan>, settleMillis: Long): WeekPlan? {
        var latest: WeekPlan? = null
        val firstArrived = CompletableDeferred<Unit>()
        coroutineScope {
            val collecting = launch {
                weeks.collect { week ->
                    latest = week
                    firstArrived.complete(Unit)
                }
            }
            withTimeoutOrNull(WEEK_WAIT_MILLIS) {
                firstArrived.await()
                if (settleMillis > 0) delay(settleMillis)
            }
            collecting.cancel()
        }
        return latest
    }

    private companion object {
        const val GROUP_WAIT_MILLIS = 3_000L
        const val WEEK_WAIT_MILLIS = 6_000L
        const val SETTLE_MILLIS = 1_500L
    }
}
