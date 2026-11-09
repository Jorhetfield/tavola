package com.tavola.core.testing.fake

import app.cash.turbine.test
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
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
}
