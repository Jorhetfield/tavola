package com.tavola.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.loading.TavolaFullScreenLoading

/**
 * Shows [content] (the app) to someone who is signed in and the sign-in screen to everyone else. The change between them
 * fades with the motion scheme, and the session is kept by Firebase, so a returning person sees the app straight away.
 *
 * @param webClientId the web client of the Firebase project, for the sign-in screen.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AuthGate(
    webClientId: String,
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val session by viewModel.uiState.collectAsStateWithLifecycle()
    val motion = MaterialTheme.motionScheme

    AnimatedContent(
        targetState = session,
        modifier = modifier,
        // A sign-in that gives another person is still the same screen: only the kind of state swaps the content.
        contentKey = { current -> current::class },
        transitionSpec = { fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.fastEffectsSpec()) },
        label = "authGate",
    ) { current ->
        when (current) {
            SessionUiState.Loading -> TavolaFullScreenLoading(label = stringResource(R.string.auth_loading))
            SessionUiState.SignedOut -> SignInScreen(webClientId)
            is SessionUiState.SignedIn -> content()
        }
    }
}
