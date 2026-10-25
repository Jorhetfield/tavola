package com.tavola.domain.dispatcher

import javax.inject.Qualifier

// Calificadores de JSR-330 para inyectar `CoroutineDispatcher`. El código de producción nunca usa
// `Dispatchers.IO/Default/Main` directamente: así los tests pueden sustituirlos. Los provee `:data`.

/** Trabajo bloqueante: red, disco, base de datos. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** Trabajo de CPU: mapeos grandes, ordenaciones, parseo. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher

/** Hilo principal. Casi nunca hace falta fuera de la capa de UI. */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher
