package com.tavola.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.tavola.core.designsystem.field.TavolaTextField
import com.tavola.core.designsystem.theme.TavolaTheme

/** Pestaña de comida rápida: título, nota opcional y sugerencias de un toque ("Sobras", "Comemos fuera"...). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun QuickMealPane(
    title: String,
    note: String,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = TavolaTheme.spacing
    val focusRequester = remember { FocusRequester() }
    val suggestions = listOf(
        stringResource(R.string.meal_picker_quick_leftovers),
        stringResource(R.string.meal_picker_quick_eat_out),
        stringResource(R.string.meal_picker_quick_sandwich),
        stringResource(R.string.meal_picker_quick_takeaway),
    )

    // Si se llega sin título (nueva comida rápida), el teclado se abre directamente sobre el campo.
    LaunchedEffect(Unit) { if (title.isBlank()) focusRequester.requestFocus() }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        TavolaTextField(
            value = title,
            onValueChange = onTitleChange,
            label = stringResource(R.string.meal_picker_quick_title_label),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            imeAction = ImeAction.Next,
        )
        TavolaTextField(
            value = note,
            onValueChange = onNoteChange,
            label = stringResource(R.string.meal_picker_quick_note_label),
            modifier = Modifier.fillMaxWidth(),
            onImeAction = { if (title.isNotBlank()) onConfirm() },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    onClick = { onTitleChange(suggestion) },
                    label = { Text(suggestion) },
                    shape = CircleShape,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onConfirm,
            enabled = title.isNotBlank(),
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null)
            Text(text = stringResource(R.string.meal_picker_quick_save), modifier = Modifier.padding(start = spacing.sm))
        }
    }
}
