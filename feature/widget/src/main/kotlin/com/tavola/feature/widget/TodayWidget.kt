package com.tavola.feature.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.material3.ColorProviders
import com.tavola.core.designsystem.theme.TavolaDarkColorScheme
import com.tavola.core.designsystem.theme.TavolaLightColorScheme
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalTime

/** The three sizes the widget is laid out for, from the cells of the launcher: 2x2, 4x2 and 4x4. */
internal val SmallWidgetSize = DpSize(110.dp, 110.dp)
internal val MediumWidgetSize = DpSize(250.dp, 110.dp)
internal val LargeWidgetSize = DpSize(250.dp, 250.dp)

/** The palette of the app in light and dark, no dynamic color. */
private val TavolaWidgetColors = ColorProviders(light = TavolaLightColorScheme, dark = TavolaDarkColorScheme)

/**
 * "Today": the lunch and the dinner of the day and its tasks, which can be ticked from the home screen. It is drawn from what
 * [TodayWidgetLoader] reads each time the widget is refreshed, and draws small, medium and large layouts as it is resized.
 */
internal class TodayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SmallWidgetSize, MediumWidgetSize, LargeWidgetSize))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val state = TodayWidgetLoader(
            authRepository = entryPoint.authRepository(),
            groupRepository = entryPoint.groupRepository(),
            calendarRepository = entryPoint.calendarRepository(),
            todayProvider = entryPoint.todayProvider(),
        ).load()
        val launcher = entryPoint.widgetLauncher()
        provideContent {
            GlanceTheme(colors = TavolaWidgetColors) {
                TodayWidgetContent(state, hourOfDay = LocalTime.now().hour, launcher = launcher)
            }
        }
    }
}
