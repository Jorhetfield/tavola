package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.theme.TavolaTheme

private val ChipIconSize = 18.dp

/** Free-text tags: the ones already added (tap to remove), a field to type a new one and suggestions from other recipes. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun TagsSection(form: RecipeEditorUiState.Editing, actions: RecipeEditorActions) {
    val spacing = TavolaTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        EditorSectionTitle(R.string.recipe_editor_tags)
        if (form.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                form.tags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { actions.onTagRemove(tag) },
                        label = { Text(tag) },
                        shape = CircleShape,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.recipe_editor_tag_remove, tag),
                                modifier = Modifier.size(ChipIconSize),
                            )
                        },
                    )
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TavolaTextField(
                value = form.tagInput,
                onValueChange = actions.onTagInputChange,
                label = stringResource(R.string.recipe_editor_tag_label),
                modifier = Modifier.weight(1f),
                imeAction = ImeAction.Done,
                onImeAction = actions.onTagAdd,
            )
            FilledTonalIconButton(
                onClick = actions.onTagAdd,
                enabled = form.tagInput.isNotBlank(),
                shapes = IconButtonDefaults.shapes(),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.recipe_editor_tag_add))
            }
        }
        if (form.tagSuggestions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                form.tagSuggestions.forEach { tag ->
                    SuggestionChip(onClick = { actions.onTagSuggestionClick(tag) }, label = { Text(tag) }, shape = CircleShape)
                }
            }
        }
    }
}
