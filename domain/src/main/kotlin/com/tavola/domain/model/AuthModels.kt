package com.tavola.domain.model

/** The signed-in person. [displayName] and [photoUrl] come from their Google account and may be missing. */
data class AuthUser(val id: String, val displayName: String?, val email: String?, val photoUrl: String?)

/** Why a sign-in did not work, as far as the person can do something about it. */
enum class SignInFailure {
    /** There is no connection (or it dropped): trying again later may work. */
    Network,

    /** Anything else; the details are not shown to the person. */
    Other,
}

sealed interface SignInResult {
    data class Success(val user: AuthUser) : SignInResult

    data class Failure(val reason: SignInFailure) : SignInResult
}
