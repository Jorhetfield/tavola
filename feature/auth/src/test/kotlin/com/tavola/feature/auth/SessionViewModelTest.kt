package com.tavola.feature.auth

import app.cash.turbine.test
import com.tavola.core.testing.fake.FakeAuthRepository
import com.tavola.core.testing.rule.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SessionViewModelTest {
    @JvmField
    @RegisterExtension
    val mainDispatcher = MainDispatcherExtension()

    @Test
    fun `nobody signed in is a signed out session`() = runTest {
        val viewModel = SessionViewModel(FakeAuthRepository())

        viewModel.uiState.test {
            assertEquals(SessionUiState.SignedOut, expectMostRecentItem())
        }
    }

    @Test
    fun `a stored session is signed in without asking again`() = runTest {
        val viewModel = SessionViewModel(FakeAuthRepository(initialUser = FakeAuthRepository.SampleUser))

        viewModel.uiState.test {
            assertEquals(SessionUiState.SignedIn(FakeAuthRepository.SampleUser), expectMostRecentItem())
        }
    }

    @Test
    fun `signing in and signing out are followed by the session`() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SessionViewModel(repository)

        viewModel.uiState.test {
            assertEquals(SessionUiState.SignedOut, expectMostRecentItem())

            repository.signInWithGoogle("token")
            assertEquals(SessionUiState.SignedIn(FakeAuthRepository.SampleUser), awaitItem())

            repository.signOut()
            assertEquals(SessionUiState.SignedOut, awaitItem())
        }
    }
}
