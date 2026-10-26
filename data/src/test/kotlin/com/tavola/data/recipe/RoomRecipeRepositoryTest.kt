package com.tavola.data.recipe

import app.cash.turbine.test
import com.tavola.domain.model.Recipe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RoomRecipeRepositoryTest {
    private val repository = RoomRecipeRepository(FakeRecipeDao())
    private val gazpacho = Recipe(id = "gazpacho", title = "Gazpacho", prepTimeMinutes = 15)

    @Test
    fun `an empty recipe book emits an empty list`() = runTest {
        repository.observeRecipes().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving a recipe with an id keeps that id and shows it sorted by title ignoring case`() = runTest {
        repository.observeRecipes().test {
            awaitItem()

            assertEquals("gazpacho", repository.saveRecipe(gazpacho))
            awaitItem()
            repository.saveRecipe(Recipe(id = "arroz", title = "arroz negro"))

            assertEquals(listOf("arroz negro", "Gazpacho"), awaitItem().map { recipe -> recipe.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving a recipe without an id generates a unique one`() = runTest {
        val first = repository.saveRecipe(Recipe(id = "", title = "Una"))
        val second = repository.saveRecipe(Recipe(id = "  ", title = "Otra"))

        assertTrue(first.isNotBlank() && second.isNotBlank())
        assertNotEquals(first, second)
        repository.observeRecipe(first).test {
            assertEquals("Una", awaitItem()?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving an existing id replaces the recipe`() = runTest {
        repository.saveRecipe(gazpacho)

        repository.observeRecipe("gazpacho").test {
            assertEquals(15, awaitItem()?.prepTimeMinutes)

            repository.saveRecipe(gazpacho.copy(title = "Gazpacho andaluz", prepTimeMinutes = 20))

            val updated = awaitItem()
            assertEquals("Gazpacho andaluz", updated?.title)
            assertEquals(20, updated?.prepTimeMinutes)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting removes the recipe and saving it again brings it back`() = runTest {
        repository.saveRecipe(gazpacho)

        repository.observeRecipe("gazpacho").test {
            assertEquals(gazpacho, awaitItem())

            repository.deleteRecipe("gazpacho")
            assertNull(awaitItem())

            repository.saveRecipe(gazpacho)
            assertEquals(gazpacho, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleting a recipe that does not exist does nothing`() = runTest {
        repository.saveRecipe(gazpacho)

        repository.deleteRecipe("missing")

        repository.observeRecipes().test {
            assertEquals(listOf(gazpacho), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
