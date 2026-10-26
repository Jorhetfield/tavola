package com.tavola.core.designsystem.scaffold

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlin.math.roundToInt

private val SnackbarOuterPadding = 12.dp

/**
 * The only scaffold allowed in the app (rule 4).
 *
 * Insets strategy (edge-to-edge, `enableEdgeToEdge()` in the Activity):
 * - The content is drawn under the system bars; the scaffold neither clips nor pushes anything.
 * - The top bar handles the status bar inset by itself.
 * - The floating bar overlaps the content (it is not a `bottomBar`), so the content scrolls behind it.
 * - [content] receives a [PaddingValues] with `top` = height of the top bar and `bottom` = the room taken by the
 *   floating bar plus the navigation bar. Use it as the `contentPadding` of the list.
 *
 * The floating bar can come in two ways. With [floatingBar] the scaffold draws it itself (previews, standalone
 * screens). With [reservesFloatingBarSpace] the app shell draws one bar above the whole navigation and provides its
 * [FloatingBarState] ([LocalFloatingBarState]); the scaffold only leaves room for it and hides it on scroll.
 *
 * If [hideFloatingBarOnScroll] is true, the floating bar hides when scrolling down and comes back when scrolling
 * up. The whole container (margins included) moves so that no edge stays visible.
 *
 * [snackbarHost] is drawn just above the floating bar (or above the navigation bar when it is hidden or absent).
 */
@Composable
fun TavolaScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingBar: (@Composable () -> Unit)? = null,
    reservesFloatingBarSpace: Boolean = false,
    hideFloatingBarOnScroll: Boolean = true,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val spacing = TavolaTheme.spacing
    val density = LocalDensity.current
    val shellBarState = LocalFloatingBarState.current
    val barState = shellBarState ?: rememberFloatingBarState()
    val hasFloatingBar = floatingBar != null || (shellBarState != null && reservesFloatingBarSpace)

    val scrollConnection = remember(hideFloatingBarOnScroll, hasFloatingBar, barState) {
        FloatingBarScrollConnection(
            enabled = hideFloatingBarOnScroll && hasFloatingBar,
            maxHidden = { barState.heightPx.toFloat() },
            hidden = { barState.hiddenPx },
            onHiddenChange = { newHidden -> barState.hiddenPx = newHidden },
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollConnection),
        topBar = topBar,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val bottomClearance = with(density) { barState.heightPx.toDp() } + spacing.lg

        Box(Modifier.fillMaxSize()) {
            content(
                PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = if (hasFloatingBar) bottomClearance else spacing.lg,
                ),
            )
            // The snackbar follows the floating bar: `lg` above it while visible and `lg` above the navigation bar
            // when it is hidden. M3 already adds its own margin, which is subtracted (never below zero).
            val navigationBarPx = WindowInsets.navigationBars.getBottom(density)
            val snackbarMargin = (spacing.lg - SnackbarOuterPadding).coerceAtLeast(0.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = snackbarMargin)
                    .offset {
                        val visibleBarPx = if (hasFloatingBar) barState.heightPx - barState.hiddenPx.roundToInt() else 0
                        IntOffset(x = 0, y = -(maxOf(visibleBarPx, navigationBarPx) + snackbarMargin.roundToPx()))
                    },
            ) {
                snackbarHost()
            }
            if (floatingBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { size -> barState.heightPx = size.height }
                        .graphicsLayer { translationY = barState.hiddenPx }
                        .navigationBarsPadding()
                        .padding(bottom = spacing.lg),
                ) {
                    floatingBar()
                }
            }
        }
    }
}

/** Turns the scroll of the content into how much of the floating bar is hidden (0 = visible). */
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
