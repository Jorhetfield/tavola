package com.tavola.feature.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.choice.PickRowModel
import com.tavola.core.designsystem.choice.TavolaPickRow
import com.tavola.core.designsystem.loading.TavolaLoadingIndicator
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCandidate

/**
 * The content of the sheet that adds to the shopping list what [candidates] offers (the ingredients of a recipe). It is meant to be
 * placed inside a bottom sheet by whoever hosts it, which hands it [onClose]: called with how many things were added, once they are.
 */
@Composable
fun AddToShoppingSheetContent(
    candidates: List<ShoppingCandidate>,
    onClose: (added: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddToShoppingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(candidates) { viewModel.onCandidates(candidates) }
    val actions = remember(viewModel, onClose) {
        AddToShoppingActions(
            onToggle = viewModel::onToggle,
            onSelectAllToggle = viewModel::onSelectAllToggle,
            onAddClick = { viewModel.onAddClick(onClose) },
            onRetryClick = viewModel::onRetryClick,
        )
    }
    AddToShoppingContent(state, actions, modifier)
}

/** Stateless part of the sheet: it receives the state and the actions, so it can be previewed and tested. */
@Composable
fun AddToShoppingContent(state: AddToShoppingUiState, actions: AddToShoppingActions, modifier: Modifier = Modifier) {
    val spacing = TavolaTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = spacing.lg)
            .padding(bottom = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Text(text = stringResource(R.string.shopping_pick_title), style = MaterialTheme.typography.headlineSmall)
        when (state) {
            AddToShoppingUiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { TavolaLoadingIndicator() }

            AddToShoppingUiState.Error -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ShoppingError(actions.onRetryClick)
            }

            is AddToShoppingUiState.Success -> AddToShoppingPicker(state, actions)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColumnScope.AddToShoppingPicker(state: AddToShoppingUiState.Success, actions: AddToShoppingActions) {
    val spacing = TavolaTheme.spacing
    val resources = LocalResources.current
    val onListLabel = stringResource(R.string.shopping_pick_on_list)

    Text(
        text = stringResource(R.string.shopping_pick_hint),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (state.canSelectAny) {
        TextButton(onClick = actions.onSelectAllToggle, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(if (state.allSelected) R.string.shopping_pick_select_none else R.string.shopping_pick_select_all))
        }
    }
    Column(
        modifier = Modifier
            .weight(1f, fill = false)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        state.rows.forEach { row ->
            TavolaPickRow(
                model = PickRowModel(
                    id = row.index.toString(),
                    label = row.candidate.name,
                    detail = row.candidate.quantity,
                    supporting = if (row.onList) onListLabel else null,
                    selected = row.selected,
                    enabled = !row.onList,
                ),
                onSelectedChange = { selected -> actions.onToggle(row.index, selected) },
            )
        }
    }
    Button(
        onClick = actions.onAddClick,
        enabled = state.selectedCount > 0 && !state.saving,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(Icons.Rounded.AddShoppingCart, contentDescription = null)
        Text(
            text = resources.getQuantityString(R.plurals.shopping_pick_add, state.selectedCount, state.selectedCount),
            modifier = Modifier.padding(start = spacing.sm),
        )
    }
}
