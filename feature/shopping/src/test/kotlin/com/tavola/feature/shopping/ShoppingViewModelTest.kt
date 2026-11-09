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
    fun `adding trims the name, empties the draft and says what was added`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("  Pan de payés ")
            awaitSuccess { state -> state.draft.name == "  Pan de payés " }
            viewModel.onAddClick()

            val state = awaitSuccess { loaded -> loaded.totalCount == 1 }
            assertEquals("Pan de payés", state.sections.single().items.single().name)
            assertEquals("", state.draft.name)
            assertEquals("Pan de payés", state.draft.addedName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the category is guessed from the name when none was picked`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("Leche")
            val typed = awaitSuccess { state -> state.draft.name == "Leche" }
            assertEquals(ShoppingCategory.DairyAndEggs, typed.draft.category)
            assertTrue(typed.draft.isSuggested)

            viewModel.onAddClick()
            val added = awaitSuccess { loaded -> loaded.totalCount == 1 }
            assertEquals(ShoppingCategory.DairyAndEggs, added.sections.single().category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a name that is not known goes to other and is not marked as suggested`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("Cosa rara")

            val typed = awaitSuccess { state -> state.draft.name == "Cosa rara" }
            assertEquals(ShoppingCategory.Other, typed.draft.category)
            assertEquals(false, typed.draft.isSuggested)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a picked category wins over the guess and is forgotten once the item is added`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("Leche")
            viewModel.onDraftCategoryPick(ShoppingCategory.Household)
            val picked = awaitSuccess { state -> state.draft.pickedCategory != null }
            assertEquals(ShoppingCategory.Household, picked.draft.category)
            assertEquals(false, picked.draft.isSuggested)

            viewModel.onAddClick()
            val added = awaitSuccess { loaded -> loaded.totalCount == 1 }
            assertEquals(ShoppingCategory.Household, added.sections.single().category)
            assertEquals(null, added.draft.pickedCategory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the quantity is saved with the item and trimmed`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("Tomates")
            viewModel.onDraftQuantityChange(" 2 kg ")
            awaitSuccess { state -> state.draft.quantity == " 2 kg " }
            viewModel.onAddClick()

            val state = awaitSuccess { loaded -> loaded.totalCount == 1 }
            assertEquals("2 kg", state.sections.single().items.single().quantity)
            assertEquals("", state.draft.quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding with a blank name does nothing`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("   ")
            viewModel.onAddClick()

            val state = awaitSuccess { loaded -> loaded.draft.name == "   " }
            assertEquals(0, state.totalCount)
            assertEquals(false, state.draft.canAdd)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `typing again clears the added notice`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onDraftNameChange("Pan")
            viewModel.onAddClick()
            awaitSuccess { state -> state.draft.addedName == "Pan" }

            viewModel.onDraftNameChange("L")

            assertEquals(null, awaitSuccess { state -> state.draft.name == "L" }.draft.addedName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `closing the sheet drops what was typed`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onDraftNameChange("Leche")
            viewModel.onDraftQuantityChange("2")
            viewModel.onDraftCategoryPick(ShoppingCategory.Drinks)
            awaitSuccess { state -> state.draft.pickedCategory != null && state.draft.quantity == "2" }

            viewModel.onAddSheetDismiss()

            assertEquals(AddItemDraft(), awaitSuccess { state -> state.draft.name.isEmpty() }.draft)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a name and a quantity longer than an item can have are cut as they are typed`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onDraftNameChange("a".repeat(ShoppingItem.MAX_NAME_LENGTH + 20))
            viewModel.onDraftQuantityChange("b".repeat(ShoppingItem.MAX_QUANTITY_LENGTH + 20))

            val state = awaitSuccess { loaded -> loaded.draft.name.isNotEmpty() && loaded.draft.quantity.isNotEmpty() }
            assertEquals(ShoppingItem.MAX_NAME_LENGTH, state.draft.name.length)
            assertEquals(ShoppingItem.MAX_QUANTITY_LENGTH, state.draft.quantity.length)
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
    fun `removing a second item keeps the first one's undo`() = runTest {
        val viewModel = viewModel(milk, tomatoes)
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onItemRemove("milk")
            awaitSuccess { state -> state.removedItems.size == 1 }
            viewModel.onItemRemove("tomatoes")
            val state = awaitSuccess { loaded -> loaded.removedItems.size == 2 }
            assertEquals(setOf("milk", "tomatoes"), state.removedItems.map { item -> item.id }.toSet())

            viewModel.onRemoveUndo()
            assertEquals(2, awaitSuccess { loaded -> loaded.removedItems.isEmpty() && loaded.totalCount == 2 }.totalCount)
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
