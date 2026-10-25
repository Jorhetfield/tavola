package com.tavola.core.designsystem.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** Destino de la barra flotante. [key] identifica el destino sin atarse a la navegación de la app. */
@Immutable
data class TavolaNavItem(val key: String, @StringRes val labelRes: Int, val icon: ImageVector, val selectedIcon: ImageVector = icon)
