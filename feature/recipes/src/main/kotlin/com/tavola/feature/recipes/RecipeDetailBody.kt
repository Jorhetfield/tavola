package com.tavola.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.motion.PlayedEntrances
import com.tavola.core.designsystem.motion.entranceDelayMillis
import com.tavola.core.designsystem.motion.popOnAppear
import com.tavola.core.designsystem.motion.staggeredEntrance
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.Ingredient
import com.tavola.domain.model.Recipe

private val InfoIconSize = 18.dp

/** Time and tags, the ingredients and the numbered steps of a recipe. */
@Composable
internal fun RecipeDetailBody(recipe: Recipe, contentPadding: PaddingValues) {
    val spacing = TavolaTheme.spacing
    val played = remember { PlayedEntrances() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.xl),
    ) {
        if (recipe.prepTimeMinutes != null || recipe.tags.isNotEmpty()) {
            item { RecipeInfo(recipe, Modifier.staggeredEntrance(INFO_KEY, index = 0, played = played)) }
        }
        item { IngredientsSection(recipe.ingredients, Modifier.staggeredEntrance(INGREDIENTS_KEY, index = 1, played = played)) }
        item { StepsSection(recipe.steps, Modifier.staggeredEntrance(STEPS_KEY, index = 2, played = played)) }
    }
}

@Composable
private fun RecipeInfo(recipe: Recipe, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm),
    ) {
        recipe.prepTimeMinutes?.let { minutes ->
            InfoPill(text = stringResource(R.string.recipe_detail_prep_time, minutes), showClock = true)
        }
        recipe.tags.forEach { tag -> InfoPill(text = tag) }
    }
}

@Composable
private fun InfoPill(text: String, showClock: Boolean = false) {
    val spacing = TavolaTheme.spacing
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            if (showClock) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(InfoIconSize))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun IngredientsSection(ingredients: List<Ingredient>, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    SectionHeader(R.string.recipe_detail_ingredients, modifier) {
        if (ingredients.isEmpty()) {
            EmptySection(R.string.recipe_detail_no_ingredients)
        } else {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                Column(modifier = Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    ingredients.forEach { ingredient -> IngredientRow(ingredient) }
                }
            }
        }
    }
}

@Composable
private fun IngredientRow(ingredient: Ingredient) {
    val resources = LocalResources.current
    val amount = ingredientAmount(ingredient, rememberLocale(), resources)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Text(text = ingredient.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (amount != null) {
            Text(text = amount, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StepsSection(steps: List<String>, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    SectionHeader(R.string.recipe_detail_steps, modifier) {
        if (steps.isEmpty()) {
            EmptySection(R.string.recipe_detail_no_steps)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                steps.forEachIndexed { index, step ->
                    StepRow(
                        number = index + 1,
                        text = step,
                        popDelayMillis =
                            STEPS_POP_BASE_MILLIS + entranceDelayMillis(index),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRow(number: Int, text: String, popDelayMillis: Long) {
    Row(horizontalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md), verticalAlignment = Alignment.Top) {
        StepNumberBadge(number = number, modifier = Modifier.popOnAppear(popDelayMillis))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SectionHeader(titleRes: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md)) {
        Text(text = stringResource(titleRes), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
private fun EmptySection(messageRes: Int) {
    Text(
        text = stringResource(messageRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private const val INFO_KEY = "info"
private const val INGREDIENTS_KEY = "ingredients"
private const val STEPS_KEY = "steps"
private const val STEPS_POP_BASE_MILLIS = 120L
