package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.R as DesignSystemR
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.snackbar.TavolaSnackbarHost
import com.tavola.core.designsystem.snackbar.TavolaSnackbarVisuals
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.core.designsystem.topbar.TavolaTopBar
import com.tavola.core.designsystem.topbar.TavolaTopBarAction
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Read-only detail of one recipe, full screen (no floating bar).
 *
 * @param onBack goes back to where the user came from.
 * @param onEditClick opens the editor for the recipe. `:app` owns the navigation.
 * @param addToShopping the content that puts the ingredients on the shopping list, shown in a bottom sheet. `:app` provides it, since
 * this module does not know the shopping feature.
 */
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    onBack: () -> Unit,
    onEditClick: (recipeId: String) -> Unit,
    addToShopping: AddToShoppingSlot,
    modifier: Modifier = Modifier,
    viewModel: RecipeDetailViewModel = hiltViewModel<RecipeDetailViewModel, RecipeDetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(recipeId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    // How many things the sheet just added, until the notice that says so has been shown. Zero when there is nothing to say.
    var addedCount by rememberSaveable { mutableIntStateOf(0) }
    val candidates = rememberShoppingCandidates((state as? RecipeDetailUiState.Success)?.recipe)

    LaunchedEffect(addedCount) {
        if (addedCount > 0) {
            val message = resources.getQuantityString(R.plurals.recipe_detail_added_to_shopping, addedCount, addedCount)
            val visuals = TavolaSnackbarVisuals(message, actionLabel = null, withDismissAction = true, timeoutMillis = NOTICE_MILLIS)
            withTimeoutOrNull(NOTICE_MILLIS) { snackbarHostState.showSnackbar(visuals) }
            // Cleared only once the notice is over: clearing it first would restart this effect and cancel the notice at once.
            addedCount = 0
        }
    }

    RecipeDetailContent(
        state = state,
        onBack = onBack,
        onRetryClick = viewModel::onRetryClick,
        onEditClick = { onEditClick(recipeId) },
        onAddToShoppingClick = { sheetOpen = true },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
    if (sheetOpen && candidates.isNotEmpty()) {
        AddToShoppingHost(
            candidates = candidates,
            onDismiss = { added ->
                sheetOpen = false
                // Closing twice (the sheet reports 0 when it is swiped away) must not wipe the count of what was just added.
                if (added > 0) addedCount = added
            },
            content = addToShopping,
        )
    }
}

private const val NOTICE_MILLIS = 4_000L

/**
 * Stateless part of the screen: it receives the state and the actions, so it can be previewed and tested. The button that adds the
 * ingredients to the shopping list is only there when [onAddToShoppingClick] is given and the recipe has ingredients.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailContent(
    state: RecipeDetailUiState,
    onBack: () -> Unit,
    onRetryClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onAddToShoppingClick: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
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
                actions = detailActions(state, onEditClick, onAddToShoppingClick),
                scrollBehavior = topBarScroll,
            )
        },
        snackbarHost = { snackbarHostState?.let { hostState -> TavolaSnackbarHost(hostState) } },
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

/** The buttons of the top bar: add to the shopping list (if there is something to add and a way to do it) and edit. */
private fun detailActions(
    state: RecipeDetailUiState,
    onEditClick: () -> Unit,
    onAddToShoppingClick: (() -> Unit)?,
): List<TavolaTopBarAction> {
    if (state !is RecipeDetailUiState.Success) return emptyList()
    val addToShopping = onAddToShoppingClick?.takeIf { state.recipe.ingredients.isNotEmpty() }?.let { onClick ->
        TavolaTopBarAction(Icons.Rounded.AddShoppingCart, R.string.recipe_detail_add_to_shopping, onClick)
    }
    return listOfNotNull(addToShopping, TavolaTopBarAction(Icons.Rounded.Edit, DesignSystemR.string.topbar_edit, onEditClick))
}
