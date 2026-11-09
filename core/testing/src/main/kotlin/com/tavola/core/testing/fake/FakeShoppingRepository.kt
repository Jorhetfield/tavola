package com.tavola.core.testing.fake

import com.tavola.domain.model.ShoppingItem
import com.tavola.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [ShoppingRepository] for tests. Starts with [initialItems]; new items get an increasing creation time. */
class FakeShoppingRepository(initialItems: List<ShoppingItem> = emptyList()) : ShoppingRepository {
    private val items = MutableStateFlow(initialItems.sorted())
    private var nextId = 0

    override fun observeItems(): Flow<List<ShoppingItem>> = items

    override suspend fun saveItem(item: ShoppingItem): String {
        val saved = if (item.id.isBlank()) item.copy(id = "fake-item-$nextId", createdAt = nextId.toLong()).also { nextId++ } else item
        items.update { stored -> (stored.filterNot { existing -> existing.id == saved.id } + saved).sorted() }
        return saved.id
    }

    override suspend fun setChecked(id: String, checked: Boolean) {
        items.update { stored -> stored.map { item -> if (item.id == id) item.copy(checked = checked) else item } }
    }

    override suspend fun removeItems(ids: List<String>) {
        items.update { stored -> stored.filterNot { item -> item.id in ids } }
    }

    private fun List<ShoppingItem>.sorted() = sortedWith(compareBy({ item -> item.createdAt }, { item -> item.id }))
}
