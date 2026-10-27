package com.tavola.core.designsystem.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The main action of the current destination ("new recipe"), docked to the floating bar as its own round button.
 * [contentDescriptionRes] names it for accessibility, since it only shows the [icon].
 */
@Immutable
data class TavolaNavBarAction(val icon: ImageVector, @StringRes val contentDescriptionRes: Int, val onClick: () -> Unit)
