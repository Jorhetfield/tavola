package com.tavola.feature.shopping

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.snackbar.TavolaSnackbarHost
import com.tavola.core.designsystem.snackbar.TavolaUndoSnackbarEffect
import com.tavola.core.designsystem.topbar.TavolaTopBar

/**
 * The shopping list tab.
 *
 * @param addSheetOpen whether the sheet that adds items is open. `:app` owns it, because the button that opens it is the action
 * docked to the floating bar, which lives in the app shell.
 * @param onAddSheetDismiss asks `:app` to close the sheet.
 */
@Composable
fun ShoppingScreen(
    addSheetOpen: Boolean,
    onAddSheetDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onAddSheetDismiss) {
        ShoppingActions(
            onDraftNameChange = viewModel::onDraftNameChange,
            onDraftQuantityChange = viewModel::onDraftQuantityChange,
            onDraftCategoryPick = viewModel::onDraftCategoryPick,
            onAddClick = viewModel::onAddClick,
            onAddSheetDismiss = {
                viewModel.onAddSheetDismiss()
                onAddSheetDismiss()
            },
            onItemCheckedChange = viewModel::onItemCheckedChange,
            onItemRemove = viewModel::onItemRemove,
            onRemoveCheckedClick = viewModel::onRemoveCheckedClick,
            onRemoveUndo = viewModel::onRemoveUndo,
            onRemovedShown = viewModel::onRemovedShown,
            onRetryClick = viewModel::onRetryClick,
        )
    }
    ShoppingContent(state, actions, addSheetOpen, modifier)
}

/** Stateless part of the screen: it receives the state and the actions, so it can be previewed and tested. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingContent(state: ShoppingUiState, actions: ShoppingActions, addSheetOpen: Boolean, modifier: Modifier = Modifier) {
    val topBarScroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }
    val success = state as? ShoppingUiState.Success
    val removed = success?.removedItems.orEmpty()
    TavolaUndoSnackbarEffect(
        removed = removed.takeIf { items -> items.isNotEmpty() },
        message = resources.getQuantityString(R.plurals.shopping_removed, removed.size, removed.size),
        undoLabel = stringResource(R.string.shopping_undo),
        hostState = snackbarHostState,
        onUndo = actions.onRemoveUndo,
        onShown = actions.onRemovedShown,
    )

    TavolaScaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            TavolaTopBar(
                title = stringResource(R.string.shopping_title),
                subtitle = success?.let { loaded ->
                    subtitleOf(
                        loaded,
                        resources.getQuantityString(R.plurals.shopping_pending, loaded.pendingCount, loaded.pendingCount),
                        stringResource(R.string.shopping_all_done),
                    )
                },
                scrollBehavior = topBarScroll,
            )
        },
        reservesFloatingBarSpace = true,
        snackbarHost = { TavolaSnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        when (state) {
            ShoppingUiState.Loading -> CenteredBox(contentPadding) { TavolaLoadingIndicator() }
            ShoppingUiState.Error -> CenteredBox(contentPadding) { ShoppingError(actions.onRetryClick) }
            is ShoppingUiState.Success -> ShoppingBody(state, actions, contentPadding)
        }
    }
    if (addSheetOpen && success != null) ShoppingAddSheet(success.draft, actions)
}

/** What the title bar says under the title: how many items are left, "all done" once everything is ticked, nothing for an empty list. */
internal fun subtitleOf(state: ShoppingUiState.Success, pending: String, allDone: String): String? = when {
    state.totalCount == 0 -> null
    state.pendingCount == 0 -> allDone
    else -> pending
}
