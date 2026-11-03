package com.tavola.feature.auth

import androidx.compose.runtime.Immutable
import com.tavola.domain.model.AuthUser

/** Whether someone is signed in; the gate shows the app, the sign-in screen or a brief loading state. */
sealed interface SessionUiState {
    data object Loading : SessionUiState

    data object SignedOut : SessionUiState

    @Immutable
    data class SignedIn(val user: AuthUser) : SessionUiState
}
