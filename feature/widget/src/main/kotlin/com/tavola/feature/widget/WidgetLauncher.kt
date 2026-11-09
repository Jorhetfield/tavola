package com.tavola.feature.widget

import android.content.Intent

/**
 * How the widget opens the app. Only `:app` knows its screens, so it implements this (and binds it with Hilt) and the widget just
 * asks for the intent.
 */
interface WidgetLauncher {
    /** Opens the app on its first screen. */
    fun openAppIntent(): Intent

    /** Opens the detail of the recipe [recipeId]. */
    fun openRecipeIntent(recipeId: String): Intent
}
