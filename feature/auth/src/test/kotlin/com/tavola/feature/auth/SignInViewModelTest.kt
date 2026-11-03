package com.tavola.feature.auth

import app.cash.turbine.test
import com.tavola.core.testing.fake.FakeAuthRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import com.tavola.domain.model.SignInFailure
import com.tavola.domain.model.SignInResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SignInViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    private val repository = FakeAuthRepository()
    private val viewModel = SignInViewModel(repository)

    @Test
    fun `starts idle with no error`() {
        assertEquals(SignInUiState(), viewModel.uiState.value)
    }

    @Test
    fun `a Google token signs in through the repository and ends idle`() = runTest {
        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Token("google-token"))

        assertEquals(listOf("google-token"), repository.signedInWith)
        assertEquals(SignInUiState(isSigningIn = false, error = null), viewModel.uiState.value)
    }

    @Test
    fun `while the token is checked the screen is signing in`() = runTest {
        viewModel.uiState.test {
            assertEquals(SignInUiState(), awaitItem())

            viewModel.onCredentialOutcome(GoogleCredentialOutcome.Token("google-token"))

            assertEquals(SignInUiState(isSigningIn = true), awaitItem())
            assertEquals(SignInUiState(isSigningIn = false), awaitItem())
        }
    }

    @Test
    fun `a failed sign in shows a network error or a generic one`() = runTest {
        repository.signInResult = SignInResult.Failure(SignInFailure.Network)
        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Token("token"))
        assertEquals(SignInError.Network, viewModel.uiState.value.error)

        repository.signInResult = SignInResult.Failure(SignInFailure.Other)
        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Token("token"))
        assertEquals(SignInError.Other, viewModel.uiState.value.error)
    }

    @Test
    fun `a new attempt clears the previous error`() = runTest {
        repository.signInResult = SignInResult.Failure(SignInFailure.Network)
        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Token("token"))
        repository.signInResult = SignInResult.Success(FakeAuthRepository.SampleUser)

        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Token("token"))

        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `closing the Google sheet is not an error and asks nothing of the server`() = runTest {
        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Cancelled)

        assertEquals(SignInUiState(), viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.signedInWith)
    }

    @Test
    fun `no Google account on the phone and an unexpected answer are different errors`() = runTest {
        viewModel.onCredentialOutcome(GoogleCredentialOutcome.NoAccount)
        assertEquals(SignInError.NoGoogleAccount, viewModel.uiState.value.error)

        viewModel.onCredentialOutcome(GoogleCredentialOutcome.Failed)
        assertEquals(SignInError.Other, viewModel.uiState.value.error)
        assertEquals(emptyList<String>(), repository.signedInWith)
    }
}
