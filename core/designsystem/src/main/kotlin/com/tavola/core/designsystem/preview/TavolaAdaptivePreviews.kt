package com.tavola.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

/**
 * Pantallas completas en móvil (light y dark), plegable y tablet, con barras del sistema visibles.
 * Sirve para comprobar insets, scroll bajo la barra flotante y cómo se estira el contenido en pantallas grandes.
 */
@Preview(name = "Phone", locale = "es", device = Devices.PIXEL_7_PRO, showSystemUi = true)
@Preview(
    name = "Phone dark",
    locale = "es",
    device = Devices.PIXEL_7_PRO,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Foldable", locale = "es", device = Devices.PIXEL_FOLD, showSystemUi = true)
@Preview(name = "Tablet", locale = "es", device = Devices.PIXEL_TABLET, showSystemUi = true)
annotation class TavolaAdaptivePreviews

/** Texto grande (accesibilidad): detecta recortes y desbordes que no se ven con la fuente por defecto. */
@Preview(name = "Font large", locale = "es", showBackground = true, backgroundColor = 0xFFF7F0E6, fontScale = 1.5f)
@Preview(name = "Font huge", locale = "es", showBackground = true, backgroundColor = 0xFFF7F0E6, fontScale = 2f)
annotation class TavolaFontScalePreviews
