package com.tavola.feature.shopping

/** Everything the "add to the shopping list" sheet can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class AddToShoppingActions(
    val onToggle: (index: Int, selected: Boolean) -> Unit = { _, _ -> },
    val onSelectAllToggle: () -> Unit = {},
    val onAddClick: () -> Unit = {},
    val onRetryClick: () -> Unit = {},
)
