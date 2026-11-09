package com.tavola.feature.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceRemoteViews
import com.tavola.domain.model.DayTask
import com.tavola.domain.model.PlannedMeal
import com.tavola.domain.model.RecipeRef
import kotlinx.datetime.LocalDate

/**
 * Debug only: draws the "Today" widget with example data as the views a launcher would show, so it can be looked at without a
 * signed-in account or a home screen (the debug build of the app has a screen that lists them, see `WidgetPreviewActivity`).
 */
object WidgetPreview {
    enum class Size(val dp: DpSize) {
        Small(SmallWidgetSize),
        Medium(MediumWidgetSize),
        Large(LargeWidgetSize),
        Tall(DpSize(250.dp, 360.dp)),
    }

    enum class Scenario { Full, Empty, OnlyTasks, SignedOut }

    private val date = LocalDate(2026, 11, 10)

    private fun state(scenario: Scenario): TodayWidgetState = when (scenario) {
        Scenario.Full -> TodayWidgetState.Content(
            date = date,
            lunch = PlannedMeal.FromRecipe(RecipeRef("paella", "Paella valenciana", prepTimeMinutes = 50, tags = listOf("Español"))),
            dinner = PlannedMeal.Quick("Sobras de ayer", note = "Con ensalada"),
            tasks = listOf(
                DayTask("t1", "Sacar el pollo a descongelar", createdAt = 1),
                DayTask("t2", "Comprar pimientos", done = true, createdAt = 2),
                DayTask("t3", "Preparar la masa de la pizza", createdAt = 3),
                DayTask("t4", "Llamar a la panadería", createdAt = 4),
                DayTask("t5", "Regar las albahacas", done = true, createdAt = 5),
            ),
        )

        Scenario.Empty -> TodayWidgetState.Content(date, lunch = null, dinner = null, tasks = emptyList())

        Scenario.OnlyTasks -> TodayWidgetState.Content(
            date,
            lunch = null,
            dinner = null,
            tasks = listOf(DayTask("t1", "Sacar el pollo", createdAt = 1)),
        )

        Scenario.SignedOut -> TodayWidgetState.SignedOut
    }

    /** The widget for [scenario] at [size], at [hourOfDay] (which decides the next meal of the small one). */
    @OptIn(ExperimentalGlanceRemoteViewsApi::class)
    suspend fun remoteViews(context: Context, size: Size, scenario: Scenario, hourOfDay: Int = 12): RemoteViews {
        val launcher = object : WidgetLauncher {
            override fun openAppIntent(): Intent = Intent()

            override fun openRecipeIntent(recipeId: String): Intent = Intent()
        }
        return GlanceRemoteViews().compose(context, size.dp) {
            TodayWidgetContent(state(scenario), hourOfDay, launcher, scrollable = false)
        }.remoteViews
    }
}
