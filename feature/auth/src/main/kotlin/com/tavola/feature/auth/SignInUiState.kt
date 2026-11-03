package com.tavola.feature.auth

import androidx.compose.runtime.Immutable

/** What went wrong with a sign-in, in the terms the person can act on. */
enum class SignInError {
    /** No connection: they can try again when it is back. */
    Network,

    /** The phone has no Google account to choose. */
    NoGoogleAccount,

    /** Anything else. */
    Other,
}

/**
 * @property isSigningIn the token is being checked with the server (the Google sheet itself is the screen's business).
 * @property error why the last attempt failed, or `null`. Cancelling the Google sheet is not an error.
 */
@Immutable
data class SignInUiState(val isSigningIn: Boolean = false, val error: SignInError? = null)

/** What the Google sheet came back with. */
sealed interface GoogleCredentialOutcome {
    /** The person picked an account; [idToken] proves it to Firebase. */
    data class Token(val idToken: String) : GoogleCredentialOutcome

    /** They closed the sheet without choosing. */
    data object Cancelled : GoogleCredentialOutcome

    /** The phone has no Google account to offer. */
    data object NoAccount : GoogleCredentialOutcome

    /** The sheet could not be shown or answered something unexpected. */
    data object Failed : GoogleCredentialOutcome
}
