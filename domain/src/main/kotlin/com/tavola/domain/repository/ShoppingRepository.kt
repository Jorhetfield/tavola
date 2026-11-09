package com.tavola.domain.repository

import com.tavola.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

/** The shopping list of the group of the signed-in person, shared by its members. The real implementation (Firestore) lives in `:data`. */
interface ShoppingRepository {
    /** Every item, oldest first, updated each time one changes. Empty while the person has no group. */
    fun observeItems(): Flow<List<ShoppingItem>>

    /**
     * Creates or replaces [item]. If its id is blank a new id is generated and the item is stamped with the current time and with
     * whoever adds it. Saving an item that was removed brings it back in its place, which is how undoing a removal works.
     * Returns the id it was saved under.
     */
    suspend fun saveItem(item: ShoppingItem): String

    /**
     * Adds every one of [items] as a new item, in a single write: they show up together, share the time they were added and are
     * all added or none. Their ids and times are ignored. Waits once for the server, however many there are.
     */
    suspend fun addItems(items: List<ShoppingItem>)

    /** Ticks or unticks the item [id]; does nothing if it does not exist. */
    suspend fun setChecked(id: String, checked: Boolean)

    /** Removes the items [ids] in one go; the ones that do not exist are ignored. */
    suspend fun removeItems(ids: List<String>)
}
