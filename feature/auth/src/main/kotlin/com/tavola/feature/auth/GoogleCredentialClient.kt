package com.tavola.feature.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/** Shows the Google account sheet (Credential Manager) and turns what it answers into a [GoogleCredentialOutcome]. */
internal class GoogleCredentialClient(private val credentialManager: CredentialManager) {
    /** [context] has to be an Activity, because the sheet is drawn over it. [webClientId] is the web client of the Firebase project. */
    suspend fun requestIdToken(context: Context, webClientId: String): GoogleCredentialOutcome {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
            .build()
        return try {
            val credential = credentialManager.getCredential(context, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleCredentialOutcome.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleCredentialOutcome.Failed
            }
        } catch (cancelled: GetCredentialCancellationException) {
            GoogleCredentialOutcome.Cancelled
        } catch (noAccount: NoCredentialException) {
            GoogleCredentialOutcome.NoAccount
        } catch (failure: GetCredentialException) {
            GoogleCredentialOutcome.Failed
        } catch (malformed: GoogleIdTokenParsingException) {
            GoogleCredentialOutcome.Failed
        }
    }
}
