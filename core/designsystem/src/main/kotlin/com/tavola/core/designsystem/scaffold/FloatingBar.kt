package com.tavola.core.designsystem.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.tavola.core.designsystem.theme.TavolaTheme

/**
 * What the screens and the floating bar need to agree on: how tall the bar is (so lists leave room under it) and
 * how much of it is hidden because the user is scrolling (0 = fully visible, [heightPx] = fully hidden).
 */
@Stable
class FloatingBarState {
    /** Height of the bar including its margins and the navigation bar inset, in pixels. */
    var heightPx by mutableIntStateOf(0)
        internal set

    /** How far the bar is pushed down, in pixels. */
    var hiddenPx by mutableFloatStateOf(0f)
        internal set

    /** Brings the bar back to full view (for example when the destination changes). */
    fun show() {
        hiddenPx = 0f
    }
}

@Composable
fun rememberFloatingBarState(): FloatingBarState = remember { FloatingBarState() }

/**
 * Provided by the app shell when a single floating bar lives above the whole navigation. Scaffolds that ask for it
 * reserve room for it and hide it on scroll through this state instead of drawing a bar of their own.
 */
val LocalFloatingBarState = staticCompositionLocalOf<FloatingBarState?> { null }

/**
 * Draws the one floating bar of the app, above the navigation content. It slides away when [visible] is false (a
 * full-screen detail) and follows the scroll through [state]. Put it in a `Box` and align it to the bottom.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaFloatingBarLayer(state: FloatingBarState, visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { fullHeight ->
            fullHeight
        } + fadeIn(motion.defaultEffectsSpec()),
        exit = slideOutVertically(motion.fastSpatialSpec<IntOffset>()) { fullHeight -> fullHeight } + fadeOut(motion.fastEffectsSpec()),
    ) {
        Box(
            modifier = Modifier
                .onSizeChanged { size -> state.heightPx = size.height }
                .graphicsLayer { translationY = state.hiddenPx }
                .navigationBarsPadding()
                .padding(bottom = TavolaTheme.spacing.lg),
        ) {
            content()
        }
    }
}
