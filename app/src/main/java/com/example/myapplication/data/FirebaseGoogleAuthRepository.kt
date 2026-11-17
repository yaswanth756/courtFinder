package com.example.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/** Firebase session operations used for Google authentication; backend sessions remain separate. */
class FirebaseGoogleAuthRepository(private val firebaseAuth: FirebaseAuth) {
    suspend fun signInWithGoogleIdToken(googleIdToken: String): String {
        require(googleIdToken.isNotBlank()) { "Google did not return an ID token." }

        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        val signInResult = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = signInResult.user
            ?: throw IllegalStateException("Firebase did not return a signed-in user.")

        return firebaseUser.getIdToken(true).await().token
            ?: throw IllegalStateException("Firebase did not return an ID token.")
    }

    suspend fun currentIdToken(): String? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return currentUser.getIdToken(false).await().token
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
