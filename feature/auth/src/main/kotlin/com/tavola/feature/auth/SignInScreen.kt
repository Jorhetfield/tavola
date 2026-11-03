package com.tavola.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.credentials.CredentialManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.scaffold.TavolaScaffold
import com.tavola.core.designsystem.theme.TavolaTheme
import kotlinx.coroutines.launch

private const val BADGE_SIZE_STEPS = 2
private const val CONTENT_MAX_WIDTH_STEPS = 10

/**
 * Where a signed-out person lands: the name of the app and one button to continue with Google.
 *
 * @param webClientId the web client of the Firebase project, which Google needs to issue a token Firebase accepts.
 */
@Composable
fun SignInScreen(webClientId: String, modifier: Modifier = Modifier, viewModel: SignInViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // The Google sheet is the screen's business (it needs the Activity); while it is open the button waits. A rotation closes
    // the sheet and its answer is lost, which is harmless: the button simply comes back.
    var isRequesting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val credentialClient = remember(context) { GoogleCredentialClient(CredentialManager.create(context)) }
    val scope = rememberCoroutineScope()

    SignInContent(
        state = state,
        isRequestingCredential = isRequesting,
        onContinueWithGoogle = {
            isRequesting = true
            scope.launch {
                val outcome = credentialClient.requestIdToken(context, webClientId)
                isRequesting = false
                viewModel.onCredentialOutcome(outcome)
            }
        },
        modifier = modifier,
    )
}

/** Stateless part of the screen, so it can be previewed and shown in the screenshots. */
@Composable
fun SignInContent(state: SignInUiState, isRequestingCredential: Boolean, onContinueWithGoogle: () -> Unit, modifier: Modifier = Modifier) {
    TavolaScaffold(modifier = modifier) { _ ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            SignInColumn(state.isSigningIn || isRequestingCredential, state.error, onContinueWithGoogle)
        }
    }
}

@Composable
private fun SignInColumn(isBusy: Boolean, error: SignInError?, onContinueWithGoogle: () -> Unit) {
    val spacing = TavolaTheme.spacing
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .widthIn(max = spacing.xxxl * CONTENT_MAX_WIDTH_STEPS)
            .padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppBadge()
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = stringResource(R.string.auth_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = stringResource(R.string.auth_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(spacing.xxl))
        GoogleButton(isBusy, onContinueWithGoogle)
        SignInErrorMessage(error)
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = stringResource(R.string.auth_google_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppBadge() {
    Box(
        modifier = Modifier
            .size(TavolaTheme.spacing.xxxl * BADGE_SIZE_STEPS)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.RestaurantMenu,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(TavolaTheme.spacing.xxxl),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GoogleButton(isBusy: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !isBusy, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) {
        Crossfade(targetState = isBusy, animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(), label = "signInButton") { busy ->
            Text(text = stringResource(if (busy) R.string.auth_signing_in else R.string.auth_continue_with_google))
        }
    }
}

/** The reason the last attempt failed, below the button; it is announced to screen readers when it appears. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SignInErrorMessage(error: SignInError?) {
    val motion = MaterialTheme.motionScheme
    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically(motion.defaultSpatialSpec<IntSize>()) + fadeIn(motion.defaultEffectsSpec()),
        exit = shrinkVertically(motion.fastSpatialSpec<IntSize>()) + fadeOut(motion.fastEffectsSpec()),
    ) {
        // The text of the error that was last shown stays while it fades out.
        val message = error?.let { shown -> stringResource(shown.messageRes()) }.orEmpty()
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = TavolaTheme.spacing.md)
                .semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

private fun SignInError.messageRes(): Int = when (this) {
    SignInError.Network -> R.string.auth_error_network
    SignInError.NoGoogleAccount -> R.string.auth_error_no_account
    SignInError.Other -> R.string.auth_error_other
}

@TavolaAdaptivePreviews
@Composable
private fun SignInScreenPreview() {
    TavolaTheme { SignInContent(SignInUiState(), isRequestingCredential = false, onContinueWithGoogle = {}) }
}

@TavolaFontScalePreviews
@Composable
private fun SignInScreenFontScalePreview() {
    TavolaTheme { SignInContent(SignInUiState(error = SignInError.Network), isRequestingCredential = false, onContinueWithGoogle = {}) }
}
