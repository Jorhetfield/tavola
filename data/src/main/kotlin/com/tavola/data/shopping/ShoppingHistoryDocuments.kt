package com.tavola.data.shopping

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingHistoryEntry
import com.tavola.domain.model.ShoppingItem

// How the history of a group is stored in Firestore: groups/{groupId}/history/{key}, one document per product, where the key is
// `ShoppingHistory.keyFor(name)`. The security rules in `firestore.rules` depend on these names and shapes.
//
//   { name, category, useCount?, lastUsedAt? (a Firestore timestamp), lastBoughtAt? (a timestamp or null), hidden? }
//
// The history is read ordered by `lastUsedAt`, so an entry without it (a forgotten one that was never used) is never read.
//
// Every write is a merge that carries only what it changes, so a document may lack any of the optional fields (the history of an item
// that was added before the history existed has no `useCount`): reading them fills in the defaults.

internal object HistoryFields {
    const val NAME = "name"
    const val CATEGORY = "category"
    const val USE_COUNT = "useCount"
    const val LAST_USED_AT = "lastUsedAt"
    const val LAST_BOUGHT_AT = "lastBoughtAt"
    const val HIDDEN = "hidden"
}

internal const val HISTORY_COLLECTION = "history"

/**
 * What adding this item changes in its history entry: one more use, now (the server's time), not bought any more, not hidden. The
 * name and the category are those of the latest use.
 */
internal fun ShoppingItem.toUseDocument(): Map<String, Any?> = mapOf(
    HistoryFields.NAME to name,
    HistoryFields.CATEGORY to category.name,
    HistoryFields.USE_COUNT to FieldValue.increment(1),
    HistoryFields.LAST_USED_AT to FieldValue.serverTimestamp(),
    HistoryFields.LAST_BOUGHT_AT to null,
    HistoryFields.HIDDEN to false,
)

/**
 * What buying this item changes in its history entry: it was bought and used now (an entry always has a `lastUsedAt`, which is what
 * the history is read by, also for an item that was added before the history existed), and it is not hidden. The uses stay as they are.
 */
internal fun ShoppingItem.toBoughtDocument(): Map<String, Any?> = mapOf(
    HistoryFields.NAME to name,
    HistoryFields.CATEGORY to category.name,
    HistoryFields.LAST_USED_AT to FieldValue.serverTimestamp(),
    HistoryFields.LAST_BOUGHT_AT to FieldValue.serverTimestamp(),
    HistoryFields.HIDDEN to false,
)

/** What forgetting this entry changes: it is hidden. The name and the category go along so the write is a valid document on its own. */
internal fun ShoppingHistoryEntry.toHiddenDocument(): Map<String, Any?> = mapOf(
    HistoryFields.NAME to name,
    HistoryFields.CATEGORY to category.name,
    HistoryFields.HIDDEN to true,
)

/** The entry stored in a document, or `null` if it has no name. Missing fields are read as never used, never bought and not hidden. */
internal fun Map<String, Any?>.toHistoryEntry(id: String): ShoppingHistoryEntry? {
    val name = this[HistoryFields.NAME] as? String ?: return null
    val categoryName = this[HistoryFields.CATEGORY] as? String
    return ShoppingHistoryEntry(
        id = id,
        name = name,
        category = ShoppingCategory.fromName(categoryName),
        useCount = (this[HistoryFields.USE_COUNT] as? Number)?.toInt() ?: 0,
        lastUsedAt = (this[HistoryFields.LAST_USED_AT] as? Timestamp)?.toDate()?.time ?: 0,
        lastBoughtAt = (this[HistoryFields.LAST_BOUGHT_AT] as? Timestamp)?.toDate()?.time,
        hidden = this[HistoryFields.HIDDEN] as? Boolean ?: false,
    )
}
