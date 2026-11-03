package com.tavola.core.testing.fake

import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.SignInResult
import com.tavola.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [AuthRepository] for tests. Starts signed out (or as [initialUser]); [signInResult] is what the next sign-in
 * answers, and [signedInWith] records the tokens it was asked with.
 */
class FakeAuthRepository(initialUser: AuthUser? = null, var signInResult: SignInResult = SignInResult.Success(SampleUser)) :
    AuthRepository {
    private val user = MutableStateFlow(initialUser)

    /** The ID tokens of the sign-ins that were asked for, in order. */
    val signedInWith = mutableListOf<String>()

    override val currentUser: Flow<AuthUser?> = user

    override suspend fun signInWithGoogle(idToken: String): SignInResult {
        signedInWith += idToken
        val result = signInResult
        if (result is SignInResult.Success) user.value = result.user
        return result
    }

    override suspend fun signOut() {
        user.value = null
    }

    companion object {
        val SampleUser = AuthUser(id = "user-marta", displayName = "Marta Ruiz", email = "marta@example.com", photoUrl = null)
    }
}
