package com.tavola.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.tavola.domain.model.AuthUser
import com.tavola.domain.model.SignInFailure
import com.tavola.domain.model.SignInResult
import com.tavola.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

/** Sign-in with Firebase Auth. The listener emits the stored session at once, so a person who signed in before is back without a network. */
@Singleton
internal class FirebaseAuthRepository @Inject constructor(private val firebaseAuth: FirebaseAuth) : AuthRepository {
    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser?.toDomain()) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun signInWithGoogle(idToken: String): SignInResult = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = firebaseAuth.signInWithCredential(credential).await().user
        if (user == null) SignInResult.Failure(SignInFailure.Other) else SignInResult.Success(user.toDomain())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        SignInResult.Failure(failure.toSignInFailure())
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }
}

private fun FirebaseUser.toDomain() = AuthUser(id = uid, displayName = displayName, email = email, photoUrl = photoUrl?.toString())

/** A lost connection is the one failure the person can fix; the rest is reported as a generic one. */
internal fun Throwable.toSignInFailure(): SignInFailure =
    if (this is FirebaseNetworkException) SignInFailure.Network else SignInFailure.Other
