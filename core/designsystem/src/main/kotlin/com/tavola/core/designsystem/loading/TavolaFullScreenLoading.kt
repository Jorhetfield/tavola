package com.tavola.core.designsystem.loading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tavola.core.designsystem.preview.TavolaPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlinx.coroutines.delay

private const val SPINNER_DELAY_MILLIS = 400L

/**
 * A full-screen wait for something that normally takes a few milliseconds (the stored session, the group of the person). The
 * spinner only shows if it takes longer than a moment: someone for whom it is quick sees the plain background and then the
 * content, not a flash of loading.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TavolaFullScreenLoading(label: String, modifier: Modifier = Modifier) {
    val motion = MaterialTheme.motionScheme
    var showSpinner by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(SPINNER_DELAY_MILLIS)
        showSpinner = true
    }
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(visible = showSpinner, enter = fadeIn(motion.defaultEffectsSpec()), exit = fadeOut(motion.fastEffectsSpec())) {
            TavolaLoadingIndicator(label = label)
        }
    }
}

@TavolaPreviews
@Composable
private fun TavolaFullScreenLoadingPreview() {
    TavolaTheme { TavolaFullScreenLoading(label = "Abriendo tu sesión…") }
}
