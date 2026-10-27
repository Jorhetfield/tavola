package com.tavola.feature.recipes

import com.tavola.core.testing.data.MediterraneanSampleData
import com.tavola.core.testing.fake.FakeRecipeRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RecipeEditorViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val gazpacho = MediterraneanSampleData.gazpacho

    private fun newRecipe(repository: RecipeRepository = FakeRecipeRepository()) = RecipeEditorViewModel("", repository)

    private fun RecipeEditorViewModel.form() = uiState.value as RecipeEditorUiState.Editing

    @Test
    fun `a blank id opens an empty form`() {
        val form = newRecipe().form()

        assertTrue(form.isNew)
        assertEquals("", form.title)
        assertTrue(form.ingredients.isEmpty() && form.steps.isEmpty())
    }

    @Test
    fun `an existing id opens the form filled with that recipe`() {
        val form = RecipeEditorViewModel(gazpacho.id, FakeRecipeRepository()).form()

        assertFalse(form.isNew)
        assertEquals(gazpacho.title, form.title)
        assertEquals(gazpacho.ingredients.map { ingredient -> ingredient.name }, form.ingredients.map { draft -> draft.name })
        assertEquals(gazpacho.steps, form.steps.map { step -> step.text })
    }

    @Test
    fun `an unknown id is not found`() {
        assertEquals(RecipeEditorUiState.NotFound, RecipeEditorViewModel("nope", FakeRecipeRepository()).uiState.value)
    }

    @Test
    fun `saving without a title shows the error and stores nothing`() = runTest {
        val repository = FakeRecipeRepository(initialRecipes = emptyList())
        val viewModel = newRecipe(repository)

        viewModel.onSaveClick()

        assertTrue(viewModel.form().showTitleError)
        assertFalse(viewModel.form().saved)
        assertTrue(repository.observeRecipes().first().isEmpty())

        viewModel.onTitleChange("Tortilla")
        assertFalse(viewModel.form().showTitleError)
    }

    @Test
    fun `saving a new recipe stores it and marks the form as saved`() = runTest {
        val repository = FakeRecipeRepository(initialRecipes = emptyList())
        val viewModel = newRecipe(repository)
        viewModel.onTitleChange("Tortilla")
        viewModel.onPrepTimeChange("25")
        viewModel.onIngredientAdd()
        val ingredientKey = viewModel.form().ingredients.single().key
        viewModel.onIngredientNameChange(ingredientKey, "Huevos")
        viewModel.onIngredientQuantityChange(ingredientKey, "6")
        viewModel.onIngredientUnitChange(ingredientKey, IngredientUnit.Piece)

        viewModel.onSaveClick()

        val stored = repository.observeRecipes().first().single()
        assertEquals("Tortilla", stored.title)
        assertEquals(25, stored.prepTimeMinutes)
        assertEquals(6.0, stored.ingredients.single().quantity)
        assertTrue(viewModel.form().saved)
    }

    @Test
    fun `saving an edit replaces the recipe under the same id`() = runTest {
        val repository = FakeRecipeRepository()
        val viewModel = RecipeEditorViewModel(gazpacho.id, repository)

        viewModel.onTitleChange("Gazpacho andaluz")
        viewModel.onSaveClick()

        val stored = repository.observeRecipe(gazpacho.id).first()
        assertEquals("Gazpacho andaluz", stored?.title)
        assertEquals(MediterraneanSampleData.allRecipes.size, repository.observeRecipes().first().size)
    }

    @Test
    fun `tags are added from the field or a comma, without duplicates`() {
        val viewModel = newRecipe()

        viewModel.onTagInputChange("Veraniego")
        viewModel.onTagAdd()
        viewModel.onTagInputChange("rapido,")
        viewModel.onTagInputChange("VERANIEGO,")

        val form = viewModel.form()
        assertEquals(listOf("Veraniego", "rapido"), form.tags)
        assertEquals("", form.tagInput)
    }

    @Test
    fun `suggestions come from other recipes, match what is typed and hide the tags already added`() {
        val viewModel = newRecipe()
        val all = viewModel.form().tagSuggestions
        assertTrue(all.isNotEmpty())

        viewModel.onTagSuggestionClick(all.first())
        assertEquals(listOf(all.first()), viewModel.form().tags)
        assertFalse(viewModel.form().tagSuggestions.contains(all.first()))

        viewModel.onTagInputChange("zzz-no-tag")
        assertTrue(viewModel.form().tagSuggestions.isEmpty())
    }

    @Test
    fun `removing a tag takes only that tag`() {
        val viewModel = newRecipe()
        viewModel.onTagInputChange("a,")
        viewModel.onTagInputChange("b,")

        viewModel.onTagRemove("a")

        assertEquals(listOf("b"), viewModel.form().tags)
    }

    @Test
    fun `steps can be moved up and down but not past the ends`() {
        val viewModel = newRecipe()
        repeat(3) { viewModel.onStepAdd() }
        val keys = viewModel.form().steps.map { step -> step.key }
        keys.forEachIndexed { index, key -> viewModel.onStepChange(key, "paso $index") }

        viewModel.onStepMove(keys[0], -1)
        assertEquals(listOf("paso 0", "paso 1", "paso 2"), viewModel.form().steps.map { step -> step.text })

        viewModel.onStepMove(keys[0], 1)
        assertEquals(listOf("paso 1", "paso 0", "paso 2"), viewModel.form().steps.map { step -> step.text })

        viewModel.onStepMove(keys[2], 1)
        assertEquals(listOf("paso 1", "paso 0", "paso 2"), viewModel.form().steps.map { step -> step.text })
    }

    @Test
    fun `removing an ingredient or a step takes only that row`() {
        val viewModel = newRecipe()
        viewModel.onIngredientAdd()
        viewModel.onIngredientAdd()
        viewModel.onStepAdd()
        val keptIngredient = viewModel.form().ingredients.last().key

        viewModel.onIngredientRemove(viewModel.form().ingredients.first().key)
        viewModel.onStepRemove(viewModel.form().steps.single().key)

        assertEquals(listOf(keptIngredient), viewModel.form().ingredients.map { draft -> draft.key })
        assertTrue(viewModel.form().steps.isEmpty())
    }

    @Test
    fun `if the repository fails to load it shows the error and retry recovers`() {
        val flaky = FlakyRecipeRepository()
        val viewModel = RecipeEditorViewModel(gazpacho.id, flaky)
        assertEquals(RecipeEditorUiState.Error, viewModel.uiState.value)

        flaky.shouldFail = false
        viewModel.onRetryClick()

        assertEquals(gazpacho.title, viewModel.form().title)
    }

    @Test
    fun `if storing fails the form stays open and tells the user once`() = runTest {
        val flaky = FlakyRecipeRepository().apply { shouldFail = false }
        val viewModel = newRecipe(flaky)
        viewModel.onTitleChange("Tortilla")
        flaky.failSaves = true

        viewModel.onSaveClick()

        assertTrue(viewModel.form().saveFailed)
        assertFalse(viewModel.form().saved)
        assertFalse(viewModel.form().saving)

        viewModel.onSaveErrorShown()
        assertFalse(viewModel.form().saveFailed)
    }

    private class FlakyRecipeRepository(private val delegate: RecipeRepository = FakeRecipeRepository()) : RecipeRepository by delegate {
        var shouldFail = true
        var failSaves = false

        override fun observeRecipes(): Flow<List<Recipe>> = flow {
            if (shouldFail) error("boom")
            emit(delegate.observeRecipes().first())
        }

        override suspend fun saveRecipe(recipe: Recipe): String {
            if (failSaves) error("boom")
            return delegate.saveRecipe(recipe)
        }
    }

    @Test
    fun `pasted text with several commas becomes several tags and leaves the rest in the field`() {
        val viewModel = newRecipe()

        viewModel.onTagInputChange("Griego,Arroz, Verano,sob")

        val form = viewModel.form()
        assertEquals(listOf("Griego", "Arroz", "Verano"), form.tags)
        assertEquals("sob", form.tagInput)
    }

    @Test
    fun `the quantity field only accepts what can be a quantity`() {
        val viewModel = newRecipe()
        viewModel.onIngredientAdd()
        val key = viewModel.form().ingredients.single().key

        viewModel.onIngredientQuantityChange(key, "1/2 kg")

        assertEquals("12", viewModel.form().ingredients.single().quantity)
    }
}
