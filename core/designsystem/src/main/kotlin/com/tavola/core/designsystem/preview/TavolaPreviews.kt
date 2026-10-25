package com.tavola.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** Preview light + dark. Cada composable del design system la lleva (regla 5). */
@Preview(name = "Light", locale = "es", showBackground = true, backgroundColor = 0xFFF7F0E6)
@Preview(
    name = "Dark",
    locale = "es",
    showBackground = true,
    backgroundColor = 0xFF0F1A24,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class TavolaPreviews

/** Igual que [TavolaPreviews] pero a pantalla completa, para scaffold y pantallas. */
@Preview(name = "Light", locale = "es", showSystemUi = true)
@Preview(name = "Dark", locale = "es", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class TavolaScreenPreviews
