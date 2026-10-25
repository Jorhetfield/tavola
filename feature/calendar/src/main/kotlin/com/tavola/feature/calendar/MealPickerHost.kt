package com.tavola.feature.calendar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.tavola.core.designsystem.sheet.TavolaModalBottomSheet
import com.tavola.core.designsystem.sheet.rememberSheetCloser

/**
 * Contenido del selector de comida que aporta `:app`. Recibe el hueco elegido y una función para cerrar la hoja con su
 * animación (el selector la llama al guardar). Así `:feature:calendar` no conoce a `:feature:recipes`.
 */
typealias MealPickerSlot = @Composable (target: MealPickerTarget, onClose: () -> Unit) -> Unit

/** Hoja inferior que aloja el selector. Se cierra con la animación del tema tanto al guardar como al deslizarla o tocar fuera. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MealPickerHost(target: MealPickerTarget, onDismiss: () -> Unit, content: MealPickerSlot) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close = rememberSheetCloser(sheetState, onClosed = onDismiss)
    TavolaModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        content(target, close)
    }
}
