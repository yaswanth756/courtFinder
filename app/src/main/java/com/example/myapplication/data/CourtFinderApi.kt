package com.example.myapplication.data

import kotlinx.coroutines.delay

/**
 * CourtFinder API Service Interface.
 *
 * NOTE FOR BACKEND INTEGRATION:
 * Replace MockCourtFinderApi with actual HTTP client implementation (Retrofit / Ktor / Firebase).
 */
interface CourtFinderApi {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun loginWithGoogle(idToken: String): Result<AuthResponse>
    suspend fun searchCourtsWithAI(query: String): Result<List<Court>>
}

/**
 * Mock API Implementation with realistic artificial delays and response placeholders.
 */
class MockCourtFinderApi : CourtFinderApi {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        // Simulating network delay for backend auth API
        delay(1200)

        return if (request.emailOrPhone.contains("@") || request.emailOrPhone.length >= 10) {
            if (request.passwordHash.length >= 6) {
                Result.success(
                    AuthResponse(
                        success = true,
                        token = "mock_jwt_token_courtfinder_12345",
                        user = User(
                            id = "user_101",
                            name = if (request.emailOrPhone.contains("@")) request.emailOrPhone.substringBefore("@") else "Court Player",
                            email = request.emailOrPhone
                        )
                    )
                )
            } else {
                Result.failure(Exception("Password must be at least 6 characters."))
            }
        } else {
            Result.failure(Exception("Invalid email or phone number."))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        // Simulating network delay for registration API
        delay(1500)

        if (request.fullName.isBlank()) {
            return Result.failure(Exception("Full name is required."))
        }
        if (!request.email.contains("@")) {
            return Result.failure(Exception("Please enter a valid email address."))
        }
        if (request.passwordHash.length < 6) {
            return Result.failure(Exception("Password must be at least 6 characters."))
        }

        return Result.success(
            AuthResponse(
                success = true,
                token = "mock_jwt_token_courtfinder_new_user",
                user = User(
                    id = "user_${System.currentTimeMillis()}",
                    name = request.fullName,
                    email = request.email
                )
            )
        )
    }

    override suspend fun loginWithGoogle(idToken: String): Result<AuthResponse> {
        delay(1000)
        return Result.success(
            AuthResponse(
                success = true,
                token = "google_auth_token_sample",
                user = User(
                    id = "google_user_001",
                    name = "Google Sports User",
                    email = "user@google.com"
                )
            )
        )
    }

    override suspend fun searchCourtsWithAI(query: String): Result<List<Court>> {
        delay(1000)
        val sampleCourts = listOf(
            Court(
                id = "c1",
                name = "Apex Tennis Arena & Academy",
                sportType = "Tennis",
                location = "Downtown Sports Hub, Sector 4",
                distanceKm = 1.8,
                pricePerHour = "$25/hr",
                rating = 4.9,
                availableTimeSlots = listOf("06:00 PM", "07:00 PM", "09:00 PM"),
                aiMatchScore = 98
            ),
            Court(
                id = "c2",
                name = "Smash Badminton Indoor Club",
                sportType = "Badminton",
                location = "Eastside Fitness Park",
                distanceKm = 3.2,
                pricePerHour = "$18/hr",
                rating = 4.7,
                availableTimeSlots = listOf("05:30 PM", "08:00 PM"),
                aiMatchScore = 94
            ),
            Court(
                id = "c3",
                name = "Urban Hoops Basketball Arena",
                sportType = "Basketball",
                location = "Central Stadium Complex",
                distanceKm = 4.5,
                pricePerHour = "$30/hr",
                rating = 4.8,
                availableTimeSlots = listOf("07:00 PM", "08:30 PM"),
                aiMatchScore = 91
            ),
            Court(
                id = "c4",
                name = "Pro Padel Court & Lounge",
                sportType = "Padel",
                location = "Green Valley Sports Club",
                distanceKm = 5.0,
                pricePerHour = "$35/hr",
                rating = 4.9,
                availableTimeSlots = listOf("06:00 PM", "07:30 PM"),
                aiMatchScore = 89
            )
        )
        return Result.success(sampleCourts)
    }
}
