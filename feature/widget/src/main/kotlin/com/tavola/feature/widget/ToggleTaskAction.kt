package com.tavola.feature.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate

private const val TAG = "ToggleTaskAction"

internal val TaskDateKey = ActionParameters.Key<String>("taskDate")
internal val TaskIdKey = ActionParameters.Key<String>("taskId")
internal val TaskDoneKey = ActionParameters.Key<Boolean>("taskDone")

/**
 * Ticks or unticks a task from the widget, without opening the app. The widget shows the change at once; then the calendar is
 * written through the same repository as the app, so every phone of the group sees it, and the widget reads the day again.
 */
internal class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val date = parameters[TaskDateKey]?.let { text -> runCatching { LocalDate.parse(text) }.getOrNull() } ?: return
        val taskId = parameters[TaskIdKey] ?: return
        val done = parameters[TaskDoneKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        TodayWidget.markPending(context, taskId, done)
        try {
            entryPoint.calendarRepository().setTaskDone(date, taskId, done)
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: Exception) {
            // A failed write must not crash the app from a widget: the refresh below puts back what the calendar has.
            Log.w(TAG, "Could not write the task from the widget", failure)
        } finally {
            // Whatever happened to the write, the widget goes back to showing what the calendar has.
            TodayWidget.refresh(context)
        }
    }
}
