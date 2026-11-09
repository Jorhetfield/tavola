package com.tavola.feature.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tavola.core.designsystem.field.TavolaQuickAddField
import com.tavola.core.designsystem.motion.LocalPlayedEntrances
import com.tavola.core.designsystem.motion.PlayedEntrances
import com.tavola.core.designsystem.motion.staggeredEntrance
import com.tavola.core.designsystem.shopping.ShoppingRowModel
import com.tavola.core.designsystem.shopping.TavolaShoppingCategoryHeader
import com.tavola.core.designsystem.shopping.TavolaShoppingRow
import com.tavola.core.designsystem.swipe.TavolaSwipeToRemove
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.domain.model.ShoppingCategory

private const val ADD_KEY = "add"
private const val EMPTY_KEY = "empty"
private const val REMOVE_CHECKED_KEY = "remove-checked"

/** The quick add field and the category chips on top; below them the items by category, or the empty state, and "Remove ticked". */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ShoppingBody(state: ShoppingUiState.Success, actions: ShoppingActions, contentPadding: PaddingValues) {
    val spacing = TavolaTheme.spacing
    val motion = MaterialTheme.motionScheme
    val removeLabel = stringResource(R.string.shopping_remove)
    // The cascade plays once per row (the shell keeps the set), so changing tab or coming back does not replay it.
    val played = LocalPlayedEntrances.current ?: remember { PlayedEntrances() }
    // How many rows come before each section, so the cascade runs through the whole list and not once per section.
    val rowsBefore = remember(state.sections) { state.sections.runningFold(0) { count, section -> count + section.items.size } }

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
        item(key = ADD_KEY) {
            ShoppingAddBar(state.draftName, state.draftCategory, actions, modifier = Modifier.padding(bottom = spacing.sm))
        }
        if (state.sections.isEmpty()) {
            item(key = EMPTY_KEY) { ShoppingEmpty() }
        }
        state.sections.forEachIndexed { sectionIndex, section ->
            item(key = "header:${section.category.name}") {
                TavolaShoppingCategoryHeader(
                    label = stringResource(section.category.labelRes),
                    icon = section.category.icon,
                    count = section.items.size,
                    modifier = Modifier
                        .padding(top = spacing.md)
                        .then(itemMotion()),
                )
            }
            items(section.items, key = { item -> item.id }) { item ->
                val index = rowsBefore[sectionIndex] + section.items.indexOf(item)
                TavolaSwipeToRemove(
                    removeLabel = removeLabel,
                    onRemove = { actions.onItemRemove(item.id) },
                    modifier = Modifier
                        .staggeredEntrance(key = "shopping:${item.id}", index = index, played = played)
                        .then(itemMotion()),
                ) {
                    TavolaShoppingRow(
                        model = ShoppingRowModel(item.id, item.name, item.quantity, item.checked),
                        onCheckedChange = { checked -> actions.onItemCheckedChange(item.id, checked) },
                    )
                }
            }
        }
        if (state.checkedCount > 0) {
            item(key = REMOVE_CHECKED_KEY) {
                FilledTonalButton(
                    onClick = actions.onRemoveCheckedClick,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier
                        .padding(top = spacing.md)
                        .then(itemMotion()),
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Text(
                        text = stringResource(R.string.shopping_remove_checked, state.checkedCount),
                        modifier = Modifier.padding(start = spacing.sm),
                    )
                }
            }
        }
    }
}

/** The quick add field with the chips that pick its category. */
@Composable
internal fun ShoppingAddBar(draftName: String, draftCategory: ShoppingCategory, actions: ShoppingActions, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm)) {
        TavolaQuickAddField(
            value = draftName,
            onValueChange = actions.onDraftNameChange,
            placeholder = stringResource(R.string.shopping_add_placeholder),
            addContentDescription = stringResource(R.string.shopping_add),
            onAdd = actions.onAddClick,
        )
        ShoppingCategoryChips(selected = draftCategory, onSelect = actions.onDraftCategoryChange)
    }
}

/** How an item of the list enters, leaves and moves: fade with the effects spring, placement with the spatial one. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LazyItemScope.itemMotion(): Modifier {
    val motion = MaterialTheme.motionScheme
    return Modifier.animateItem(
        fadeInSpec = motion.defaultEffectsSpec(),
        placementSpec = motion.defaultSpatialSpec(),
        fadeOutSpec = motion.fastEffectsSpec(),
    )
}
