package com.tavola.feature.shopping

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.tavola.core.testing.fake.FakeShoppingRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.ShoppingCandidate
import com.tavola.domain.model.ShoppingCategory
import com.tavola.domain.model.ShoppingItem
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class AddToShoppingViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val rice = ShoppingCandidate("Arroz bomba", "400 g")
    private val stock = ShoppingCandidate("Caldo de pollo", "1,2 l")
    private val salt = ShoppingCandidate("Sal")

    private fun viewModel(
        repository: FakeShoppingRepository = FakeShoppingRepository(),
        offered: List<ShoppingCandidate> = listOf(rice, stock, salt),
    ) = AddToShoppingViewModel(repository).also { viewModel -> viewModel.onCandidates(offered) }

    private suspend fun ReceiveTurbine<AddToShoppingUiState>.awaitSuccess(
        until: (AddToShoppingUiState.Success) -> Boolean = { true },
    ): AddToShoppingUiState.Success {
        while (true) {
            val state = awaitItem()
            if (state is AddToShoppingUiState.Success && until(state) && state.rows.isNotEmpty()) return state
        }
    }

    @Test
    fun `everything that is not on the list starts picked`() = runTest {
        viewModel().uiState.test {
            val state = awaitSuccess()

            assertEquals(3, state.selectedCount)
            assertTrue(state.rows.all { row -> row.selected && !row.onList })
            assertTrue(state.allSelected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a thing already on the list cannot be picked, whatever its case, accents or plural`() = runTest {
        val repository = FakeShoppingRepository(listOf(ShoppingItem("1", "arroz  BOMBA", ShoppingCategory.Pantry, createdAt = 1)))
        viewModel(repository).uiState.test {
            val state = awaitSuccess()

            val onList = state.rows.single { row -> row.onList }
            assertEquals("Arroz bomba", onList.candidate.name)
            assertFalse(onList.selected)
            assertEquals(2, state.selectedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `an item that is already ticked off does not count as being on the list`() = runTest {
        val repository = FakeShoppingRepository(listOf(ShoppingItem("1", "Sal", checked = true, createdAt = 1)))
        viewModel(repository).uiState.test {
            val state = awaitSuccess()

            assertFalse(state.rows.single { row -> row.candidate.name == "Sal" }.onList)
            assertEquals(3, state.selectedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unpicking and picking a row changes what will be added`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onToggle(index = 1, selected = false)
            val unpicked = awaitSuccess { state -> state.selectedCount == 2 }
            assertFalse(unpicked.rows[1].selected)
            assertFalse(unpicked.allSelected)

            viewModel.onToggle(index = 1, selected = true)
            assertEquals(3, awaitSuccess { state -> state.selectedCount == 3 }.selectedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `select all turns into select none and back`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()

            viewModel.onSelectAllToggle()
            assertEquals(0, awaitSuccess { state -> state.selectedCount == 0 }.selectedCount)

            viewModel.onSelectAllToggle()
            assertEquals(3, awaitSuccess { state -> state.selectedCount == 3 }.selectedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding saves only what is picked, with its quantity and a guessed category, and says how many`() = runTest {
        val repository = FakeShoppingRepository()
        val viewModel = viewModel(repository, listOf(rice, ShoppingCandidate("Leche", "1 l"), salt))
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onToggle(index = 2, selected = false)
            awaitSuccess { state -> state.selectedCount == 2 }

            var added = -1
            viewModel.onAddClick { count -> added = count }

            // What was added now shows as already on the list, and the one that was not picked is still free.
            val after = awaitSuccess { state -> state.rows.count { row -> row.onList } == 2 }
            assertEquals(listOf(true, true, false), after.rows.map { row -> row.onList })
            assertEquals(2, added)
            repository.observeItems().test {
                val saved = awaitItem()
                assertEquals(listOf("Arroz bomba", "Leche"), saved.map { item -> item.name })
                assertEquals(listOf("400 g", "1 l"), saved.map { item -> item.quantity })
                assertEquals(ShoppingCategory.Pantry, saved[0].category)
                assertEquals(ShoppingCategory.DairyAndEggs, saved[1].category)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `adding with nothing picked does nothing`() = runTest {
        val repository = FakeShoppingRepository()
        val viewModel = viewModel(repository)
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onSelectAllToggle()
            awaitSuccess { state -> state.selectedCount == 0 }

            var called = false
            viewModel.onAddClick { called = true }

            assertFalse(called)
            repository.observeItems().test {
                assertTrue(awaitItem().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a name that comes twice in the offer is offered once`() = runTest {
        val viewModel =
            viewModel(
                offered = listOf(rice, ShoppingCandidate("Aceite de oliva", "2 cdas"), ShoppingCandidate("aceite de OLIVA", "1 l"), salt),
            )
        viewModel.uiState.test {
            val state = awaitSuccess()

            assertEquals(listOf("Arroz bomba", "Aceite de oliva", "Sal"), state.rows.map { row -> row.candidate.name })
            assertEquals("2 cdas", state.rows[1].candidate.quantity)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `with nothing offered yet it is loading, not an empty picker`() = runTest {
        val viewModel = viewModel(offered = emptyList())
        viewModel.uiState.test {
            assertEquals(AddToShoppingUiState.Loading, awaitItem())

            viewModel.onCandidates(listOf(rice))
            assertEquals(1, awaitSuccess().rows.size)

            viewModel.onCandidates(emptyList())
            while (awaitItem() !is AddToShoppingUiState.Loading) Unit
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a new sheet starts with everything picked again`() = runTest {
        val viewModel = viewModel()
        viewModel.uiState.test {
            awaitSuccess()
            viewModel.onToggle(index = 0, selected = false)
            awaitSuccess { state -> state.selectedCount == 2 }

            viewModel.onCandidates(listOf(rice, stock, salt))

            assertEquals(3, awaitSuccess { state -> state.selectedCount == 3 }.selectedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
