package com.tavola.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Acceso a los tokens propios de Tavola: `TavolaTheme.colors`, `TavolaTheme.spacing`. */
object TavolaTheme {
    val colors: TavolaColors
        @Composable @ReadOnlyComposable
        get() = LocalTavolaColors.current

    val spacing: TavolaSpacing
        @Composable @ReadOnlyComposable
        get() = LocalTavolaSpacing.current
}

/** Tema de Tavola sobre Material 3 Expressive. `dynamicColor` está desactivado a propósito. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) TavolaDarkColorScheme else TavolaLightColorScheme
    val extraColors = if (darkTheme) TavolaDarkExtraColors else TavolaLightExtraColors

    CompositionLocalProvider(
        LocalTavolaColors provides extraColors,
        LocalTavolaSpacing provides TavolaSpacing(),
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = TavolaShapes,
            typography = TavolaTypography,
            content = content,
        )
    }
}
