package com.tavola.feature.shopping

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.ShoppingCandidate

/**
 * One thing a feature offers to put on the list. [onList] is set when the list already has an item with that name still to buy: such a
 * row cannot be picked, so nothing is added twice. [selected] is what will be added.
 */
@Immutable
data class AddToShoppingRow(val index: Int, val candidate: ShoppingCandidate, val onList: Boolean, val selected: Boolean)

sealed interface AddToShoppingUiState {
    data object Loading : AddToShoppingUiState

    data object Error : AddToShoppingUiState

    /** @property saving the chosen things are being added, so the button waits. */
    @Immutable
    data class Success(val rows: List<AddToShoppingRow>, val saving: Boolean = false) : AddToShoppingUiState {
        private val pickable = rows.filter { row -> !row.onList }

        val selectedCount: Int = rows.count { row -> row.selected }

        /** Whether every row that can be picked is picked, which is what "select all" turns into "select none". */
        val allSelected: Boolean = pickable.isNotEmpty() && pickable.all { row -> row.selected }

        val canSelectAny: Boolean = pickable.isNotEmpty()
    }
}
