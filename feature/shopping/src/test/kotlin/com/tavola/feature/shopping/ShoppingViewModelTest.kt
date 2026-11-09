package com.tavola.feature.shopping

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.fake.FakeShoppingRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
import com.tavola.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class ShoppingViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val milk = ShoppingItem("milk", "Leche", ShoppingCategory.DairyAndEggs, createdAt = 1)
    private val tomatoes = ShoppingItem("tomatoes", "Tomates", ShoppingCategory.FruitAndVegetables, "1 kg", createdAt = 2)
    private val peppers = ShoppingItem("peppers", "Pimientos", ShoppingCategory.FruitAndVegetables, checked = true, createdAt = 3)

    private fun viewModel(vararg items: ShoppingItem) = ShoppingViewModel(FakeShoppingRepository(items.toList()))

    private suspend fun ReceiveTurbine<ShoppingUiState>.awaitSuccess(
        until: (ShoppingUiState.Success) -> Boolean = { true },
    ): ShoppingUiState.Success {
        while (true) {
            val state = awaitItem()
            if (state is ShoppingUiState.Success && until(state)) return state
        }
    }

    @Test
    fun `the list is grouped by category in the order of the categories`() = runTest {
        viewModel(milk, tomatoes).uiState.test {
            val state = awaitSuccess()

            assertEquals(
                listOf(ShoppingCategory.FruitAndVegetables, ShoppingCategory.DairyAndEggs),
                state.sections.map { section ->
                    section.category
                },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `inside a category the ticked items come last`() = runTest {
        viewModel(peppers, tomatoes).uiState.test {
            val state = awaitSuccess()

            assertEquals(listOf("Tomates", "Pimientos"), state.sections.single().items.map { item -> item.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `it counts the items, the ticked ones and the ones still to buy`() = runTest {
        viewModel(milk, tomatoes, peppers).uiState.test {
            val state = awaitSuccess()

            assertEquals(3, state.totalCount)
            assertEquals(1, state.checkedCount)
            assertEquals(2, state.pendingCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an empty list is a success with no sections`() = runTest {
        viewModel().uiState.test {
            assertTrue(awaitSuccess().sections.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding trims the name, uses the chosen category and empties the field but keeps the category`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftCategoryChange(ShoppingCategory.Bakery)
            viewModel.onDraftNameChange("  Pan de payés ")
            awaitSuccess { state -> state.draftName == "  Pan de payés " }
            viewModel.onAddClick()

            val state = awaitSuccess { loaded -> loaded.totalCount == 1 }
            val item = state.sections.single().items.single()
            assertEquals("Pan de payés", item.name)
            assertEquals(ShoppingCategory.Bakery, item.category)
            assertEquals("", state.draftName)
            assertEquals(ShoppingCategory.Bakery, state.draftCategory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding with a blank field does nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("   ")
            viewModel.onAddClick()

            assertEquals(0, awaitSuccess { state -> state.draftName == "   " }.totalCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a name longer than an item can have is cut as it is typed`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("a".repeat(ShoppingItem.MAX_NAME_LENGTH + 20))

            val state = awaitSuccess { loaded -> loaded.draftName.isNotEmpty() }
            assertEquals(ShoppingItem.MAX_NAME_LENGTH, state.draftName.length)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ticking and unticking an item changes it in the list`() = runTest {
        val viewModel = viewModel(milk)
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onItemCheckedChange("milk", true)
            assertEquals(1, awaitSuccess { state -> state.checkedCount == 1 }.checkedCount)

            viewModel.onItemCheckedChange("milk", false)
            assertEquals(0, awaitSuccess { state -> state.checkedCount == 0 }.checkedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing an item takes it out of the list and offers to undo it`() = runTest {
        val viewModel = viewModel(milk, tomatoes)
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onItemRemove("milk")

            val state = awaitSuccess { loaded -> loaded.removedItems.isNotEmpty() }
            assertEquals(listOf(milk), state.removedItems)
            assertEquals(listOf("Tomates"), state.sections.flatMap { section -> section.items }.map { item -> item.name })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `undoing a removal brings the item back in its place`() = runTest {
        val viewModel = viewModel(milk, tomatoes)
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onItemRemove("milk")
            awaitSuccess { state -> state.removedItems.isNotEmpty() }

            viewModel.onRemoveUndo()

            val state = awaitSuccess { loaded -> loaded.removedItems.isEmpty() && loaded.totalCount == 2 }
            assertEquals(
                listOf(ShoppingCategory.FruitAndVegetables, ShoppingCategory.DairyAndEggs),
                state.sections.map { section ->
                    section.category
                },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing the ticked items removes only those and undo brings them all back`() = runTest {
        val extra = ShoppingItem("salt", "Sal", checked = true, createdAt = 4)
        val viewModel = viewModel(milk, peppers, extra)
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onRemoveCheckedClick()

            val removed = awaitSuccess { state -> state.removedItems.isNotEmpty() }
            assertEquals(setOf("peppers", "salt"), removed.removedItems.map { item -> item.id }.toSet())
            assertEquals(1, removed.totalCount)

            viewModel.onRemoveUndo()
            assertEquals(3, awaitSuccess { state -> state.removedItems.isEmpty() && state.totalCount == 3 }.totalCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removing the ticked items with nothing ticked does nothing`() = runTest {
        val viewModel = viewModel(milk)
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onRemoveCheckedClick()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when the removed notice ends there is nothing left to undo`() = runTest {
        val viewModel = viewModel(milk)
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onItemRemove("milk")
            awaitSuccess { state -> state.removedItems.isNotEmpty() }

            viewModel.onRemovedShown()

            assertTrue(awaitSuccess { state -> state.removedItems.isEmpty() }.removedItems.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a repository that fails to load shows the error and retrying loads again`() = runTest {
        var attempts = 0
        val repository = object : ShoppingRepository by FakeShoppingRepository(listOf(milk)) {
            override fun observeItems(): Flow<List<ShoppingItem>> = flow {
                if (attempts++ == 0) error("no connection")
                emit(listOf(milk))
            }
        }
        val viewModel = ShoppingViewModel(repository)
        viewModel.uiState.test {
            while (awaitItem() !is ShoppingUiState.Error) Unit

            viewModel.onRetryClick()

            assertEquals(1, awaitSuccess().totalCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the subtitle counts what is left, says all done, and is empty for an empty list`() {
        val some = ShoppingUiState.Success(listOf(milk, peppers).groupedByCategory())
        val all = ShoppingUiState.Success(listOf(peppers).groupedByCategory())
        val none = ShoppingUiState.Success(emptyList())

        assertEquals("1 pendiente", subtitleOf(some, pending = "1 pendiente", allDone = "Todo"))
        assertEquals("Todo", subtitleOf(all, pending = "0 pendientes", allDone = "Todo"))
        assertEquals(null, subtitleOf(none, pending = "0 pendientes", allDone = "Todo"))
    }
}
