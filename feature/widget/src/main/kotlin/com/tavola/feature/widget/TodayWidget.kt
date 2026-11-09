package com.tavola.feature.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.EntryPointAccessors

/** Changes every time the widget has to read the day again; the widget reads it as its state and reloads when it changes. */
private val RefreshKey = longPreferencesKey("refresh")

/** The ticks made on the widget that the calendar has not confirmed yet (see [PendingTasks]). */
private val PendingKey = stringSetPreferencesKey("pending")

/**
 * "Today": the lunch and the dinner of the day and its tasks, which can be ticked from the home screen. It is drawn from what
 * [TodayWidgetLoader] reads, laid out for the exact size the widget has (one layout for any size the person gives it, not a few
 * fixed ones the launcher has to choose from) and scrolling when it does not fit.
 *
 * It is drawn at once with the first answer of the calendar, which is fast, and then read again once with the time to get a newer
 * one: a size change draws it again, and that must not wait for the server. Glance also keeps the session of a widget open for a
 * while after it is drawn, and asking for an update then only composes the content again, without running [provideGlance] again. So
 * the content reads the day again whenever the [RefreshKey] of its state changes, which is what [refresh] does.
 *
 * A tick on a task does not wait for any of that: [markPending] puts it in the state and the content shows it over what it has read,
 * so the widget changes as soon as it is tapped; the calendar is written and read again afterwards.
 */
internal class TodayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val loader = TodayWidgetLoader(
            authRepository = entryPoint.authRepository(),
            groupRepository = entryPoint.groupRepository(),
            calendarRepository = entryPoint.calendarRepository(),
            todayProvider = entryPoint.todayProvider(),
        )
        val launcher = entryPoint.widgetLauncher()
        val firstState = loader.load(settle = false)
        provideContent {
            val preferences = currentState<Preferences>()
            val refresh = preferences[RefreshKey]
            val pending = preferences[PendingKey].orEmpty()
            var state by remember { mutableStateOf(firstState) }
            var firstRead by remember { mutableStateOf(true) }
            LaunchedEffect(refresh) {
                // The first read takes a moment to settle (a cold cache can be behind); the ones after a tick or a refresh do not,
                // since the calendar already has what was written.
                state = loader.load(settle = firstRead)
                firstRead = false
                if (pending.isNotEmpty()) clearPending(context, id)
            }
            TodayWidgetContent(state.withPending(pending), launcher = launcher)
        }
    }

    private suspend fun clearPending(context: Context, id: GlanceId) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { preferences ->
            preferences.toMutablePreferences().apply { remove(PendingKey) }
        }
        update(context, id)
    }

    companion object {
        /** Whether there is a "Today" widget on a home screen. */
        suspend fun isOnScreen(context: Context): Boolean =
            GlanceAppWidgetManager(context).getGlanceIds(TodayWidget::class.java).isNotEmpty()

        /** Draws every "Today" widget again with what is in the calendar now. */
        suspend fun refresh(context: Context) {
            val widget = TodayWidget()
            GlanceAppWidgetManager(context).getGlanceIds(TodayWidget::class.java).forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { preferences ->
                    preferences.toMutablePreferences().apply { this[RefreshKey] = System.currentTimeMillis() }
                }
            }
            widget.updateAll(context)
        }

        /** Shows the task [taskId] as [done] in every "Today" widget at once, before the calendar has been written. */
        suspend fun markPending(context: Context, taskId: String, done: Boolean) {
            val widget = TodayWidget()
            GlanceAppWidgetManager(context).getGlanceIds(TodayWidget::class.java).forEach { id ->
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { preferences ->
                    preferences.toMutablePreferences().apply {
                        this[PendingKey] =
                            PendingTasks.with(this[PendingKey].orEmpty(), taskId, done)
                    }
                }
            }
            widget.updateAll(context)
        }
    }
}
