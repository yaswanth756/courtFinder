package com.example.myapplication.data

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInManager(private val activity: Activity) {
    private val credentialManager = CredentialManager.create(activity)

    suspend fun getIdToken(webClientId: String): String {
        require(webClientId.isNotBlank()) {
            "Google sign-in is not configured. Check the Firebase Android app and Web OAuth client configuration."
        }
        val googleOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
        val regularRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleOption)
            .build()

        val credential = try {
            credentialManager.getCredential(activity, regularRequest).credential
        } catch (noCredential: NoCredentialException) {
            // The bottom-sheet flow can have no eligible credentials (for example,
            // a new emulator or an account requiring reauthentication). The explicit
            // Google button flow can let the user add or reauthenticate an account.
            val signInOption = GetSignInWithGoogleOption.Builder(webClientId).build()
            val buttonRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()
            try {
                credentialManager.getCredential(activity, buttonRequest).credential
            } catch (_: NoCredentialException) {
                throw IllegalStateException(
                    "No Google account is available. Add a Google account to this device or emulator, then try again.",
                    noCredential
                )
            }
        }
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Google did not return a supported sign-in credential.")
        }
        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}
