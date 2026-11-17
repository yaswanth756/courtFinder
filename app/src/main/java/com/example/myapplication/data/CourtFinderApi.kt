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
    suspend fun searchCourts(query: String): Result<List<Court>>
    suspend fun bookCourt(courtId: String, timeSlot: String): Result<BookingConfirmation>
    suspend fun getCurrentUser(): Result<User?> = Result.success(null)
    suspend fun deleteAccount(): Result<Unit>
    fun clearSession() = Unit
}

/**
 * Mock API Implementation with realistic artificial delays and response placeholders.
 */
class MockCourtFinderApi : CourtFinderApi {

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        delay(1200)

        return if (request.emailOrPhone.contains("@") || request.emailOrPhone.length >= 10) {
            if (request.password.length >= 10) {
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
                Result.failure(Exception("Password must be at least 10 characters."))
            }
        } else {
            Result.failure(Exception("Invalid email or phone number."))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        delay(1500)

        if (request.fullName.isBlank()) {
            return Result.failure(Exception("Full name is required."))
        }
        if (!request.email.contains("@")) {
            return Result.failure(Exception("Please enter a valid email address."))
        }
        if (request.password.length < 10) {
            return Result.failure(Exception("Password must be at least 10 characters."))
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
        return Result.failure(IllegalStateException("Google sign-in is not configured for the mock service."))
    }

    override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)

    override suspend fun searchCourts(query: String): Result<List<Court>> {
        delay(800)
        val sampleCourts = listOf(
            Court(
                id = "c1",
                name = "Apex Tennis Arena & Academy",
                sportType = "Tennis",
                location = "Downtown Sports Hub, Sector 4",
                distanceKm = 1.8,
                latitude = 37.7749,
                longitude = -122.4194,
                pricePerHour = "$25/hr",
                rating = 4.9,
                availableTimeSlots = listOf("06:00 PM", "07:00 PM", "09:00 PM"),
                aiMatchScore = 98,
                isFavorite = true,
                amenities = listOf("💡 LED Floodlights", "🚗 Free Parking", "🚿 Changing Rooms", "🎾 Racket Rental")
            ),
            Court(
                id = "c2",
                name = "Smash Badminton Indoor Club",
                sportType = "Badminton",
                location = "Eastside Fitness Park",
                distanceKm = 3.2,
                latitude = 37.7849,
                longitude = -122.4094,
                pricePerHour = "$18/hr",
                rating = 4.7,
                availableTimeSlots = listOf("05:30 PM", "08:00 PM"),
                aiMatchScore = 94,
                isFavorite = false,
                amenities = listOf("❄️ Air Conditioned", "👟 Shoe Rental", "💧 Hydration Station")
            ),
            Court(
                id = "c3",
                name = "Urban Hoops Basketball Arena",
                sportType = "Basketball",
                location = "Central Stadium Complex",
                distanceKm = 4.5,
                latitude = 37.7649,
                longitude = -122.4294,
                pricePerHour = "$30/hr",
                rating = 4.8,
                availableTimeSlots = listOf("07:00 PM", "08:30 PM"),
                aiMatchScore = 91,
                isFavorite = false,
                amenities = listOf("🔊 Sound System", "💡 Night Lights", "🚿 Lockers & Showers")
            ),
            Court(
                id = "c4",
                name = "Pro Padel Court & Lounge",
                sportType = "Padel",
                location = "Green Valley Sports Club",
                distanceKm = 5.0,
                latitude = 37.7549,
                longitude = -122.4394,
                pricePerHour = "$35/hr",
                rating = 4.9,
                availableTimeSlots = listOf("06:00 PM", "07:30 PM"),
                aiMatchScore = 89,
                isFavorite = true,
                amenities = listOf("☕ Cafe Lounge", "🎾 Glass Panoramic Courts", "🚗 Valet Parking")
            ),
            Court(
                id = "c5",
                name = "Rally Pickleball Center",
                sportType = "Pickleball",
                location = "Westside Community Sports Park",
                distanceKm = 2.6,
                latitude = 37.7949,
                longitude = -122.3994,
                pricePerHour = "$22/hr",
                rating = 4.6,
                availableTimeSlots = listOf("05:00 PM", "06:30 PM", "08:00 PM"),
                aiMatchScore = 92,
                amenities = listOf("Court Lighting", "Free Parking", "Water Station")
            )
        )

        // Filter based on query if relevant
        val filtered = if (query.contains("Tennis", ignoreCase = true)) {
            sampleCourts.filter { it.sportType == "Tennis" }
        } else if (query.contains("Badminton", ignoreCase = true)) {
            sampleCourts.filter { it.sportType == "Badminton" }
        } else if (query.contains("Basketball", ignoreCase = true)) {
            sampleCourts.filter { it.sportType == "Basketball" }
        } else if (query.contains("Padel", ignoreCase = true)) {
            sampleCourts.filter { it.sportType == "Padel" }
        } else if (query.contains("Pickleball", ignoreCase = true)) {
            sampleCourts.filter { it.sportType == "Pickleball" }
        } else {
            sampleCourts
        }

        return Result.success(filtered)
    }

    override suspend fun bookCourt(courtId: String, timeSlot: String): Result<BookingConfirmation> {
        delay(1200)
        return Result.success(
            BookingConfirmation(
                bookingId = "BK-${(1000..9999).random()}",
                courtName = "Court #$courtId",
                timeSlot = timeSlot,
                date = "Today"
            )
        )
    }
}
