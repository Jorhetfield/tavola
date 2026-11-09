package com.tavola.domain.repository

import com.tavola.domain.model.ShoppingHistoryEntry
import com.tavola.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

/** The shopping list of the group of the signed-in person, shared by its members. The real implementation (Firestore) lives in `:data`. */
interface ShoppingRepository {
    /** Every item, oldest first, updated each time one changes. Empty while the person has no group. */
    fun observeItems(): Flow<List<ShoppingItem>>

    /**
     * Creates or replaces [item]. If its id is blank a new id is generated and the item is stamped with the current time and with
     * whoever adds it, and the group's history counts one more use of it. Saving an item that was removed brings it back in its
     * place, which is how undoing a removal works (it is not counted again). Returns the id it was saved under.
     */
    suspend fun saveItem(item: ShoppingItem): String

    /**
     * Adds every one of [items] as a new item, in a single write: they show up together, share the time they were added and are
     * all added or none. Their ids and times are ignored. Waits once for the server, however many there are. Each one counts as a
     * use in the history, like a new item saved with [saveItem].
     */
    suspend fun addItems(items: List<ShoppingItem>)

    /** Every entry of the history of the group, the most recently used first, updated each time one changes. Empty with no group. */
    fun observeHistory(): Flow<List<ShoppingHistoryEntry>>

    /**
     * Buys [item]: it leaves the list and the history remembers it was bought now, which is what "Bought recently" shows. Both
     * happen in a single write. An item that is not on the list any more is still remembered as bought.
     */
    suspend fun buyItem(item: ShoppingItem)

    /**
     * Forgets [entry]: one of a product of the catalog is only hidden from the suggestions (the catalog itself is always there), and
     * any other one is deleted. Using the product again brings it back.
     */
    suspend fun forgetHistoryEntry(entry: ShoppingHistoryEntry)

    /** Ticks or unticks the item [id]; does nothing if it does not exist. */
    suspend fun setChecked(id: String, checked: Boolean)

    /** Removes the items [ids] in one go; the ones that do not exist are ignored. */
    suspend fun removeItems(ids: List<String>)
}
