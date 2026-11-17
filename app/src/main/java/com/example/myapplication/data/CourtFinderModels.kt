package com.example.myapplication.data

/**
 * Data Models for CourtFinder Authentication, Court Search, and Booking.
 */

data class LoginRequest(
    val emailOrPhone: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String?,
    val user: User?,
    val errorMessage: String? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isGuest: Boolean = false
)

data class Court(
    val id: String,
    val name: String,
    val sportType: String, // e.g. Tennis, Badminton, Basketball, Padel
    val location: String,
    val distanceKm: Double,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pricePerHour: String,
    val rating: Double,
    val availableTimeSlots: List<String>,
    val aiMatchScore: Int, // retained for Room schema compatibility; not shown as a recommendation
    val isFavorite: Boolean = false,
    val hasAlertSet: Boolean = false,
    val amenities: List<String> = listOf("💡 Floodlights", "🚗 Free Parking", "🚿 Showers", "💧 Water Station")
)

data class BookingConfirmation(
    val bookingId: String,
    val courtName: String,
    val timeSlot: String,
    val date: String
)

data class BookingRequest(
    val courtId: String,
    val timeSlot: String
)

data class CourtFilterOptions(
    val onlyFavorites: Boolean = false,
    val minRating: Double = 0.0
)
