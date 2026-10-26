package com.tavola.feature.calendar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource

/**
 * Muestra "Tarea eliminada · Deshacer" mientras [removedTask] no sea `null`. Si se pulsa Deshacer avisa con
 * [onUndo]; si el aviso se cierra solo, con [onShown], porque ya no hay nada que restaurar. Si llega otra
 * eliminación antes de que acabe, el aviso se sustituye por el nuevo.
 */
@Composable
internal fun RemovedTaskSnackbarEffect(
    removedTask: RemovedTask?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onShown: () -> Unit,
) {
    val message = stringResource(R.string.calendar_task_removed)
    val undoLabel = stringResource(R.string.calendar_task_undo)

    LaunchedEffect(removedTask) {
        if (removedTask != null) {
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = undoLabel, withDismissAction = true)
            if (result == SnackbarResult.ActionPerformed) onUndo() else onShown()
        }
    }
}
