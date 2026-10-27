package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction

/**
 * Read-only detail of one recipe, full screen (no floating bar).
 *
 * @param onBack goes back to where the user came from.
 * @param onEditClick opens the editor for the recipe. `:app` owns the navigation.
 */
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    onEditClick: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailViewModel = hiltViewModel<RecipeDetailViewModel, RecipeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(recipeId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeDetailContent(state, onBack, viewModel::onRetryClick, { onEditClick(recipeId) }, modifier)
}

/** Stateless part of the screen: it receives the state and the actions, so it can be previewed and tested. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailContent(
    state: RecipeDetailUiState,
    onBack: () -> Unit,
    onRetryClick: () -> Unit,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val title = (state as? RecipeDetailUiState.Success)?.recipe?.title ?: stringResource(R.string.recipe_detail_title_fallback)

    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            TavolaTopBar(
                title = title,
                navigationAction = TavolaTopBarAction(Icons.AutoMirrored.Rounded.ArrowBack, DesignSystemR.string.topbar_back, onBack),
                actions = if (state is RecipeDetailUiState.Success) {
                    listOf(TavolaTopBarAction(Icons.Rounded.Edit, DesignSystemR.string.topbar_edit, onEditClick))
                } else {
                    emptyList()
                },
                scrollBehavior = topBarScroll,
            )
        },
    ) { contentPadding ->
        when (state) {
            RecipeDetailUiState.Loading -> CenteredBox(contentPadding) { TavolaLoadingIndicator() }

            RecipeDetailUiState.Error -> CenteredBox(contentPadding) {
                MessageWithAction(stringResource(R.string.recipe_detail_error), stringResource(R.string.recipe_detail_retry), onRetryClick)
            }

            RecipeDetailUiState.NotFound -> CenteredBox(contentPadding) { RecipeNotFound(onBack) }

            is RecipeDetailUiState.Success -> RecipeDetailBody(state.recipe, contentPadding)
        }
    }
}

@Composable
private fun RecipeNotFound(onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.md),
    ) {
        Icon(
            imageVector = Icons.Rounded.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MessageWithAction(
            message = stringResource(R.string.recipe_detail_not_found),
            actionText = stringResource(DesignSystemR.string.topbar_back),
            onActionClick = onBack,
        )
    }
}
