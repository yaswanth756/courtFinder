package com.example.courtfinder.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

data class VerifiedGoogleAccount(
    val subject: String,
    val email: String,
    val name: String?,
    val firebaseUid: String? = null
)

/** Verifies Firebase signatures/claims and accepts only accounts authenticated by Google. */
class FirebaseGoogleTokenVerifier(private val firebaseAuth: FirebaseAuth) {
    fun verify(idToken: String): VerifiedGoogleAccount? {
        val decoded = try {
            // Revocation checking is required here because a CourtFinder session is
            // minted from this assertion; fail closed if Firebase has revoked it.
            firebaseAuth.verifyIdToken(idToken, true)
        } catch (_: FirebaseAuthException) {
            return null
        } catch (_: IllegalArgumentException) {
            return null
        }

        val claims = decoded.claims
        val firebaseClaims = claims["firebase"] as? Map<*, *> ?: return null
        if (firebaseClaims["sign_in_provider"] != "google.com") return null
        if (claims["email_verified"] != true) return null

        val email = (claims["email"] as? String)?.takeIf { it.isNotBlank() } ?: return null
        val googleIdentities = firebaseClaims["identities"] as? Map<*, *>
        val googleSubject = (googleIdentities?.get("google.com") as? List<*>)
            ?.firstOrNull() as? String ?: return null
        val subject = googleSubject.takeIf { it.isNotBlank() } ?: return null
        return VerifiedGoogleAccount(subject, email, claims["name"] as? String, decoded.uid)
    }
}
