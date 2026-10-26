package com.tavola.core.testing.fake

import app.cash.turbine.test
import com.tavola.domain.model.Recipe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class FakeRecipeRepositoryTest {
    @Test
    fun `recipes are emitted sorted by title ignoring case`() = runTest {
        val repository = FakeRecipeRepository(listOf(Recipe("b", "banana"), Recipe("a", "Zumo"), Recipe("c", "Arroz")))

        repository.observeRecipes().test {
            assertEquals(listOf("Arroz", "banana", "Zumo"), awaitItem().map { recipe -> recipe.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveRecipe generates ids, replaces by id and deleteRecipe removes`() = runTest {
        val repository = FakeRecipeRepository(initialRecipes = emptyList())
        val first = repository.saveRecipe(Recipe(id = "", title = "Una"))
        val second = repository.saveRecipe(Recipe(id = "", title = "Otra"))
        assertNotEquals(first, second)

        repository.observeRecipe(first).test {
            assertEquals("Una", awaitItem()?.title)

            repository.saveRecipe(Recipe(first, "Una mejor"))
            assertEquals("Una mejor", awaitItem()?.title)

            repository.deleteRecipe(first)
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
