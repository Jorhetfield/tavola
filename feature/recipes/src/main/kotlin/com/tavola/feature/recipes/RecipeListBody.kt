package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.tavola.core.designsystem.field.TavolaSearchField
import com.tavola.core.designsystem.motion.LocalPlayedEntrances
import com.tavola.core.designsystem.motion.PlayedEntrances
import com.tavola.core.designsystem.recipe.TavolaRecipeRow
import com.tavola.core.designsystem.theme.TavolaTheme

/** Search field above the list; the list, or the empty state, below it. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecipeListBody(state: RecipeListUiState.Success, actions: RecipeListActions, contentPadding: PaddingValues) {
    val spacing = TavolaTheme.spacing
    val motion = MaterialTheme.motionScheme
    val focusManager = LocalFocusManager.current
    val rows = rememberRecipeRows(state.recipes, selectedRecipeId = null)
    // The cascade plays once per row (the shell keeps the set), so changing tab or coming back from a detail does not replay it.
    val played = LocalPlayedEntrances.current ?: remember { PlayedEntrances() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        if (state.totalRecipes > 0) {
            item(key = SEARCH_KEY) {
                TavolaSearchField(
                    value = state.query,
                    onValueChange = actions.onQueryChange,
                    placeholder = stringResource(R.string.recipe_list_search_placeholder),
                    clearContentDescription = stringResource(R.string.recipe_list_search_clear),
                    onSearch = { focusManager.clearFocus() },
                    modifier = Modifier.padding(bottom = spacing.sm),
                )
            }
        }
        if (rows.isEmpty()) {
            item(key = EMPTY_KEY) { RecipeListEmpty(state) }
        } else {
            itemsIndexed(rows, key = { _, rowModel -> rowModel.id }) { index, row ->
                TavolaRecipeRow(
                    model = row,
                    onClick = { actions.onRecipeClick(row.id) },
                    modifier = Modifier
                        .staggeredEntrance(key = "recipe:${row.id}", index = index, played = played)
                        .animateItem(
                            fadeInSpec = motion.defaultEffectsSpec(),
                            placementSpec = motion.defaultSpatialSpec(),
                            fadeOutSpec = motion.fastEffectsSpec(),
                        ),
                )
            }
        }
    }
}

/** Either "there are no recipes at all" or "nothing matches the search". */
@Composable
private fun RecipeListEmpty(state: RecipeListUiState.Success) {
    val spacing = TavolaTheme.spacing
    val noRecipes = state.totalRecipes == 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Icon(
            imageVector = if (noRecipes) Icons.Rounded.MenuBook else Icons.Rounded.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = if (noRecipes) Modifier else Modifier.wiggleOnAppear(),
        )
        Text(
            text = if (noRecipes) {
                stringResource(R.string.recipe_list_empty_title)
            } else {
                stringResource(R.string.recipe_list_no_results, state.query.trim())
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (noRecipes) {
            Text(
                text = stringResource(R.string.recipe_list_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RecipeListError(onRetryClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.lg),
    ) {
        Text(
            text = stringResource(R.string.recipe_list_error),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetryClick, shapes = ButtonDefaults.shapes()) {
            Text(stringResource(R.string.recipe_list_retry))
        }
    }
}

private const val SEARCH_KEY = "search"
private const val EMPTY_KEY = "empty"
