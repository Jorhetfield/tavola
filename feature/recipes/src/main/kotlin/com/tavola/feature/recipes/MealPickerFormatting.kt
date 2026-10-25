package com.tavola.feature.recipes

import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import com.tavola.core.designsystem.recipe.RecipeRowModel
import com.tavola.domain.model.Recipe
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaDayOfWeek
import kotlinx.datetime.toJavaMonth

@Composable
internal fun rememberLocale(): Locale = LocalConfiguration.current.locales[0]

/** "miércoles 21 de octubre" (el orden y las palabras de enlace salen de los recursos de cada idioma). */
internal fun mealDateLabel(date: LocalDate, locale: Locale, resources: Resources): String = resources.getString(
    R.string.meal_picker_date_format,
    date.dayOfWeek.toJavaDayOfWeek().getDisplayName(TextStyle.FULL, locale),
    date.dayOfMonth,
    date.month.toJavaMonth().getDisplayName(TextStyle.FULL, locale),
)

/** Filas de la lista con el detalle ya formateado ("50 min · Español") y la receta actual marcada. */
@Composable
internal fun rememberRecipeRows(recipes: List<Recipe>, selectedRecipeId: String?): List<RecipeRowModel> {
    val resources = LocalResources.current
    return remember(recipes, selectedRecipeId, resources) {
        recipes.map { recipe ->
            val details = listOfNotNull(
                recipe.prepTimeMinutes?.let { minutes -> resources.getString(R.string.meal_picker_prep_time, minutes) },
                recipe.tags.firstOrNull(),
            )
            RecipeRowModel(
                id = recipe.id,
                title = recipe.title,
                subtitle = details.joinToString(" · ").ifEmpty { null },
                selected = recipe.id == selectedRecipeId,
            )
        }
    }
}
