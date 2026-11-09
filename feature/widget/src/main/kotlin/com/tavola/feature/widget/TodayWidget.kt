package com.tavola.feature.widget

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalTime

/** The three sizes the widget is laid out for, from the cells of the launcher: 2x2, 4x2 and 4x4. */
internal val SmallWidgetSize = DpSize(110.dp, 110.dp)
internal val MediumWidgetSize = DpSize(250.dp, 110.dp)
internal val LargeWidgetSize = DpSize(250.dp, 250.dp)

/** Changes every time the widget has to read the day again; the widget reads it as its state and reloads when it changes. */
private val RefreshKey = longPreferencesKey("refresh")

/**
 * "Today": the lunch and the dinner of the day and its tasks, which can be ticked from the home screen. It is drawn from what
 * [TodayWidgetLoader] reads, and draws small, medium and large layouts as it is resized.
 *
 * Glance keeps the session of a widget open for a while after it is drawn, and asking for an update then only composes the content
 * again, without running [provideGlance] again. So the day is not read once up front: the content reads it again whenever the
 * [RefreshKey] of its state changes, which is what [refresh] does.
 */
internal class TodayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SmallWidgetSize, MediumWidgetSize, LargeWidgetSize))

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
        // What is drawn first is read before composing, so the widget is never drawn empty.
        val firstRefresh = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, id)[RefreshKey]
        val firstState = loader.load()
        provideContent {
            val refresh = currentState<Preferences>()[RefreshKey]
            var state by remember { mutableStateOf(firstState) }
            var shownRefresh by remember { mutableStateOf(firstRefresh) }
            LaunchedEffect(refresh) {
                if (refresh != shownRefresh) {
                    state = loader.load()
                    shownRefresh = refresh
                }
            }
            TodayWidgetContent(state, hourOfDay = LocalTime.now().hour, launcher = launcher)
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
