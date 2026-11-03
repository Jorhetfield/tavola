package com.tavola.core.designsystem.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * Asks the person to confirm something they cannot easily undo. With [isDestructive] the confirm button is drawn in the error
 * color, so leaving a group does not look like saving a name.
 */
@Composable
fun TavolaConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    val confirmColors = if (isDestructive) {
        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
    } else {
        ButtonDefaults.textButtonColors()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { TextButton(onClick = onConfirm, colors = confirmColors) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissText) } },
    )
}

@TavolaPreviews
@Composable
private fun TavolaConfirmDialogPreview() {
    TavolaTheme {
        TavolaConfirmDialog(
            title = "¿Salir del grupo?",
            message = "Dejarás de ver el calendario y las recetas del grupo.",
            confirmText = "Salir",
            dismissText = "Cancelar",
            onConfirm = {},
            onDismiss = {},
            isDestructive = true,
        )
    }
}
