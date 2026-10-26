package com.tavola.core.designsystem.scaffold

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * Único scaffold permitido en la app (regla 4).
 *
 * Estrategia de insets (edge-to-edge, `enableEdgeToEdge()` en la Activity):
 * - El contenido se dibuja bajo las barras del sistema; el scaffold no recorta ni empuja nada.
 * - La barra superior gestiona el inset de la status bar por sí misma.
 * - La barra flotante se superpone al contenido (no es `bottomBar`), así que el contenido pasa por detrás.
 * - [content] recibe un [PaddingValues] con `top` = alto de la barra superior y `bottom` = hueco que ocupa
 *   la barra flotante más la navigation bar. Úsalo como `contentPadding` de la lista.
 *
 * Si [hideFloatingBarOnScroll] es true, la barra flotante se esconde al hacer scroll hacia abajo y reaparece
 * al subir. Se desplaza el contenedor completo (márgenes incluidos) para que no quede ni un borde visible.
 *
 * [snackbarHost] se dibuja justo encima de la barra flotante (o de la navigation bar si no hay).
 */
@Composable
fun TavolaScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingBar: (@Composable () -> Unit)? = null,
    hideFloatingBarOnScroll: Boolean = true,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val spacing = TavolaTheme.spacing
    val density = LocalDensity.current
    var floatingBarHeightPx by remember { mutableIntStateOf(0) }
    var hiddenPx by remember { mutableFloatStateOf(0f) }

    val scrollConnection = remember(hideFloatingBarOnScroll) {
        FloatingBarScrollConnection(
            enabled = hideFloatingBarOnScroll,
            maxHidden = { floatingBarHeightPx.toFloat() },
            hidden = { hiddenPx },
            onHiddenChange = { newHidden -> hiddenPx = newHidden },
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollConnection),
        topBar = topBar,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val bottomClearance = with(density) { floatingBarHeightPx.toDp() } + spacing.lg

        Box(Modifier.fillMaxSize()) {
            content(
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (floatingBar == null) spacing.lg else bottomClearance,
                ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .then(if (floatingBar == null) Modifier.navigationBarsPadding() else Modifier)
                    .padding(horizontal = spacing.lg)
                    .padding(bottom = if (floatingBar == null) spacing.lg else bottomClearance),
            ) {
                snackbarHost()
            }
            if (floatingBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { size -> floatingBarHeightPx = size.height }
                        .graphicsLayer { translationY = hiddenPx }
                        .navigationBarsPadding()
                        .padding(bottom = spacing.lg),
                ) {
                    floatingBar()
                }
            }
        }
    }
}

/** Convierte el scroll del contenido en cuánto está escondida la barra flotante (0 = visible). */
private class FloatingBarScrollConnection(
    private val enabled: Boolean,
    private val maxHidden: () -> Float,
    private val hidden: () -> Float,
    private val onHiddenChange: (Float) -> Unit,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (enabled) {
            onHiddenChange((hidden() - available.y).coerceIn(0f, maxHidden()))
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        if (enabled) {
            val start = hidden()
            val target = if (start > maxHidden() / 2f) maxHidden() else 0f
            animate(
                initialValue = start,
                targetValue = target,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) { value, _ -> onHiddenChange(value) }
        }
        return Velocity.Zero
    }
}
