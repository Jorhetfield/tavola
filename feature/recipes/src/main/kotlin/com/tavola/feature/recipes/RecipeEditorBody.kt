package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.theme.TavolaTheme

/** The form: name and time, tags, ingredients and steps. */
@Composable
internal fun RecipeEditorBody(form: RecipeEditorUiState.Editing, actions: RecipeEditorActions, contentPadding: PaddingValues) {
    val spacing = TavolaTheme.spacing
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(
            start = spacing.lg,
            end = spacing.lg,
            top = contentPadding.calculateTopPadding() + spacing.sm,
            bottom = contentPadding.calculateBottomPadding() + spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        item(key = BASICS_KEY) { BasicsSection(form, actions) }
        item(key = TAGS_KEY) { TagsSection(form, actions) }
        ingredientsSection(form.ingredients, actions)
        stepsSection(form.steps, actions)
    }
}

@Composable
private fun BasicsSection(form: RecipeEditorUiState.Editing, actions: RecipeEditorActions) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(TavolaTheme.spacing.sm)) {
        TavolaTextField(
            value = form.title,
            onValueChange = actions.onTitleChange,
            label = stringResource(R.string.recipe_editor_name_label),
            modifier = Modifier.fillMaxWidth(),
            imeAction = ImeAction.Next,
            onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
            isError = form.showTitleError,
        )
        if (form.showTitleError) {
            Text(
                text = stringResource(R.string.recipe_editor_name_required),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TavolaTextField(
            value = form.prepTime,
            onValueChange = actions.onPrepTimeChange,
            label = stringResource(R.string.recipe_editor_prep_time_label),
            modifier = Modifier.fillMaxWidth(),
            keyboardType = KeyboardType.Number,
            onImeAction = { focusManager.clearFocus() },
        )
    }
}

@Composable
internal fun EditorSectionTitle(titleRes: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.fillMaxWidth(),
    )
}

/** "+ Add ..." button that closes a list section. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun AddRowButton(labelRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick = onClick, modifier = modifier, shapes = ButtonDefaults.shapes()) {
        Icon(Icons.Rounded.Add, contentDescription = null)
        Text(text = stringResource(labelRes), modifier = Modifier.padding(start = TavolaTheme.spacing.sm))
    }
}

/** Rows that enter, leave and move with the theme's motion, so adding, removing and reordering reads as one gesture. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LazyItemScope.editorItemMotion(): Modifier {
    val motion = MaterialTheme.motionScheme
    return Modifier.animateItem(
        fadeInSpec = motion.defaultEffectsSpec(),
        placementSpec = motion.defaultSpatialSpec(),
        fadeOutSpec = motion.fastEffectsSpec(),
    )
}

private const val BASICS_KEY = "basics"
private const val TAGS_KEY = "tags"
