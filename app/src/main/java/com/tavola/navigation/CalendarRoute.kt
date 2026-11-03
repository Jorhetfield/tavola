package com.tavola.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** App routes. They carry only simple identifiers, never heavy objects. Only `:app` knows them. */
@Serializable
data object CalendarRoute : NavKey

@Serializable
data object RecipesRoute : NavKey

@Serializable
data class RecipeDetailRoute(val recipeId: String) : NavKey

/** Create or edit a recipe; a blank [recipeId] creates a new one. */
@Serializable
data class RecipeEditorRoute(val recipeId: String = "") : NavKey

/** The group of the person: its code, its members and what a member can do. Opened from the top bar of the calendar. */
@Serializable
data object GroupRoute : NavKey
