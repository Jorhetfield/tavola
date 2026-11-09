package com.tavola.data.shopping

import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem

// How the shopping list of a group is stored in Firestore: groups/{groupId}/shopping/{itemId}, one document per item so two people
// adding at once never overwrite each other. The security rules in `firestore.rules` depend on these names and shapes.
//
//   { name, category, quantity?, checked, createdAt, addedBy? }

internal object ShoppingFields {
    const val NAME = "name"
    const val CATEGORY = "category"
    const val QUANTITY = "quantity"
    const val CHECKED = "checked"
    const val CREATED_AT = "createdAt"

    /** Who added the item. It is written when the item is created and left alone by later writes. */
    const val ADDED_BY = "addedBy"
}

internal const val SHOPPING_COLLECTION = "shopping"

/**
 * The document of this item. [addedBy] is only included when it is known, so a write that merges it into an existing document
 * leaves whoever added the item as they were. The category is stored by name so reordering the enum never corrupts data.
 */
internal fun ShoppingItem.toDocument(addedBy: String?): Map<String, Any?> = buildMap {
    put(ShoppingFields.NAME, name)
    put(ShoppingFields.CATEGORY, category.name)
    put(ShoppingFields.QUANTITY, quantity)
    put(ShoppingFields.CHECKED, checked)
    put(ShoppingFields.CREATED_AT, createdAt)
    if (addedBy != null) put(ShoppingFields.ADDED_BY, addedBy)
}

/**
 * The item stored in a document, or `null` if it has no name. A missing `checked` is read as pending, and an unknown category
 * (from a newer app version, say) falls back to [ShoppingCategory.Other].
 */
internal fun Map<String, Any?>.toShoppingItem(id: String): ShoppingItem? {
    val name = this[ShoppingFields.NAME] as? String ?: return null
    val categoryName = this[ShoppingFields.CATEGORY] as? String
    return ShoppingItem(
        id = id,
        name = name,
        category = ShoppingCategory.entries.firstOrNull { category -> category.name == categoryName } ?: ShoppingCategory.Other,
        quantity = this[ShoppingFields.QUANTITY] as? String,
        checked = this[ShoppingFields.CHECKED] as? Boolean ?: false,
        createdAt = (this[ShoppingFields.CREATED_AT] as? Number)?.toLong() ?: 0,
        addedBy = this[ShoppingFields.ADDED_BY] as? String,
    )
}
