package com.tavola.core.designsystem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Remembers which entrance animations (a list opening in a cascade, for example) have already played, so they do not
 * play again every time the user comes back to a screen. It has to live above the screens, because a screen that
 * leaves the back stack loses everything it remembered.
 */
@Stable
class PlayedEntrances(played: Collection<String> = emptyList()) {
    private val keys = played.toMutableSet()

    /** `true` the first time [key] is asked for and `false` ever after. */
    fun isFirstTime(key: String): Boolean = keys.add(key)

    internal fun snapshot(): List<String> = keys.toList()
}

/** Provided by the app shell; screens fall back to their own short-lived set when there is none (previews). */
val LocalPlayedEntrances = staticCompositionLocalOf<PlayedEntrances?> { null }

/** One [PlayedEntrances] for the whole app, kept across configuration changes. */
@Composable
fun rememberPlayedEntrances(): PlayedEntrances = rememberSaveable(
    saver = listSaver(save = { entrances -> entrances.snapshot() }, restore = { keys -> PlayedEntrances(keys) }),
) { PlayedEntrances() }
