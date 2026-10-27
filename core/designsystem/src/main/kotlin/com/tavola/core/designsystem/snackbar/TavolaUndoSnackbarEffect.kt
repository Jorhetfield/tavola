package com.tavola.core.designsystem.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Shows "[message] · [undoLabel]" while [removed] is not `null` (the thing that was just removed and can still be brought
 * back). Tapping Undo calls [onUndo]; if the notice closes on its own (timeout or dismiss icon) it calls [onShown], since
 * there is nothing left to restore. If something else is removed before it ends, the notice is replaced by the new one.
 */
@Composable
fun TavolaUndoSnackbarEffect(
    removed: Any?,
    message: String,
    undoLabel: String,
    hostState: SnackbarHostState,
    onUndo: () -> Unit,
    onShown: () -> Unit,
) {
    LaunchedEffect(removed) {
        if (removed != null) {
            // With an action label M3 defaults to Indefinite, so the duration is explicit.
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo() else onShown()
        }
    }
}
