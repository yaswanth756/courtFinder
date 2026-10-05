package com.example.myapplication.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val api: CourtFinderApi = MockCourtFinderApi()
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun login(emailOrPhone: String, password: String): Result<User> {
        // TODO: Placeholder for real API endpoint authentication
        val result = api.login(LoginRequest(emailOrPhone, password))
        return if (result.isSuccess) {
            val response = result.getOrNull()
            if (response?.success == true && response.user != null) {
                _currentUser.value = response.user
                Result.success(response.user)
            } else {
                Result.failure(Exception(response?.errorMessage ?: "Login failed."))
            }
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Network error during login."))
        }
    }

    suspend fun register(fullName: String, email: String, phone: String, password: String): Result<User> {
        // TODO: Placeholder for real API endpoint registration
        val result = api.register(RegisterRequest(fullName, email, phone, password))
        return if (result.isSuccess) {
            val response = result.getOrNull()
            if (response?.success == true && response.user != null) {
                _currentUser.value = response.user
                Result.success(response.user)
            } else {
                Result.failure(Exception(response?.errorMessage ?: "Registration failed."))
            }
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Network error during registration."))
        }
    }

    suspend fun loginWithGoogle(): Result<User> {
        // TODO: Integrate Google OneTap / Credential Manager SDK
        val result = api.loginWithGoogle("sample_google_token")
        return if (result.isSuccess && result.getOrNull()?.user != null) {
            val user = result.getOrNull()!!.user!!
            _currentUser.value = user
            Result.success(user)
        } else {
            Result.failure(Exception("Google Sign-In failed."))
        }
    }

    fun continueAsGuest() {
        _currentUser.value = User(
            id = "guest_${System.currentTimeMillis()}",
            name = "Guest Player",
            email = "guest@courtfinder.ai",
            isGuest = true
        )
    }

    fun logout() {
        _currentUser.value = null
    }
}
