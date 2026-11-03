package com.tavola.domain.repository

import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.SignInResult
import kotlinx.coroutines.flow.Flow

/** Who is using the app. The real implementation (Firebase Auth) lives in `:data`. */
interface AuthRepository {
    /** Emits the signed-in person, or `null` when nobody is signed in, now and every time it changes. */
    val currentUser: Flow<AuthUser?>

    /** Signs in with the ID token that Google gave the app for the person's account. */
    suspend fun signInWithGoogle(idToken: String): SignInResult

    suspend fun signOut()
}
