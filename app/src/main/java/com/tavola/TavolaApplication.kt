package com.tavola

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

private const val LEGACY_DATABASE_NAME = "tavola.db"

/** Raíz de la inyección de dependencias (Hilt). Los módulos de `:data` se instalan aquí solos. */
@HiltAndroidApp
class TavolaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // The calendar and the recipes used to live in a local database; they are in Firestore now, so what is left of it is
        // removed (it was test data). It does nothing when the file is not there.
        deleteDatabase(LEGACY_DATABASE_NAME)
    }
}
