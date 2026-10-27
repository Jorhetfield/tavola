package com.tavola.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.IngredientUnit
import com.tavola.domain.model.Recipe
import com.tavola.domain.repository.RecipeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The create / edit form of a recipe. A blank [recipeId] creates a new recipe; otherwise that recipe is loaded once and
 * edited (the form is never overwritten by later changes to the stored recipe).
 */
@HiltViewModel(assistedFactory = RecipeEditorViewModel.Factory::class)
class RecipeEditorViewModel @AssistedInject constructor(
    @Assisted private val recipeId: String,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {
    private val state = MutableStateFlow<RecipeEditorUiState>(RecipeEditorUiState.Loading)
    val uiState: StateFlow<RecipeEditorUiState> = state.asStateFlow()

    /** Tags used by the recipe book, most used first; the source of the suggestions. */
    private var knownTags: List<String> = emptyList()
    private var nextKey = 0

    init {
        load()
    }

    fun onRetryClick() {
        state.value = RecipeEditorUiState.Loading
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val recipes = recipeRepository.observeRecipes().first()
                knownTags = recipes.rankedTags()
                val recipe = recipes.firstOrNull { stored -> stored.id == recipeId }
                state.value = when {
                    recipeId.isBlank() -> RecipeEditorUiState.Editing(isNew = true)
                    recipe == null -> RecipeEditorUiState.NotFound
                    else -> recipe.toEditing(::newKey)
                }.withSuggestions()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                state.value = RecipeEditorUiState.Error
            }
        }
    }

    fun onTitleChange(text: String) = edit { form -> form.copy(title = text, showTitleError = false) }

    fun onPrepTimeChange(text: String) = edit { form -> form.copy(prepTime = sanitizePrepTime(text)) }

    fun onTagInputChange(text: String) = edit { form ->
        // A comma closes a tag, like pressing the keyboard action; pasted text with several commas becomes several tags.
        if (TAG_SEPARATOR in text) {
            val parts = text.split(TAG_SEPARATOR)
            parts.dropLast(1).fold(form) { current, part -> current.withTag(part) }.copy(tagInput = parts.last()).withSuggestions()
        } else {
            form.copy(tagInput = text).withSuggestions()
        }
    }

    fun onTagAdd() = edit { form -> form.withTag(form.tagInput) }

    fun onTagSuggestionClick(tag: String) = edit { form -> form.withTag(tag) }

    fun onTagRemove(tag: String) = edit { form -> form.copy(tags = form.tags - tag).withSuggestions() }

    fun onIngredientAdd() = edit { form -> form.copy(ingredients = form.ingredients + IngredientDraft(key = newKey())) }

    fun onIngredientRemove(key: Int) = edit { form -> form.copy(ingredients = form.ingredients.filterNot { draft -> draft.key == key }) }

    fun onIngredientNameChange(key: Int, text: String) = editIngredient(key) { draft -> draft.copy(name = text) }

    fun onIngredientQuantityChange(key: Int, text: String) = editIngredient(key) { draft -> draft.copy(quantity = sanitizeQuantity(text)) }

    fun onIngredientUnitChange(key: Int, unit: IngredientUnit) = editIngredient(key) { draft -> draft.copy(unit = unit) }

    fun onStepAdd() = edit { form -> form.copy(steps = form.steps + StepDraft(key = newKey())) }

    fun onStepRemove(key: Int) = edit { form -> form.copy(steps = form.steps.filterNot { step -> step.key == key }) }

    fun onStepChange(key: Int, text: String) = edit { form ->
        form.copy(steps = form.steps.map { step -> if (step.key == key) step.copy(text = text) else step })
    }

    /** Moves the step [offset] places (-1 up, +1 down); it stays where it is at either end. */
    fun onStepMove(key: Int, offset: Int) = edit { form ->
        val from = form.steps.indexOfFirst { step -> step.key == key }
        val to = from + offset
        if (from < 0 || to !in form.steps.indices) {
            form
        } else {
            form.copy(steps = form.steps.toMutableList().apply { add(to, removeAt(from)) })
        }
    }

    fun onSaveClick() {
        val form = state.value as? RecipeEditorUiState.Editing ?: return
        if (form.saving) return
        if (form.title.isBlank()) {
            edit { current -> current.copy(showTitleError = true) }
            return
        }
        edit { current -> current.copy(saving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                recipeRepository.saveRecipe(form.toRecipe(recipeId))
                edit { current -> current.copy(saving = false, saved = true) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                edit { current -> current.copy(saving = false, saveFailed = true) }
            }
        }
    }

    /** The "could not save" notice ended. */
    fun onSaveErrorShown() = edit { form -> form.copy(saveFailed = false) }

    private fun editIngredient(key: Int, transform: (IngredientDraft) -> IngredientDraft) = edit { form ->
        form.copy(ingredients = form.ingredients.map { draft -> if (draft.key == key) transform(draft) else draft })
    }

    /** Applies [transform] to the form; does nothing while it is not on screen (loading, error...). */
    private fun edit(transform: (RecipeEditorUiState.Editing) -> RecipeEditorUiState.Editing) {
        state.update { current -> if (current is RecipeEditorUiState.Editing) transform(current) else current }
    }

    private fun newKey(): Int = nextKey++

    /** Adds [text] as a tag unless it is blank or already there (ignoring case), and clears the tag field. */
    private fun RecipeEditorUiState.Editing.withTag(text: String): RecipeEditorUiState.Editing {
        val tag = text.trim()
        val added = if (tag.isEmpty() || tags.any { existing -> existing.equals(tag, ignoreCase = true) }) tags else tags + tag
        return copy(tags = added, tagInput = "").withSuggestions()
    }

    private fun RecipeEditorUiState.withSuggestions(): RecipeEditorUiState =
        if (this is RecipeEditorUiState.Editing) withSuggestions() else this

    private fun RecipeEditorUiState.Editing.withSuggestions(): RecipeEditorUiState.Editing {
        val typed = tagInput.trim()
        val suggestions = knownTags
            .filter { tag -> tags.none { added -> added.equals(tag, ignoreCase = true) } && tag.contains(typed, ignoreCase = true) }
            .take(MAX_SUGGESTIONS)
        return copy(tagSuggestions = suggestions)
    }

    private fun List<Recipe>.rankedTags(): List<String> = flatMap { recipe -> recipe.tags }
        .groupingBy { tag -> tag }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { entry -> entry.value }.thenBy { entry -> entry.key.lowercase() })
        .map { entry -> entry.key }

    @AssistedFactory
    interface Factory {
        fun create(recipeId: String): RecipeEditorViewModel
    }

    private companion object {
        const val TAG_SEPARATOR = ','
        const val MAX_SUGGESTIONS = 8
    }
}
