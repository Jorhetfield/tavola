package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.R as DesignSystemR
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.snackbar.TavolaSnackbarHost
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction

/**
 * Create / edit form of a recipe, full screen (no floating bar). A blank [recipeId] creates a new recipe.
 *
 * @param onBack leaves the form, both when going back and once the recipe is saved; `:app` owns the navigation.
 */
@Composable
fun RecipeEditorScreen(
    recipeId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeEditorViewModel = hiltViewModel<RecipeEditorViewModel, RecipeEditorViewModel.Factory>(
        creationCallback = { factory -> factory.create(recipeId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    val saved = (state as? RecipeEditorUiState.Editing)?.saved == true
    LaunchedEffect(saved) { if (saved) onBack() }
    RecipeEditorContent(state, actions, onBack, viewModel::onRetryClick, viewModel::onSaveErrorShown, modifier)
}

private fun RecipeEditorViewModel.toActions() = RecipeEditorActions(
    onTitleChange = ::onTitleChange,
    onPrepTimeChange = ::onPrepTimeChange,
    onTagInputChange = ::onTagInputChange,
    onTagAdd = ::onTagAdd,
    onTagSuggestionClick = ::onTagSuggestionClick,
    onTagRemove = ::onTagRemove,
    onIngredientAdd = ::onIngredientAdd,
    onIngredientRemove = ::onIngredientRemove,
    onIngredientNameChange = ::onIngredientNameChange,
    onIngredientQuantityChange = ::onIngredientQuantityChange,
    onIngredientUnitChange = ::onIngredientUnitChange,
    onStepAdd = ::onStepAdd,
    onStepRemove = ::onStepRemove,
    onStepChange = ::onStepChange,
    onStepMove = ::onStepMove,
    onSaveClick = ::onSaveClick,
)

/** Stateless part of the screen: it receives the state and the actions, so it can be previewed and tested. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorContent(
    state: RecipeEditorUiState,
    actions: RecipeEditorActions,
    onBack: () -> Unit,
    onRetryClick: () -> Unit,
    onSaveErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val form = state as? RecipeEditorUiState.Editing
    val saveFailedMessage = stringResource(R.string.recipe_editor_save_failed)
    LaunchedEffect(form?.saveFailed) {
        if (form?.saveFailed == true) {
            snackbarHostState.showSnackbar(saveFailedMessage)
            onSaveErrorShown()
        }
    }

    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            TavolaTopBar(
                title = stringResource(if (form?.isNew == false) R.string.recipe_detail_edit else R.string.recipe_editor_title_new),
                navigationAction = TavolaTopBarAction(Icons.AutoMirrored.Rounded.ArrowBack, DesignSystemR.string.topbar_back, onBack),
                actions = listOfNotNull(
                    form?.let { TavolaTopBarAction(Icons.Rounded.Check, DesignSystemR.string.topbar_save, actions.onSaveClick) },
                ),
                scrollBehavior = topBarScroll,
            )
        },
        snackbarHost = { TavolaSnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (state) {
            RecipeEditorUiState.Loading -> CenteredBox(contentPadding) { TavolaLoadingIndicator() }

            RecipeEditorUiState.Error -> CenteredBox(contentPadding) {
                MessageWithAction(stringResource(R.string.recipe_editor_error), stringResource(R.string.recipe_editor_retry), onRetryClick)
            }

            RecipeEditorUiState.NotFound -> CenteredBox(contentPadding) {
                MessageWithAction(
                    stringResource(R.string.recipe_detail_not_found),
                    stringResource(DesignSystemR.string.topbar_back),
                    onBack,
                )
            }

            is RecipeEditorUiState.Editing -> RecipeEditorBody(state, actions, contentPadding)
        }
    }
}
