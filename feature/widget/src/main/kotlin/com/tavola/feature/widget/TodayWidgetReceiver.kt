package com.tavola.feature.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** The entry the system uses to create and update the "Today" widget (declared in the manifest of this module). */
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()

    /** The first widget was added: from now on it is refreshed by itself during the day and at midnight. */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        TodayWidgetRefresh.schedule(context)
    }

    /** The last widget was removed: nothing is refreshed any more. */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        TodayWidgetRefresh.cancel(context)
    }
}
