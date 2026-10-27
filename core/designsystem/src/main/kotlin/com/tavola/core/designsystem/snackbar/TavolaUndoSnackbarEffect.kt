package com.tavola.core.designsystem.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalAccessibilityManager
import kotlinx.coroutines.withTimeoutOrNull

/** How long the user has to undo a removal, in every screen. */
private const val UNDO_SNACKBAR_MILLIS = 5_000L

/**
 * Shows "[message] · [undoLabel]" for 5 seconds while [removed] is not `null` (the thing that was just removed and can still be
 * brought back). Tapping Undo calls [onUndo]; if the notice closes on its own (timeout or dismiss icon) it calls [onShown],
 * since there is nothing left to restore. If something else is removed before it ends, the notice is replaced by the new one.
 *
 * Material only has 4 and 10 seconds, so the host is told the snackbar never times out and this ends it after 5 (or the longer
 * time an accessibility service asks for); the countdown ring of [TavolaSnackbar] counts that same time.
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
    val accessibilityManager = LocalAccessibilityManager.current
    LaunchedEffect(removed) {
        if (removed != null) {
            val timeoutMillis = recommendedTimeoutMillis(UNDO_SNACKBAR_MILLIS, hasAction = true, accessibilityManager)
            val visuals = TavolaSnackbarVisuals(message, undoLabel, withDismissAction = true, timeoutMillis = timeoutMillis)
            // Cancelling `showSnackbar` takes the snackbar off the screen, and the timeout is what makes `result` null.
            val result = withTimeoutOrNull(timeoutMillis) { hostState.showSnackbar(visuals) }
            if (result == SnackbarResult.ActionPerformed) onUndo() else onShown()
        }
    }
}
