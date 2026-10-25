package com.tavola.core.designsystem.topbar

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** Acción de la barra superior (icono + descripción accesible + callback). */
@Immutable
data class TavolaTopBarAction(val icon: ImageVector, @StringRes val contentDescriptionRes: Int, val onClick: () -> Unit)
