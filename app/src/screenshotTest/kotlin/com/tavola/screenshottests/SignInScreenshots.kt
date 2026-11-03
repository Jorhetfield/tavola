package com.tavola.screenshottests

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.tavola.core.designsystem.preview.TavolaAdaptivePreviews
import com.tavola.core.designsystem.preview.TavolaFontScalePreviews
import com.tavola.core.designsystem.preview.TavolaScreenPreviews
import com.tavola.core.designsystem.theme.TavolaTheme
import com.tavola.feature.auth.SignInContent
import com.tavola.feature.auth.SignInError
import com.tavola.feature.auth.SignInUiState

@PreviewTest
@TavolaAdaptivePreviews
@Composable
fun SignInScreenshot() {
    TavolaTheme { SignInContent(SignInUiState(), isRequestingCredential = false, onContinueWithGoogle = {}) }
}

/** The button waits while the token is checked. */
@PreviewTest
@TavolaScreenPreviews
@Composable
fun SignInBusyScreenshot() {
    TavolaTheme { SignInContent(SignInUiState(isSigningIn = true), isRequestingCredential = false, onContinueWithGoogle = {}) }
}

@PreviewTest
@TavolaScreenPreviews
@Composable
fun SignInNetworkErrorScreenshot() {
    TavolaTheme { SignInContent(SignInUiState(error = SignInError.Network), isRequestingCredential = false, onContinueWithGoogle = {}) }
}

/** The longest message with large text: nothing may be clipped. */
@PreviewTest
@TavolaFontScalePreviews
@Composable
fun SignInLargeFontScreenshot() {
    TavolaTheme {
        SignInContent(SignInUiState(error = SignInError.NoGoogleAccount), isRequestingCredential = false, onContinueWithGoogle = {})
    }
}
