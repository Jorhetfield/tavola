package com.tavola.feature.calendar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource

/**
 * Shows "Task removed · Undo" while [removedTask] is not `null`. Tapping Undo calls [onUndo]; if the notice
 * closes on its own (timeout or dismiss icon) it calls [onShown], since there is nothing left to restore.
 * If another task is removed before it ends, the notice is replaced by the new one.
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
            // With an action label M3 defaults to Indefinite, so the duration is explicit.
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo() else onShown()
        }
    }
}
