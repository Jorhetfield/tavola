package com.tavola.debug

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.tavola.feature.widget.WidgetPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Debug only: lists the "Today" widget at its sizes with example data, so how it looks can be checked without an account or a home
 * screen. Open it with `adb shell am start -n com.tavola/.debug.WidgetPreviewActivity`; each case is drawn in the dark and the light
 * theme, whatever the theme of the phone is.
 */
class WidgetPreviewActivity : ComponentActivity() {
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        setContentView(ScrollView(this).apply { addView(column) })

        scope.launch {
            val cases = listOf(
                Triple(WidgetPreview.Size.Small, WidgetPreview.Scenario.Full, "2x2 · full · 12h"),
                Triple(WidgetPreview.Size.Medium, WidgetPreview.Scenario.Full, "4x2 · full"),
                Triple(WidgetPreview.Size.Large, WidgetPreview.Scenario.Full, "4x4 · full"),
                Triple(WidgetPreview.Size.Large, WidgetPreview.Scenario.Empty, "4x4 · empty day"),
                Triple(WidgetPreview.Size.Small, WidgetPreview.Scenario.SignedOut, "2x2 · signed out"),
            )
            cases.forEach { (size, scenario, label) ->
                listOf(true to "dark", false to "light").forEach { (night, theme) ->
                    column.addView(TextView(this@WidgetPreviewActivity).apply { text = "$label · $theme" })
                    val themed = themedContext(night)
                    val frame = FrameLayout(themed)
                    val remoteViews = WidgetPreview.remoteViews(themed, size, scenario)
                    frame.addView(remoteViews.apply(themed, frame))
                    column.addView(frame, LinearLayout.LayoutParams(dp(size.dp.width.value.toInt()), dp(size.dp.height.value.toInt())))
                    column.addView(TextView(this@WidgetPreviewActivity), ViewGroup.LayoutParams.MATCH_PARENT, dp(8))
                }
            }
        }
    }

    /** This activity's context with the dark theme forced on or off, which is what the widget looks at to pick its colors. */
    private fun themedContext(night: Boolean): Context {
        val configuration = Configuration(resources.configuration)
        val mode = if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        configuration.uiMode = (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or mode
        return createConfigurationContext(configuration)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
