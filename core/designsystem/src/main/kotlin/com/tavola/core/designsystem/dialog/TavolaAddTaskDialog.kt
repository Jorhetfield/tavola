package com.tavola.core.designsystem.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

private const val MAX_VISIBLE_LINES = 4

/**
 * Diálogo con un campo de texto para escribir una tarea. Los textos llegan por parámetro (los pone la pantalla).
 * [onConfirm] solo se invoca con texto no vacío; el texto se conserva al rotar la pantalla. Para editar una tarea, [initialText]
 * trae el texto actual y el cursor empieza al final, para seguir escribiendo. El campo crece hasta [MAX_VISIBLE_LINES] líneas
 * para que un texto largo se lea entero; el teclado sigue ofreciendo "Hecho", no un salto de línea.
 */
@Composable
fun TavolaAddTaskDialog(
    title: String,
    label: String,
    confirmText: String,
    dismissText: String,
    onConfirm: (text: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialText: String = "",
) {
    // A TextFieldValue, not a String, because it is the only way to put the cursor at the end of the text it starts with.
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }
    val text = field.text
    val focusRequester = remember { FocusRequester() }
    val canConfirm = text.isNotBlank()

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            OutlinedTextField(
                value = field,
                onValueChange = { newField -> field = newField },
                label = { Text(label) },
                maxLines = MAX_VISIBLE_LINES,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canConfirm) onConfirm(text) }),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = canConfirm) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        },
    )
}

@TavolaPreviews
@Composable
private fun TavolaAddTaskDialogPreview() {
    TavolaTheme {
        TavolaAddTaskDialog(
            title = "Nueva tarea",
            label = "¿Qué hay que hacer?",
            confirmText = "Añadir",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
