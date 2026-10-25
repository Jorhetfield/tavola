package com.tavola.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tavola.feature.showcase.ShowcaseScreen
import kotlinx.serialization.Serializable

/** Solo existe en debug: pantalla de muestra del tema y los componentes. */
@Serializable
data object ShowcaseRoute : NavKey

/** Acción para abrir el showcase desde el calendario (en release es `null` y el botón no aparece). */
internal fun debugMenuAction(backStack: MutableList<NavKey>): (() -> Unit)? = { backStack.add(ShowcaseRoute) }

internal fun EntryProviderScope<NavKey>.debugEntries(backStack: MutableList<NavKey>) {
    entry<ShowcaseRoute> { ShowcaseScreen() }
}
