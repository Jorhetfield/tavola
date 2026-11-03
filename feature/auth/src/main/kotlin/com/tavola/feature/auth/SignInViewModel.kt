package com.tavola.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.model.SignInFailure
import com.tavola.domain.model.SignInResult
import com.tavola.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The sign-in screen. The Google sheet needs an Activity, so the screen shows it and hands the outcome here; this class only
 * turns the outcome into a sign-in (or an error). On success the session changes and the gate swaps the screen, so there is
 * nothing to report back.
 */
@HiltViewModel
class SignInViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {
    private val state = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = state.asStateFlow()

    fun onCredentialOutcome(outcome: GoogleCredentialOutcome) {
        when (outcome) {
            is GoogleCredentialOutcome.Token -> signIn(outcome.idToken)
            GoogleCredentialOutcome.Cancelled -> state.update { current -> current.copy(error = null) }
            GoogleCredentialOutcome.NoAccount -> state.update { current -> current.copy(error = SignInError.NoGoogleAccount) }
            GoogleCredentialOutcome.Failed -> state.update { current -> current.copy(error = SignInError.Other) }
        }
    }

    private fun signIn(idToken: String) {
        if (state.value.isSigningIn) return
        state.value = SignInUiState(isSigningIn = true)
        viewModelScope.launch {
            val error = when (val result = authRepository.signInWithGoogle(idToken)) {
                is SignInResult.Success -> null
                is SignInResult.Failure -> if (result.reason == SignInFailure.Network) SignInError.Network else SignInError.Other
            }
            state.value = SignInUiState(isSigningIn = false, error = error)
        }
    }
}
