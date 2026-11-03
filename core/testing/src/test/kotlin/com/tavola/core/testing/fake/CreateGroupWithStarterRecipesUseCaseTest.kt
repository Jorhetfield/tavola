package com.tavola.core.testing.fake

import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.GroupFailure
import com.tavola.domain.model.MyGroupState
import com.tavola.domain.model.Recipe
import com.tavola.domain.model.StarterRecipes
import com.tavola.domain.repository.RecipeRepository
import com.tavola.domain.usecase.CreateGroupWithStarterRecipesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateGroupWithStarterRecipesUseCaseTest {
    private val groups = FakeGroupRepository()
    private val recipes = FakeRecipeRepository(initialRecipes = emptyList())
    private val createGroup = CreateGroupWithStarterRecipesUseCase(groups, recipes)

    @Test
    fun `the group is created and gets the three example recipes with an id each`() = runTest {
        val result = createGroup("Casa")

        assertEquals(GroupActionResult.Done, result)
        assertTrue(groups.myGroup.first() is MyGroupState.Joined)
        val saved = recipes.observeRecipes().first()
        assertEquals(StarterRecipes.all.map { recipe -> recipe.title }.sorted(), saved.map { recipe -> recipe.title }.sorted())
        assertTrue(saved.all { recipe -> recipe.id.isNotBlank() })
        assertTrue(saved.all { recipe -> recipe.ingredients.isNotEmpty() && recipe.steps.isNotEmpty() })
    }

    @Test
    fun `the example recipes are complete recipes, with a title, ingredients and steps`() {
        assertEquals(3, StarterRecipes.all.size)
        StarterRecipes.all.forEach { recipe ->
            assertTrue(recipe.title.isNotBlank(), "a starter recipe has no title")
            assertTrue(recipe.ingredients.isNotEmpty(), "${recipe.title} has no ingredients")
            assertTrue(recipe.steps.isNotEmpty(), "${recipe.title} has no steps")
            assertEquals("", recipe.id, "${recipe.title} has an id: each group has to get its own")
        }
    }

    @Test
    fun `when the group cannot be created no recipe is saved and the failure is reported`() = runTest {
        groups.failWith = GroupFailure.Network

        val result = createGroup("Casa")

        assertEquals(GroupActionResult.Failed(GroupFailure.Network), result)
        assertTrue(recipes.observeRecipes().first().isEmpty())
    }

    @Test
    fun `a blank name creates nothing`() = runTest {
        assertEquals(GroupActionResult.Failed(GroupFailure.InvalidName), createGroup("   "))
        assertTrue(recipes.observeRecipes().first().isEmpty())
    }

    @Test
    fun `an example that cannot be saved does not fail the creation of the group`() = runTest {
        val failing = object : RecipeRepository by recipes {
            override suspend fun saveRecipe(recipe: Recipe): String = throw IllegalStateException("offline")
        }

        val result = CreateGroupWithStarterRecipesUseCase(groups, failing)("Casa")

        assertEquals(GroupActionResult.Done, result)
        assertTrue(groups.myGroup.first() is MyGroupState.Joined)
    }
}
