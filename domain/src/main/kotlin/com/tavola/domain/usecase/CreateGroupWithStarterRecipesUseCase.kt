package com.tavola.domain.usecase

import com.tavola.domain.model.GroupActionResult
import com.tavola.domain.model.Recipe
import com.tavola.domain.model.StarterRecipes
import com.tavola.domain.repository.GroupRepository
import com.tavola.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Creates a group and, once it exists, gives it the example recipes. The examples are a nicety: if one cannot be saved the group
 * is already created, so the person is not told it failed, they just start with fewer recipes. It is a use case because it
 * combines two repositories.
 */
class CreateGroupWithStarterRecipesUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val recipeRepository: RecipeRepository,
) {
    suspend operator fun invoke(name: String): GroupActionResult {
        val result = groupRepository.createGroup(name)
        if (result is GroupActionResult.Done) {
            // Together, not one after another: each save can wait a few seconds for the server, and they do not depend on each other.
            coroutineScope {
                StarterRecipes.all.map { recipe -> async { saveExample(recipe) } }.awaitAll()
            }
        }
        return result
    }

    private suspend fun saveExample(recipe: Recipe) {
        try {
            recipeRepository.saveRecipe(recipe)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            // The group exists; an example that is missing is not worth a failure.
        }
    }
}
