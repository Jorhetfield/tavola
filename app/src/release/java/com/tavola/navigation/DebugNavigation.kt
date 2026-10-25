package com.tavola.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/** En release no hay menú de depuración. */
@Suppress("UnusedParameter")
internal fun debugMenuAction(backStack: MutableList<NavKey>): (() -> Unit)? = null

@Suppress("UnusedParameter")
internal fun EntryProviderScope<NavKey>.debugEntries(backStack: MutableList<NavKey>) = Unit
