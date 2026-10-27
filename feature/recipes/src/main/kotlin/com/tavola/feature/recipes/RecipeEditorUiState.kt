package com.tavola.feature.recipes

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.IngredientUnit

/** One ingredient row of the form. [key] is stable while the row is on screen, so edits and animations follow the row. */
@Immutable
data class IngredientDraft(val key: Int, val name: String = "", val quantity: String = "", val unit: IngredientUnit = IngredientUnit.Gram)

/** One step of the form. */
@Immutable
data class StepDraft(val key: Int, val text: String = "")

sealed interface RecipeEditorUiState {
    data object Loading : RecipeEditorUiState

    data object Error : RecipeEditorUiState

    /** The recipe being edited does not exist (anymore). */
    data object NotFound : RecipeEditorUiState

    /**
     * @property isNew `true` when creating a recipe, `false` when editing one.
     * @property prepTime minutes as typed (digits only); blank means "not set".
     * @property tagInput text of the tag field, not yet added.
     * @property tagSuggestions tags from other recipes that match [tagInput] and are not already added.
     * @property showTitleError the user tried to save without a title.
     * @property saved the recipe was stored; the screen leaves.
     * @property saveFailed storing the recipe failed; the screen tells the user once.
     */
    @Immutable
    data class Editing(
        val isNew: Boolean,
        val title: String = "",
        val prepTime: String = "",
        val tags: List<String> = emptyList(),
        val tagInput: String = "",
        val tagSuggestions: List<String> = emptyList(),
        val ingredients: List<IngredientDraft> = emptyList(),
        val steps: List<StepDraft> = emptyList(),
        val showTitleError: Boolean = false,
        val saving: Boolean = false,
        val saved: Boolean = false,
        val saveFailed: Boolean = false,
    ) : RecipeEditorUiState
}
