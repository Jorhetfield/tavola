package com.tavola.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Rutas de la app. Llevan solo identificadores simples, nunca objetos pesados. Solo `:app` las conoce. */
@Serializable
data object CalendarRoute : NavKey
