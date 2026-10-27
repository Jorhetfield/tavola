package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.topbar.TavolaTopBar

/**
 * The recipe book tab.
 *
 * @param onRecipeClick opens the detail of the recipe; `:app` owns the navigation.
 */
@Composable
fun RecipeListScreen(
    onRecipeClick: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onRecipeClick) {
        RecipeListActions(
            onQueryChange = viewModel::onQueryChange,
            onRecipeClick = onRecipeClick,
            onRecipeRemove = viewModel::onRecipeRemove,
            onRecipeRemoveUndo = viewModel::onRecipeRemoveUndo,
            onRecipeRemovedShown = viewModel::onRecipeRemovedShown,
            onRetryClick = viewModel::onRetryClick,
        )
    }
    RecipeListContent(state, actions, modifier)
}

/** Stateless part of the screen: it receives the state and the actions, so it can be previewed and tested. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListContent(state: RecipeListUiState, actions: RecipeListActions, modifier: Modifier = Modifier) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    RemovedRecipeSnackbarEffect(
        removedRecipe = (state as? RecipeListUiState.Success)?.removedRecipe,
        snackbarHostState = snackbarHostState,
        onUndo = actions.onRecipeRemoveUndo,
        onShown = actions.onRecipeRemovedShown,
    )
    val success = state as? RecipeListUiState.Success
    // The subtitle counts what the search shows; it is hidden only when the recipe book itself is empty.
    val shown = success?.recipes?.size ?: 0
    val hasRecipes = (success?.totalRecipes ?: 0) > 0

    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            TavolaTopBar(
                title = stringResource(R.string.recipe_list_title),
                subtitle = if (hasRecipes) resources.getQuantityString(R.plurals.recipe_list_count, shown, shown) else null,
                scrollBehavior = topBarScroll,
            )
        },
        reservesFloatingBarSpace = true,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (state) {
            RecipeListUiState.Loading -> CenteredBox(contentPadding) { TavolaLoadingIndicator() }
            RecipeListUiState.Error -> CenteredBox(contentPadding) { RecipeListError(actions.onRetryClick) }
            is RecipeListUiState.Success -> RecipeListBody(state, actions, contentPadding)
        }
    }
}

@Composable
private fun CenteredBox(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
