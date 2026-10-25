package com.tavola.feature.recipes

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.tavola.core.designsystem.field.TavolaSearchField
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.recipe.TavolaRecipeRow
import com.tavola.core.designsystem.theme.TavolaTheme

/** Pestaña de recetas: buscador y lista. [onWriteInstead] salta a "comida rápida" llevándose el texto buscado. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecipesPane(
    state: MealPickerUiState,
    selectedRecipeId: String?,
    actions: MealPickerActions,
    onWriteInstead: (text: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        MealPickerUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TavolaLoadingIndicator()
        }

        MealPickerUiState.Error -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
            ) {
                Text(
                    text = stringResource(R.string.meal_picker_error),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = actions.onRetryClick, shapes = ButtonDefaults.shapes()) {
                    Text(stringResource(R.string.meal_picker_retry))
                }
            }
        }

        is MealPickerUiState.Success -> RecipesContent(state, selectedRecipeId, actions, onWriteInstead, modifier)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RecipesContent(
    state: MealPickerUiState.Success,
    selectedRecipeId: String?,
    actions: MealPickerActions,
    onWriteInstead: (String) -> Unit,
    modifier: Modifier,
) {
    val spacing = TavolaTheme.spacing
    val motion = MaterialTheme.motionScheme
    val focusManager = LocalFocusManager.current
    val rows = rememberRecipeRows(state.recipes, selectedRecipeId)

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        TavolaSearchField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            placeholder = stringResource(R.string.meal_picker_search_placeholder),
            clearContentDescription = stringResource(R.string.meal_picker_search_clear),
            onSearch = { focusManager.clearFocus() },
        )
        AnimatedContent(
            targetState = rows.isEmpty(),
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
            label = "recipesOrEmpty",
        ) { empty ->
            if (empty) {
                EmptyResults(state, onWriteInstead)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(rows, key = { it.id }) { row ->
                        TavolaRecipeRow(
                            model = row,
                            onClick = { state.recipes.firstOrNull { it.id == row.id }?.let(actions.onRecipeClick) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = motion.defaultEffectsSpec(),
                                placementSpec = motion.defaultSpatialSpec(),
                                fadeOutSpec = motion.fastEffectsSpec(),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyResults(state: MealPickerUiState.Success, onWriteInstead: (String) -> Unit) {
    val spacing = TavolaTheme.spacing
    val query = state.query.trim()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Icon(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (state.totalRecipes == 0) {
                stringResource(R.string.meal_picker_empty_no_recipes)
            } else {
                stringResource(R.string.meal_picker_empty_no_results, query)
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(onClick = { onWriteInstead(query) }, shapes = ButtonDefaults.shapes()) {
            Icon(Icons.Rounded.EditNote, contentDescription = null)
            Text(
                text = if (query.isEmpty()) {
                    stringResource(R.string.meal_picker_empty_write)
                } else {
                    stringResource(R.string.meal_picker_empty_write_query, query)
                },
                modifier = Modifier.padding(start = spacing.sm),
            )
        }
    }
}
