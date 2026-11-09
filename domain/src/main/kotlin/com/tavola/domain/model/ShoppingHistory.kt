package com.tavola.domain.model

/**
 * Something the group has put on its shopping list before, kept so it can be added again with one tap. [id] is the key of the
 * product (see [ShoppingHistory.keyFor]), so the same product is always the same entry however it is written.
 *
 * [useCount] is how many times it was added, [lastUsedAt] when it was last added or bought, and [lastBoughtAt] when it was last
 * bought (`null` once it is added again); all the times are milliseconds since the epoch from the server's clock. A [hidden] entry
 * was forgotten: it stays out of the suggestions until it is used again.
 */
data class ShoppingHistoryEntry(
    val id: String,
    val name: String,
    val category: ShoppingCategory = ShoppingCategory.Other,
    val useCount: Int = 0,
    val lastUsedAt: Long = 0,
    val lastBoughtAt: Long? = null,
    val hidden: Boolean = false,
) {
    /** Whether this entry is the one of a product of the catalog (its key is the id of the product), which is never deleted. */
    val isFromCatalog: Boolean get() = ShoppingCatalog.byId(id) != null
}

object ShoppingHistory {
    /** How long a bought item stays in "Bought recently", and how many of them are shown. */
    const val RECENTLY_BOUGHT_WINDOW_MILLIS = 24 * 60 * 60 * 1000L
    const val MAX_RECENTLY_BOUGHT = 12

    /** The security rules cap the id of an entry at this many bytes (UTF-8), which is the strictest way they could count it. */
    const val MAX_KEY_BYTES = 100

    /** The most recently used first, then the most used, then by name: the order the history is offered in. */
    val mostRecentFirst: Comparator<ShoppingHistoryEntry> = compareByDescending<ShoppingHistoryEntry> { entry -> entry.lastUsedAt }
        .thenByDescending { entry -> entry.useCount }
        .thenBy { entry -> entry.name.lowercase() }

    /**
     * The id of the history entry of a product called [name]: the id of its catalog product when it is one ("Tomates" and "tomato"
     * are both `tomato`), or else its words and digits, so "Pollo asado" and "pollo  asado" are the same entry but "Vitamina B6"
     * and "Vitamina B12" are not. A name with no letters or digits at all (only symbols) still gets a key, from the text itself.
     */
    fun keyFor(name: String): String {
        ShoppingCatalog.find(name)?.let { product -> return product.id }
        val key = ShoppingNames.words(name).joinToString("-").cutToBytes(MAX_KEY_BYTES).trim('-')
        return key.ifEmpty { "other-${name.trim().lowercase().hashCode().toUInt()}" }
    }

    /**
     * What was bought in the last [RECENTLY_BOUGHT_WINDOW_MILLIS] before [now] and is not on the list ([items]) any more, the last
     * bought first and at most [MAX_RECENTLY_BOUGHT]. Forgotten entries are left out. [now] is the phone's clock while the times of
     * the entries come from the server's, so a phone a few hours off shifts the window by that much.
     */
    fun recentlyBought(entries: List<ShoppingHistoryEntry>, items: List<ShoppingItem>, now: Long): List<ShoppingHistoryEntry> {
        val onTheList = items.map { item -> ShoppingNames.key(item.name) }.filter { key -> key.isNotEmpty() }.toSet()
        return entries
            .filter { entry -> !entry.hidden && entry.lastBoughtAt != null && now - entry.lastBoughtAt <= RECENTLY_BOUGHT_WINDOW_MILLIS }
            .filter { entry -> !entry.isOn(onTheList) }
            .sortedByDescending { entry -> entry.lastBoughtAt }
            .take(MAX_RECENTLY_BOUGHT)
    }

    /** Whether one of the keys of [itemKeys] names the same thing as the entry, by the rules of [ShoppingNames.same]. */
    private fun ShoppingHistoryEntry.isOn(itemKeys: Set<String>): Boolean {
        val key = ShoppingNames.key(name)
        return key.isNotEmpty() && ShoppingNames.spellings(key).any { spelling -> spelling in itemKeys }
    }

    private fun String.cutToBytes(maxBytes: Int): String {
        var text = this
        while (text.toByteArray(Charsets.UTF_8).size > maxBytes) text = text.dropLast(1)
        return text
    }
}
