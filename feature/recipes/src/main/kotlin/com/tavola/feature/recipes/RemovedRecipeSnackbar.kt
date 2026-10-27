package com.tavola.feature.recipes

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.tavola.domain.model.Recipe

/**
 * Shows "Recipe deleted · Undo" while [removedRecipe] is not `null`. Tapping Undo calls [onUndo]; if the notice closes on its
 * own (timeout or dismiss icon) it calls [onShown], since there is nothing left to restore. If another recipe is deleted
 * before it ends, the notice is replaced by the new one.
 */
@Composable
internal fun RemovedRecipeSnackbarEffect(
    removedRecipe: Recipe?,
    snackbarHostState: SnackbarHostState,
    onUndo: () -> Unit,
    onShown: () -> Unit,
) {
    val message = stringResource(R.string.recipe_list_removed)
    val undoLabel = stringResource(R.string.recipe_list_undo)

    LaunchedEffect(removedRecipe) {
        if (removedRecipe != null) {
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
