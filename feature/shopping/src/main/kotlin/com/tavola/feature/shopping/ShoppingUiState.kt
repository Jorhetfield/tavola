package com.tavola.feature.shopping

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingCategorySuggester
import com.tavola.domain.model.ShoppingItem

/** The items of one [category], with the ones still to buy first and the ticked ones after them. */
@Immutable
data class ShoppingSection(val category: ShoppingCategory, val items: List<ShoppingItem>)

/**
 * What is typed in the sheet that adds items. The category is the one the person [pickedCategory] or, if they did not pick one, the
 * one [ShoppingCategorySuggester] guesses from the name, or [ShoppingCategory.Other]. [addedName] is the item that was just added
 * (the sheet stays open for the next one) until something new is typed.
 */
@Immutable
data class AddItemDraft(
    val name: String = "",
    val quantity: String = "",
    val pickedCategory: ShoppingCategory? = null,
    val addedName: String? = null,
) {
    private val suggestedCategory: ShoppingCategory? = if (pickedCategory == null) ShoppingCategorySuggester.suggest(name) else null

    val category: ShoppingCategory = pickedCategory ?: suggestedCategory ?: ShoppingCategory.Other

    /** Whether [category] is a guess from the name (so the sheet can say so) and not what the person picked or the default. */
    val isSuggested: Boolean = suggestedCategory != null

    val canAdd: Boolean = name.isNotBlank()
}

sealed interface ShoppingUiState {
    data object Loading : ShoppingUiState

    data object Error : ShoppingUiState

    /**
     * @property sections the list grouped by category, in the order of the categories; a category with no items is left out.
     * @property draft what is typed in the sheet that adds items.
     * @property removedItems what was just removed while the removal can still be undone; empty if there is nothing to undo.
     */
    @Immutable
    data class Success(
        val sections: List<ShoppingSection>,
        val draft: AddItemDraft = AddItemDraft(),
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
