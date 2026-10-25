package com.tavola.core.designsystem.sheet

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

/**
 * Hoja inferior modal con la forma y los colores de Tavola (esquinas superiores muy redondeadas, superficie del tema).
 * Se abre y se cierra con la animación de resorte del propio Material 3. Para cerrarla desde dentro con esa animación
 * usa [rememberSheetCloser] y pasa el mismo [sheetState].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TavolaModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0f), bottomEnd = CornerSize(0f))
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = Color.Black.copy(alpha = SCRIM_ALPHA),
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant) },
        content = content,
    )
}

/** Devuelve una función que oculta la hoja con su animación y, al terminar, avisa con [onClosed]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberSheetCloser(sheetState: SheetState, onClosed: () -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    return {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onClosed() }
    }
}

private const val SCRIM_ALPHA = 0.4f
