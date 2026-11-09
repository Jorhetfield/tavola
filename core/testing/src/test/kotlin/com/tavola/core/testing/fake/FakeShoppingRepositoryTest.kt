package com.tavola.core.testing.fake

import app.cash.turbine.test
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingHistoryEntry
import com.tavola.domain.model.ShoppingItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FakeShoppingRepositoryTest {
    @Test
    fun `saveItem generates ids and items come out oldest first`() = runTest {
        val repository = FakeShoppingRepository()
        val first = repository.saveItem(ShoppingItem(id = "", name = "Leche", category = ShoppingCategory.DairyAndEggs))
        val second = repository.saveItem(ShoppingItem(id = "", name = "Pan", category = ShoppingCategory.Bakery))
        assertNotEquals(first, second)

        repository.observeItems().test {
            assertEquals(listOf("Leche", "Pan"), awaitItem().map { item -> item.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addItems adds every item as a new one, ignoring the ids it was given`() = runTest {
        val repository = FakeShoppingRepository()

        repository.addItems(listOf(ShoppingItem("same", "Leche"), ShoppingItem("same", "Pan"), ShoppingItem("", "Sal")))

        repository.observeItems().test {
            val items = awaitItem()
            assertEquals(listOf("Leche", "Pan", "Sal"), items.map { item -> item.name })
            assertEquals(3, items.map { item -> item.id }.toSet().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setChecked ticks and unticks an item`() = runTest {
        val repository = FakeShoppingRepository()
        val id = repository.saveItem(ShoppingItem(id = "", name = "Huevos"))

        repository.observeItems().test {
            assertEquals(false, awaitItem().single().checked)

            repository.setChecked(id, true)
            assertTrue(awaitItem().single().checked)

            repository.setChecked(id, false)
            assertEquals(false, awaitItem().single().checked)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeItems removes several at once and saving one back restores it in its place`() = runTest {
        val repository = FakeShoppingRepository()
        val milk = repository.saveItem(ShoppingItem(id = "", name = "Leche"))
        val bread = repository.saveItem(ShoppingItem(id = "", name = "Pan"))
        repository.saveItem(ShoppingItem(id = "", name = "Sal"))

        repository.observeItems().test {
            val all = awaitItem()
            assertEquals(3, all.size)

            repository.removeItems(listOf(milk, bread))
            assertEquals(listOf("Sal"), awaitItem().map { item -> item.name })

            repository.saveItem(all.first { item -> item.id == milk })
            assertEquals(listOf("Leche", "Sal"), awaitItem().map { item -> item.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding an item counts a use in the history, once per add and not when an item is restored`() = runTest {
        val repository = FakeShoppingRepository()
        repository.now = 10
        repository.saveItem(ShoppingItem(id = "", name = "Tomates", category = ShoppingCategory.FruitAndVegetables))
        repository.now = 20
        repository.addItems(listOf(ShoppingItem("", "tomate"), ShoppingItem("", "Sal")))
        val milk = ShoppingItem(id = "gone", name = "Leche")
        repository.saveItem(milk)

        repository.observeHistory().test {
            val entries = awaitItem().associateBy { entry -> entry.id }
            assertEquals(2, entries.getValue("tomato").useCount)
            assertEquals(20, entries.getValue("tomato").lastUsedAt)
            assertEquals("tomate", entries.getValue("tomato").name)
            assertEquals(1, entries.getValue("salt").useCount)
            assertEquals(false, entries.containsKey("leche"), "restoring an item is not a use")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the history comes with the most recently used first`() = runTest {
        val repository = FakeShoppingRepository()
        repository.now = 1
        repository.saveItem(ShoppingItem(id = "", name = "Pan"))
        repository.now = 2
        repository.saveItem(ShoppingItem(id = "", name = "Leche"))

        repository.observeHistory().test {
            assertEquals(listOf("Leche", "Pan"), awaitItem().map { entry -> entry.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `buying an item takes it off the list and notes it as bought, keeping the uses`() = runTest {
        val repository = FakeShoppingRepository()
        repository.now = 5
        val id = repository.saveItem(ShoppingItem(id = "", name = "Leche", category = ShoppingCategory.DairyAndEggs))
        val item = repository.observeItems().first().single { stored -> stored.id == id }
        repository.now = 9

        repository.buyItem(item)

        assertEquals(emptyList<ShoppingItem>(), repository.observeItems().first())
        val entry = repository.observeHistory().first().single()
        assertEquals(9L, entry.lastBoughtAt)
        assertEquals(1, entry.useCount)
        assertEquals(9L, entry.lastUsedAt, "buying also counts as using, so every entry has a time to be read by")
    }

    @Test
    fun `adding a product again clears that it was bought`() = runTest {
        val repository = FakeShoppingRepository()
        val id = repository.saveItem(ShoppingItem(id = "", name = "Leche"))
        repository.buyItem(repository.observeItems().first().single { stored -> stored.id == id })
        assertEquals(0L, repository.observeHistory().first().single().lastBoughtAt)

        repository.saveItem(ShoppingItem(id = "", name = "leche"))

        val entry = repository.observeHistory().first().single()
        assertEquals(null, entry.lastBoughtAt)
        assertEquals(2, entry.useCount)
    }

    @Test
    fun `buying something that was never in the history creates its entry`() = runTest {
        val repository = FakeShoppingRepository(initialItems = listOf(ShoppingItem("old", "Pan")))
        repository.now = 7

        repository.buyItem(ShoppingItem("old", "Pan"))

        val entry = repository.observeHistory().first().single()
        assertEquals("Pan", entry.name)
        assertEquals(7L, entry.lastBoughtAt)
        assertEquals(0, entry.useCount)
    }

    @Test
    fun `forgetting hides a catalog product and deletes any other, and using it again brings it back`() = runTest {
        val repository = FakeShoppingRepository()
        repository.saveItem(ShoppingItem(id = "", name = "Leche"))
        repository.saveItem(ShoppingItem(id = "", name = "Tarta de la abuela"))

        repository.observeHistory().first().forEach { entry -> repository.forgetHistoryEntry(entry) }

        val left: List<ShoppingHistoryEntry> = repository.observeHistory().first()
        assertEquals(listOf("Leche"), left.map { entry -> entry.name })
        assertTrue(left.single().hidden)

        repository.saveItem(ShoppingItem(id = "", name = "Leche"))
        assertEquals(false, repository.observeHistory().first().single().hidden)
    }
}
