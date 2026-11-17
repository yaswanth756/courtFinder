package com.example.myapplication.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf

class AuthRepository(
    private val api: CourtFinderApi = MockCourtFinderApi(),
    private val courtDao: CourtDao? = null,
    private val firebaseGoogleAuth: FirebaseGoogleAuthRepository? = null
) {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun restoreSession(): Result<User?> {
        val result = api.getCurrentUser()
        val restoredUser = result.getOrNull()
        if (restoredUser != null) {
            _currentUser.value = restoredUser
            return Result.success(restoredUser)
        }
        if (result.isFailure) api.clearSession()

        val firebaseToken = firebaseGoogleAuth?.currentIdToken()
        if (firebaseToken != null) {
            val backendLogin = loginWithGoogle(firebaseToken)
            if (backendLogin.isSuccess) return backendLogin.map { it as User? }
            return Result.failure(
                backendLogin.exceptionOrNull() ?: result.exceptionOrNull()
                ?: IllegalStateException("Could not restore the CourtFinder session.")
            )
        }

        _currentUser.value = null
        return if (result.isSuccess) Result.success(null) else result
    }

    fun observeCourts(): Flow<List<Court>> {
        return courtDao?.getAllCourts()?.map { courts -> courts.map { it.toCourt() } }
            ?: flowOf(emptyList())
    }

    suspend fun searchCourts(query: String): Result<List<Court>> {
        val apiResult = api.searchCourts(query)
        if (apiResult.isSuccess) {
            val savedCourts = courtDao?.getAllCourtsOnce().orEmpty().associateBy { it.id }
            val updatedCourts = apiResult.getOrDefault(emptyList()).map { court ->
                val savedCourt = savedCourts[court.id]
                court.copy(
                    isFavorite = savedCourt?.isFavorite ?: court.isFavorite,
                    hasAlertSet = savedCourt?.hasAlertSet ?: court.hasAlertSet
                )
            }
            courtDao?.replaceCourts(updatedCourts.map { it.toEntity() })
            return Result.success(updatedCourts)
        }

        val cachedCourts = courtDao?.getAllCourtsOnce().orEmpty().map { it.toCourt() }
        if (cachedCourts.isNotEmpty()) {
            val sport = listOf("Tennis", "Badminton", "Basketball", "Padel", "Pickleball")
                .firstOrNull { query.contains(it, ignoreCase = true) }
            val matchingCourts = if (sport == null) cachedCourts else cachedCourts.filter {
                it.sportType.equals(sport, ignoreCase = true)
            }
            return Result.success(matchingCourts)
        }

        return Result.failure(apiResult.exceptionOrNull() ?: Exception("Search failed."))
    }

    suspend fun setFavorite(courtId: String, isFavorite: Boolean) {
        courtDao?.updateFavorite(courtId, isFavorite)
    }

    suspend fun setAvailabilityAlert(courtId: String, hasAlertSet: Boolean) {
        courtDao?.updateAlert(courtId, hasAlertSet)
    }

    suspend fun bookCourt(courtId: String, timeSlot: String): Result<BookingConfirmation> {
        return api.bookCourt(courtId, timeSlot)
    }

    suspend fun deleteAccount(): Result<Unit> {
        val result = api.deleteAccount()
        if (result.isSuccess) {
            _currentUser.value = null
            api.clearSession()
            firebaseGoogleAuth?.signOut()
            courtDao?.clearCourts()
        }
        return result
    }

    suspend fun login(emailOrPhone: String, password: String): Result<User> {
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

    suspend fun loginWithGoogle(idToken: String): Result<User> {
        val result = api.loginWithGoogle(idToken)
        return if (result.isSuccess) {
            val response = result.getOrNull()
            if (response?.success == true && response.user != null) {
                _currentUser.value = response.user
                Result.success(response.user)
            } else {
                Result.failure(IllegalStateException(response?.errorMessage ?: "Google sign-in failed."))
            }
        } else {
            Result.failure(result.exceptionOrNull() ?: IllegalStateException("Google sign-in failed."))
        }
    }

    fun continueAsGuest() {
        firebaseGoogleAuth?.signOut()
        _currentUser.value = User(
            id = "guest_${System.currentTimeMillis()}",
            name = "Guest Player",
            email = "guest@courtfinder.ai",
            isGuest = true
        )
    }

    fun logout() {
        _currentUser.value = null
        api.clearSession()
        firebaseGoogleAuth?.signOut()
    }
}
