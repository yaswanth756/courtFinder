package com.example.courtfinder.backend

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class CourtDatabaseAuthTest {
    private lateinit var tempDirectory: Path
    private lateinit var database: CourtDatabase

    @Before
    fun setUp() {
        tempDirectory = Files.createTempDirectory("court-finder-auth-test")
        database = CourtDatabase(tempDirectory.resolve("courtfinder.db").toString(), seedDemoCourts = false)
    }

    @After
    fun tearDown() {
        Files.walk(tempDirectory).use { paths ->
            paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists)
        }
    }

    @Test
    fun firebaseUidResolvesToSameCourtFinderUserOnRepeatLogin() = runBlocking {
        val first = database.loginWithFirebaseGoogle(
            firebaseUid = "firebase-user-123",
            googleSubject = "google-subject-123",
            email = "player@example.com",
            name = "Court Player"
        )
        val second = database.loginWithFirebaseGoogle(
            firebaseUid = "firebase-user-123",
            googleSubject = "google-subject-123",
            email = "player@example.com",
            name = "Updated Name"
        )

        assertEquals(first.id, second.id)
        assertEquals("player@example.com", second.email)
    }

    @Test
    fun firebaseLoginReusesExistingLegacyGoogleAccount() = runBlocking {
        val legacyUser = database.loginWithGoogle(
            subject = "existing-google-subject",
            email = "legacy@example.com",
            name = "Legacy Player"
        )

        val firebaseUser = database.loginWithFirebaseGoogle(
            firebaseUid = "new-firebase-uid",
            googleSubject = "existing-google-subject",
            email = "legacy@example.com",
            name = "Legacy Player"
        )

        assertEquals(legacyUser.id, firebaseUser.id)
    }

    @Test
    fun emailPasswordRegistrationAndLoginRemainAvailable() = runBlocking {
        val registered = database.register(
            RegisterRequestDto(
                fullName = "Email Player",
                email = "email@example.com",
                password = "secure-test-password"
            )
        )
        val loggedIn = database.login(LoginRequestDto("email@example.com", "secure-test-password"))

        assertEquals(registered.id, loggedIn.id)
    }

    @Test
    fun deletingAccountRemovesFirebaseLink() = runBlocking {
        val user = database.loginWithFirebaseGoogle(
            firebaseUid = "firebase-user-to-delete",
            googleSubject = "google-subject-to-delete",
            email = "delete@example.com",
            name = "Delete Me"
        )
        database.deleteAccount(user.id)

        val recreated = database.loginWithFirebaseGoogle(
            firebaseUid = "firebase-user-to-delete",
            googleSubject = "google-subject-to-delete",
            email = "delete@example.com",
            name = "Delete Me"
        )

        org.junit.Assert.assertNotEquals(user.id, recreated.id)
    }
}
