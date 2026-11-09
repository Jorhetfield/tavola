package com.tavola.core.testing.fake

import com.tavola.domain.model.ShoppingCatalog
import com.tavola.domain.model.ShoppingHistory
import com.tavola.domain.model.ShoppingHistoryEntry
import com.tavola.domain.model.ShoppingItem
import com.tavola.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [ShoppingRepository] for tests. Starts with [initialItems] and [initialHistory]; new items get an increasing creation time.
 * The history is stamped with [now], which a test moves to make time pass (for "Bought recently").
 */
class FakeShoppingRepository(initialItems: List<ShoppingItem> = emptyList(), initialHistory: List<ShoppingHistoryEntry> = emptyList()) :
    ShoppingRepository {
    private val items = MutableStateFlow(initialItems.sorted())
    private val history = MutableStateFlow(initialHistory.associateBy { entry -> entry.id })
    private var nextId = 0

    /** The time the fake's clock shows, in milliseconds since the epoch. */
    var now: Long = 0

    override fun observeItems(): Flow<List<ShoppingItem>> = items

    override suspend fun saveItem(item: ShoppingItem): String {
        val clean = item.normalized()
        val isNew = clean.id.isBlank()
        val saved = if (isNew) clean.copy(id = "fake-item-$nextId", createdAt = nextId.toLong()).also { nextId++ } else clean
        items.update { stored -> (stored.filterNot { existing -> existing.id == saved.id } + saved).sorted() }
        if (isNew) recordUse(saved)
        return saved.id
    }

    override suspend fun addItems(items: List<ShoppingItem>) {
        items.forEach { item -> saveItem(item.copy(id = "")) }
    }

    override fun observeHistory(): Flow<List<ShoppingHistoryEntry>> = history.map { entries ->
        entries.values.sortedWith(
            compareByDescending<ShoppingHistoryEntry> { entry -> entry.lastUsedAt }
                .thenByDescending { entry -> entry.useCount }
                .thenBy { entry -> entry.name.lowercase() },
        )
    }

    override suspend fun buyItem(item: ShoppingItem) {
        require(item.id.isNotBlank()) { "Only an item of the list can be bought" }
        val clean = item.normalized()
        items.update { stored -> stored.filterNot { existing -> existing.id == clean.id } }
        val key = ShoppingHistory.keyFor(clean.name)
        history.update { entries ->
            val entry = entries[key] ?: ShoppingHistoryEntry(id = key, name = clean.name)
            entries + (key to entry.copy(name = clean.name, category = clean.category, lastBoughtAt = now, hidden = false))
        }
    }

    override suspend fun forgetHistoryEntry(entry: ShoppingHistoryEntry) {
        history.update { entries ->
            if (ShoppingCatalog.find(entry.name) !=
                null
            ) {
                entries + (entry.id to (entries[entry.id] ?: entry).copy(hidden = true))
            } else {
                entries - entry.id
            }
        }
    }

    override suspend fun setChecked(id: String, checked: Boolean) {
        items.update { stored -> stored.map { item -> if (item.id == id) item.copy(checked = checked) else item } }
    }

    override suspend fun removeItems(ids: List<String>) {
        items.update { stored -> stored.filterNot { item -> item.id in ids } }
    }

    private fun recordUse(item: ShoppingItem) {
        val key = ShoppingHistory.keyFor(item.name)
        history.update { entries ->
            val entry = entries[key] ?: ShoppingHistoryEntry(id = key, name = item.name)
            val used = entry.copy(
                name = item.name,
                category = item.category,
                useCount = entry.useCount + 1,
                lastUsedAt = now,
                lastBoughtAt = null,
                hidden = false,
            )
            entries + (key to used)
        }
    }

    private fun List<ShoppingItem>.sorted() = sortedWith(compareBy({ item -> item.createdAt }, { item -> item.id }))
}
