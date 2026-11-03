package com.tavola.data.auth

import com.google.firebase.FirebaseNetworkException
import com.tavola.domain.model.SignInFailure
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SignInFailureTest {
    @Test
    fun `a lost connection is reported as a network failure`() {
        assertEquals(SignInFailure.Network, FirebaseNetworkException("A network error").toSignInFailure())
    }

    @Test
    fun `any other error is reported as a generic failure`() {
        assertEquals(SignInFailure.Other, IllegalStateException("invalid credential").toSignInFailure())
    }
}
