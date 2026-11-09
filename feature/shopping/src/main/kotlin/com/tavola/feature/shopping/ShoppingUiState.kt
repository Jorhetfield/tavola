package com.tavola.feature.shopping

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem

/** The items of one [category], with the ones still to buy first and the ticked ones after them. */
@Immutable
data class ShoppingSection(val category: ShoppingCategory, val items: List<ShoppingItem>)

sealed interface ShoppingUiState {
    data object Loading : ShoppingUiState

    data object Error : ShoppingUiState

    /**
     * @property sections the list grouped by category, in the order of the categories; a category with no items is left out.
     * @property draftName text of the quick add field.
     * @property draftCategory category the next item will be added to.
     * @property removedItems what was just removed while the removal can still be undone; empty if there is nothing to undo.
     */
    @Immutable
    data class Success(
        val sections: List<ShoppingSection>,
        val draftName: String = "",
        val draftCategory: ShoppingCategory = ShoppingCategory.Other,
        val removedItems: List<ShoppingItem> = emptyList(),
    ) : ShoppingUiState {
        val totalCount: Int = sections.sumOf { section -> section.items.size }
        val checkedCount: Int = sections.sumOf { section -> section.items.count { item -> item.checked } }
        val pendingCount: Int = totalCount - checkedCount
    }
}

/** Groups [this] by category, in the order of the categories, keeping the order of the list inside each one with the ticked items last. */
internal fun List<ShoppingItem>.groupedByCategory(): List<ShoppingSection> = ShoppingCategory.entries.mapNotNull { category ->
    val inCategory = filter { item -> item.category == category }
    if (inCategory.isEmpty()) null else ShoppingSection(category, inCategory.sortedBy { item -> item.checked })
}
