package com.example.myapplication.data

/**
 * Data Models for CourtFinder Authentication, Court Search, and Booking.
 */

data class LoginRequest(
    val emailOrPhone: String,
    val passwordHash: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String
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
    val pricePerHour: String,
    val rating: Double,
    val availableTimeSlots: List<String>,
    val aiMatchScore: Int, // e.g. 95% AI Match
    val isFavorite: Boolean = false,
    val amenities: List<String> = listOf("💡 Floodlights", "🚗 Free Parking", "🚿 Showers", "💧 Water Station")
)

data class BookingConfirmation(
    val bookingId: String,
    val courtName: String,
    val timeSlot: String,
    val date: String
)
