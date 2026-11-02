package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.BookingConfirmation
import com.example.myapplication.data.Court
import com.example.myapplication.data.MockCourtFinderApi
import com.example.myapplication.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppScreen {
    Splash,
    Auth,
    Home
}

enum class AuthMode {
    Login,
    SignUp
}

data class AuthUiState(
    val currentScreen: AppScreen = AppScreen.Splash,
    val authMode: AuthMode = AuthMode.Login,
    // Input Fields
    val emailOrPhoneInput: String = "",
    val passwordInput: String = "",
    val fullNameInput: String = "",
    val rememberMe: Boolean = true,
    val isPasswordVisible: Boolean = false,
    // Status
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null,
    // Home/Search AI state
    val searchAiQuery: String = "Find Tennis courts available near me at 6 PM",
    val searchResults: List<Court> = emptyList(),
    val isSearchingCourts: Boolean = false,
    // Interactive Booking & Favorites Feature
    val selectedCourtForBooking: Court? = null,
    val selectedTimeSlot: String? = null,
    val isBookingInProgress: Boolean = false,
    val bookingSuccessConfirmation: BookingConfirmation? = null,
    // Filter & Notification Feature
    val onlyFavoritesFilter: Boolean = false,
    val minRatingFilter: Double = 0.0,
    val alertToastMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val api = MockCourtFinderApi()

    fun navigateTo(screen: AppScreen) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun switchAuthMode(mode: AuthMode) {
        _uiState.value = _uiState.value.copy(
            authMode = mode,
            errorMessage = null
        )
    }

    fun onEmailOrPhoneChanged(input: String) {
        _uiState.value = _uiState.value.copy(emailOrPhoneInput = input, errorMessage = null)
    }

    fun onPasswordChanged(input: String) {
        _uiState.value = _uiState.value.copy(passwordInput = input, errorMessage = null)
    }

    fun onFullNameChanged(input: String) {
        _uiState.value = _uiState.value.copy(fullNameInput = input, errorMessage = null)
    }

    fun toggleRememberMe(checked: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = checked)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun submitAuth() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            if (state.authMode == AuthMode.Login) {
                val result = repository.login(state.emailOrPhoneInput, state.passwordInput)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loggedInUser = user,
                        currentScreen = AppScreen.Home
                    )
                    loadInitialCourts()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Login failed. Please check your credentials."
                    )
                }
            } else {
                val result = repository.register(
                    fullName = state.fullNameInput,
                    email = state.emailOrPhoneInput,
                    phone = state.emailOrPhoneInput,
                    password = state.passwordInput
                )
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loggedInUser = user,
                        currentScreen = AppScreen.Home
                    )
                    loadInitialCourts()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Registration failed."
                    )
                }
            }
        }
    }

    fun continueWithGoogle() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.loginWithGoogle()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loggedInUser = result.getOrNull(),
                    currentScreen = AppScreen.Home
                )
                loadInitialCourts()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google login failed."
                )
            }
        }
    }

    fun continueAsGuest() {
        repository.continueAsGuest()
        _uiState.value = _uiState.value.copy(
            loggedInUser = repository.currentUser.value,
            currentScreen = AppScreen.Home
        )
        loadInitialCourts()
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState(
            currentScreen = AppScreen.Auth,
            authMode = AuthMode.Login
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchAiQuery = query)
    }

    fun searchCourtsWithAI() {
        val query = _uiState.value.searchAiQuery
        _uiState.value = _uiState.value.copy(isSearchingCourts = true)
        viewModelScope.launch {
            val result = api.searchCourtsWithAI(query)
            _uiState.value = _uiState.value.copy(
                isSearchingCourts = false,
                searchResults = result.getOrDefault(emptyList())
            )
        }
    }

    fun toggleFavorite(courtId: String) {
        val updatedList = _uiState.value.searchResults.map { court ->
            if (court.id == courtId) {
                court.copy(isFavorite = !court.isFavorite)
            } else {
                court
            }
        }
        _uiState.value = _uiState.value.copy(searchResults = updatedList)
    }

    fun toggleAlert(courtId: String) {
        var alertMessage = ""
        val updatedList = _uiState.value.searchResults.map { court ->
            if (court.id == courtId) {
                val newAlert = !court.hasAlertSet
                alertMessage = if (newAlert) "🔔 Availability alert set for ${court.name}" else "Alert cancelled for ${court.name}"
                court.copy(hasAlertSet = newAlert)
            } else {
                court
            }
        }
        _uiState.value = _uiState.value.copy(
            searchResults = updatedList,
            alertToastMessage = alertMessage
        )
    }

    fun toggleFavoritesFilter() {
        _uiState.value = _uiState.value.copy(onlyFavoritesFilter = !_uiState.value.onlyFavoritesFilter)
    }

    fun toggleMinRatingFilter() {
        val newRating = if (_uiState.value.minRatingFilter == 0.0) 4.8 else 0.0
        _uiState.value = _uiState.value.copy(minRatingFilter = newRating)
    }

    fun dismissAlertToast() {
        _uiState.value = _uiState.value.copy(alertToastMessage = null)
    }

    fun openBookingDialog(court: Court) {
        _uiState.value = _uiState.value.copy(
            selectedCourtForBooking = court,
            selectedTimeSlot = court.availableTimeSlots.firstOrNull()
        )
    }

    fun selectTimeSlot(timeSlot: String) {
        _uiState.value = _uiState.value.copy(selectedTimeSlot = timeSlot)
    }

    fun dismissBookingDialog() {
        _uiState.value = _uiState.value.copy(
            selectedCourtForBooking = null,
            selectedTimeSlot = null
        )
    }

    fun confirmBooking() {
        val court = _uiState.value.selectedCourtForBooking ?: return
        val slot = _uiState.value.selectedTimeSlot ?: return

        _uiState.value = _uiState.value.copy(isBookingInProgress = true)
        viewModelScope.launch {
            val result = api.bookCourt(court.id, slot)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isBookingInProgress = false,
                    selectedCourtForBooking = null,
                    bookingSuccessConfirmation = result.getOrNull()
                )
            }
        }
    }

    fun dismissBookingConfirmation() {
        _uiState.value = _uiState.value.copy(bookingSuccessConfirmation = null)
    }

    private fun loadInitialCourts() {
        searchCourtsWithAI()
    }
}
