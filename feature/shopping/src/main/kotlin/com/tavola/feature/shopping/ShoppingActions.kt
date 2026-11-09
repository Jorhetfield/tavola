package com.tavola.feature.shopping

import com.tavola.domain.model.ShoppingCategory

/** Everything the shopping list can ask of its ViewModel. Does nothing by default (previews and screenshots). */
data class ShoppingActions(
    val onDraftNameChange: (text: String) -> Unit = {},
    val onDraftQuantityChange: (text: String) -> Unit = {},
    val onDraftCategoryPick: (category: ShoppingCategory) -> Unit = {},
    val onAddClick: () -> Unit = {},
    val onAddSheetDismiss: () -> Unit = {},
    val onItemCheckedChange: (itemId: String, checked: Boolean) -> Unit = { _, _ -> },
    val onItemRemove: (itemId: String) -> Unit = {},
    val onRemoveCheckedClick: () -> Unit = {},
    val onRemoveUndo: () -> Unit = {},
    val onRemovedShown: () -> Unit = {},
    val onRetryClick: () -> Unit = {},
)
