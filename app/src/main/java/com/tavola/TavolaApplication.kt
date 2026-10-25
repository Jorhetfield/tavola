package com.tavola

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Raíz de la inyección de dependencias (Hilt). Los módulos de `:data` se instalan aquí solos. */
@HiltAndroidApp
class TavolaApplication : Application()
