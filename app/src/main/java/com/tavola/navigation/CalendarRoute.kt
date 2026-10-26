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
