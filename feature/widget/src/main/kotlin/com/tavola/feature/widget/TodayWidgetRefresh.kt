package com.tavola.feature.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val PERIODIC_WORK = "today-widget-periodic"
private const val MIDNIGHT_WORK = "today-widget-midnight"
private const val NOW_WORK = "today-widget-now"
private const val PERIOD_MINUTES = 30L

/** A little after midnight, so the new day is already there when the widget is drawn. */
private const val MIDNIGHT_MARGIN_MINUTES = 5L

/**
 * When the "Today" widget is drawn again. A widget has no listener of its own, so it is refreshed by WorkManager: every half hour
 * (which also moves "the next meal" from lunch to dinner), just after midnight (when "today" changes), and when the app asks for it
 * because something may have changed in it ([requestNow]). Tasks ticked on the widget redraw it themselves.
 */
object TodayWidgetRefresh {
    /** Starts the periodic and the midnight refresh. Safe to call again: what is already scheduled is kept. */
    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TodayWidgetWorker>(PERIOD_MINUTES, TimeUnit.MINUTES).build(),
        )
        scheduleMidnight(context)
    }

    /** Stops every refresh, once there is no widget left. */
    fun cancel(context: Context) {
        cancelScheduled(context)
        WorkManager.getInstance(context).cancelUniqueWork(NOW_WORK)
    }

    /** Stops the periodic and the midnight refresh, which keep themselves going, but not a refresh that is running. */
    internal fun cancelScheduled(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PERIODIC_WORK)
        workManager.cancelUniqueWork(MIDNIGHT_WORK)
    }

    /** Draws the widget again as soon as possible: the app calls it when it goes to the background, since it may have changed something. */
    fun requestNow(context: Context) {
        WorkManager.getInstance(context)
            .enqueueUniqueWork(NOW_WORK, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<TodayWidgetWorker>().build())
    }

    internal fun scheduleMidnight(context: Context) {
        val delay = millisUntilAfterMidnight(LocalDateTime.now())
        WorkManager.getInstance(context).enqueueUniqueWork(
            MIDNIGHT_WORK,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TodayWidgetWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).build(),
        )
    }
}

/** How long until [MIDNIGHT_MARGIN_MINUTES] after the next midnight, counting from [now]. */
internal fun millisUntilAfterMidnight(now: LocalDateTime): Long {
    val target = now.toLocalDate().plusDays(1).atStartOfDay().plusMinutes(MIDNIGHT_MARGIN_MINUTES)
    return Duration.between(now, target).toMillis()
}

/**
 * Draws every "Today" widget again, and lines up the next midnight refresh. With no widget on the screen there is nothing to draw, and
 * the refreshes stop here: the app asks for one every time it goes to the background, so this is where a removed widget's are ended.
 */
internal class TodayWidgetWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        if (!TodayWidget.isOnScreen(applicationContext)) {
            TodayWidgetRefresh.cancelScheduled(applicationContext)
            return Result.success()
        }
        TodayWidget.refresh(applicationContext)
        TodayWidgetRefresh.scheduleMidnight(applicationContext)
        return Result.success()
    }
}
