package com.tavola.data.recipe

import com.tavola.domain.model.RecipeRef

/** Recetas de ejemplo (España, Italia, Grecia) mientras no exista el CRUD real de recetas. */
internal object MediterraneanCatalog {
    val recipes = listOf(
        RecipeRef("gazpacho", "Gazpacho", prepTimeMinutes = 15, tags = listOf("Español")),
        RecipeRef("tortilla", "Tortilla de patatas", prepTimeMinutes = 30, tags = listOf("Español")),
        RecipeRef("paella", "Paella valenciana", prepTimeMinutes = 50, tags = listOf("Español")),
        RecipeRef("pisto", "Pisto con huevo", prepTimeMinutes = 35, tags = listOf("Español")),
        RecipeRef("moussaka", "Moussaka", prepTimeMinutes = 70, tags = listOf("Griego")),
        RecipeRef("greek-salad", "Ensalada griega", prepTimeMinutes = 10, tags = listOf("Griego")),
        RecipeRef("tzatziki", "Tzatziki con pita", prepTimeMinutes = 15, tags = listOf("Griego")),
        RecipeRef("souvlaki", "Souvlaki de pollo", prepTimeMinutes = 40, tags = listOf("Griego")),
        RecipeRef("risotto", "Risotto de setas", prepTimeMinutes = 40, tags = listOf("Italiano")),
        RecipeRef("pomodoro", "Pasta al pomodoro", prepTimeMinutes = 25, tags = listOf("Italiano")),
        RecipeRef("pizza", "Pizza margarita", prepTimeMinutes = 45, tags = listOf("Italiano")),
        RecipeRef("tiramisu", "Tiramisú", prepTimeMinutes = 30, tags = listOf("Italiano")),
    )
}
