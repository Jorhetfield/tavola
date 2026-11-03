package com.tavola.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tavola.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The session of the whole app: who is signed in, or that nobody is. It follows the repository, so a sign-out shows up at once. */
@HiltViewModel
class SessionViewModel @Inject constructor(authRepository: AuthRepository) : ViewModel() {
    val uiState: StateFlow<SessionUiState> = authRepository.currentUser
        .map { user -> if (user == null) SessionUiState.SignedOut else SessionUiState.SignedIn(user) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), SessionUiState.Loading)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
