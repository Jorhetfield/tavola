package com.tavola.feature.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
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

/**
 * "Today": the lunch and the dinner of the day and its tasks, which can be ticked from the home screen. It is drawn from what
 * [TodayWidgetLoader] reads, laid out for the exact size the widget has (one layout for any size the person gives it, not a few
 * fixed ones the launcher has to choose from) and scrolling when it does not fit.
 *
 * It is drawn at once with the first answer of the calendar, which is fast, and then read again once with the time to get a newer
 * one: a size change draws it again, and that must not wait for the server. Glance also keeps the session of a widget open for a
 * while after it is drawn, and asking for an update then only composes the content again, without running [provideGlance] again. So
 * the content reads the day again whenever the [RefreshKey] of its state changes, which is what [refresh] does.
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
            val refresh = currentState<Preferences>()[RefreshKey]
            var state by remember { mutableStateOf(firstState) }
            LaunchedEffect(refresh) { state = loader.load() }
            TodayWidgetContent(state, launcher = launcher)
        }
    }

    companion object {
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
    }
}
