package com.tavola.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay

private const val PARALLAX_DIVISOR = 4
private const val BEHIND_SHEET_SCALE = 0.94f
private const val BEHIND_SHEET_ALPHA = 0.4f
private val SheetCornerRadius = 28.dp
private val SheetElevation = 12.dp

/**
 * The transitions of the app, all built from the theme's motion scheme (rule 11):
 * - Between top-level tabs the new tab slides in from the side it sits on in the bar and the old one leaves the
 *   other way, with a little parallax.
 * - A detail rises from the bottom like a sheet while what is behind it steps back (smaller and dimmer); going back
 *   (button, system back or the predictive gesture) reverses it.
 *
 * Transitions are chosen by the destination that ends up on top, so each one is set on the entry it belongs to.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal class NavigationMotion(private val motion: MotionScheme) {
    /** Metadata for top-level tab entries. */
    val tabMetadata: Map<String, Any> = NavDisplay.transitionSpec { tabTransition() } +
        NavDisplay.popTransitionSpec { tabTransition() } +
        NavDisplay.predictivePopTransitionSpec { _ -> tabTransition() }

    /** Metadata for entries that behave as a sheet over the previous screen. */
    val sheetMetadata: Map<String, Any> = NavDisplay.transitionSpec { sheetEnter() } +
        NavDisplay.popTransitionSpec { sheetExit() } +
        NavDisplay.predictivePopTransitionSpec { _ -> sheetExit() }

    private fun AnimatedContentTransitionScope<Scene<*>>.tabTransition(): ContentTransform? {
        val from = initialState.topLevelIndex() ?: return null
        val to = targetState.topLevelIndex() ?: return null
        val direction = if (to > from) 1 else -1
        return ContentTransform(
            targetContentEnter = slideInHorizontally(motion.defaultSpatialSpec<IntOffset>()) { fullWidth -> direction * fullWidth } +
                fadeIn(motion.defaultEffectsSpec()),
            initialContentExit = slideOutHorizontally(motion.defaultSpatialSpec<IntOffset>()) { fullWidth ->
                -direction * fullWidth / PARALLAX_DIVISOR
            } + fadeOut(motion.fastEffectsSpec()),
        )
    }

    private fun sheetEnter(): ContentTransform = ContentTransform(
        targetContentEnter = slideInVertically(motion.defaultSpatialSpec<IntOffset>()) { fullHeight -> fullHeight },
        initialContentExit = scaleOut(motion.defaultSpatialSpec(), targetScale = BEHIND_SHEET_SCALE) +
            fadeOut(motion.defaultEffectsSpec(), targetAlpha = BEHIND_SHEET_ALPHA),
    )

    private fun sheetExit(): ContentTransform = ContentTransform(
        targetContentEnter = scaleIn(motion.defaultSpatialSpec(), initialScale = BEHIND_SHEET_SCALE) +
            fadeIn(motion.defaultEffectsSpec(), initialAlpha = BEHIND_SHEET_ALPHA),
        initialContentExit = slideOutVertically(motion.fastSpatialSpec<IntOffset>()) { fullHeight -> fullHeight },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun rememberNavigationMotion(): NavigationMotion {
    val motion = MaterialTheme.motionScheme
    return remember(motion) { NavigationMotion(motion) }
}

/** Index of the top-level tab this scene shows, or `null` if it is not a tab. */
private fun Scene<*>.topLevelIndex(): Int? = entries.lastOrNull()?.contentKey.topLevelIndex()

/**
 * Wraps the content of a sheet entry: while the sheet is entering or leaving it has rounded top corners and a shadow,
 * which settle to a plain full-screen surface once it is at rest.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SheetEntrySurface(content: @Composable () -> Unit) {
    val motion = MaterialTheme.motionScheme
    val transition = LocalNavAnimatedContentScope.current.transition
    // The spatial springs overshoot, which for a size would go below zero (a negative corner radius crashes), so it is clamped.
    val corner by transition.animateDp(transitionSpec = { motion.defaultSpatialSpec() }, label = "sheetCorner") { state ->
        if (state == EnterExitState.Visible) 0.dp else SheetCornerRadius
    }
    val elevation by transition.animateDp(transitionSpec = { motion.defaultEffectsSpec() }, label = "sheetElevation") { state ->
        if (state == EnterExitState.Visible) 0.dp else SheetElevation
    }
    val safeCorner = corner.coerceAtLeast(0.dp)
    Surface(
        shape = RoundedCornerShape(topStart = safeCorner, topEnd = safeCorner),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = elevation.coerceAtLeast(0.dp),
    ) {
        content()
    }
}
