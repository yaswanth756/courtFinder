package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.BookingConfirmation
import com.example.myapplication.data.Court
import com.example.myapplication.data.DeviceLocation
import com.example.myapplication.data.LocationRepository
import com.example.myapplication.data.User
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CancellationException
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
    val phoneInput: String = "",
    val passwordInput: String = "",
    val fullNameInput: String = "",
    val rememberMe: Boolean = true,
    val isPasswordVisible: Boolean = false,
    // Status
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null,
    // Home/search state
    val searchAiQuery: String = "Find Tennis courts available near me at 6 PM",
    val searchResults: List<Court> = emptyList(),
    val isSearchingCourts: Boolean = false,
    val searchErrorMessage: String? = null,
    // Interactive Booking & Favorites Feature
    val selectedCourtForBooking: Court? = null,
    val selectedTimeSlot: String? = null,
    val isBookingInProgress: Boolean = false,
    val bookingSuccessConfirmation: BookingConfirmation? = null,
    val bookingErrorMessage: String? = null,
    // Filter & Notification Feature
    val onlyFavoritesFilter: Boolean = false,
    val minRatingFilter: Double = 0.0,
    val alertToastMessage: String? = null,
    val currentLocation: DeviceLocation? = null,
    val locationAddress: String? = null,
    val isFindingLocation: Boolean = false,
    val locationErrorMessage: String? = null,
    val isDeletingAccount: Boolean = false,
    val accountDeletionError: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository(),
    private val locationRepository: LocationRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCourts().collect { courts ->
                _uiState.value = _uiState.value.copy(searchResults = courts)
            }
        }
        viewModelScope.launch {
            runCatchingCancellable { repository.restoreSession() }
                .onSuccess { result ->
                    val user = result.getOrNull()
                    if (result.isSuccess && user != null) {
                        _uiState.value = _uiState.value.copy(
                            currentScreen = AppScreen.Home,
                            loggedInUser = user
                        )
                        loadInitialCourts()
                    }
                }
        }
    }

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

    fun onPhoneChanged(input: String) {
        _uiState.value = _uiState.value.copy(phoneInput = input, errorMessage = null)
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
                val result = runCatchingCancellable {
                    repository.login(state.emailOrPhoneInput, state.passwordInput)
                }.getOrElse { Result.failure(it) }
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
                val result = runCatchingCancellable {
                    repository.register(
                        fullName = state.fullNameInput,
                        email = state.emailOrPhoneInput,
                        phone = state.phoneInput,
                        password = state.passwordInput
                    )
                }.getOrElse { Result.failure(it) }
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

    fun beginGoogleSignIn() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
    }

    fun continueWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = runCatchingCancellable { repository.loginWithGoogle(idToken) }
                .getOrElse { Result.failure(it) }
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
                    errorMessage = result.exceptionOrNull()?.message ?: "Google login failed."
                )
            }
        }
    }

    fun showGoogleSignInError(message: String) {
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = message)
    }

    fun cancelGoogleSignIn() {
        _uiState.value = _uiState.value.copy(isLoading = false)
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

    fun deleteAccount() {
        val user = _uiState.value.loggedInUser
        if (user == null) return
        if (user.isGuest) {
            logout()
            return
        }
        _uiState.value = _uiState.value.copy(isDeletingAccount = true, accountDeletionError = null)
        viewModelScope.launch {
            runCatchingCancellable { repository.deleteAccount() }
                .fold(
                    onSuccess = { result ->
                        if (result.isSuccess) {
                            repository.logout()
                            _uiState.value = AuthUiState(currentScreen = AppScreen.Auth)
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isDeletingAccount = false,
                                accountDeletionError = result.exceptionOrNull()?.message ?: "Account deletion failed. Please try again."
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isDeletingAccount = false,
                            accountDeletionError = error.message ?: "Account deletion failed. Please try again."
                        )
                    }
                )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchAiQuery = query)
    }

    fun searchCourts() {
        val query = _uiState.value.searchAiQuery
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchErrorMessage = "Enter a search to find courts.")
            return
        }
        _uiState.value = _uiState.value.copy(isSearchingCourts = true, searchErrorMessage = null)
        viewModelScope.launch {
            runCatchingCancellable { repository.searchCourts(query) }
                .fold(
                    onSuccess = { result ->
                        _uiState.value = _uiState.value.copy(
                            isSearchingCourts = false,
                            searchResults = result.getOrElse { emptyList() },
                            searchErrorMessage = result.exceptionOrNull()?.message
                                ?: if (result.getOrNull().isNullOrEmpty()) "No courts matched that search." else null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSearchingCourts = false,
                            searchErrorMessage = error.message ?: "Search failed. Please try again."
                        )
                    }
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
        val updatedCourt = updatedList.firstOrNull { it.id == courtId } ?: return
        viewModelScope.launch {
            repository.setFavorite(courtId, updatedCourt.isFavorite)
        }
    }

    fun toggleAlert(courtId: String) {
        var alertMessage = ""
        val updatedList = _uiState.value.searchResults.map { court ->
            if (court.id == courtId) {
                val newAlert = !court.hasAlertSet
                alertMessage = if (newAlert) "ðŸ”” Availability alert set for ${court.name}" else "Alert cancelled for ${court.name}"
                court.copy(hasAlertSet = newAlert)
            } else {
                court
            }
        }
        _uiState.value = _uiState.value.copy(
            searchResults = updatedList,
            alertToastMessage = alertMessage
        )
        val updatedCourt = updatedList.firstOrNull { it.id == courtId } ?: return
        viewModelScope.launch {
            repository.setAvailabilityAlert(courtId, updatedCourt.hasAlertSet)
        }
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

    fun findCurrentLocation() {
        val locationSource = locationRepository
        if (locationSource == null) {
            _uiState.value = _uiState.value.copy(locationErrorMessage = "Location is not configured.")
            return
        }

        _uiState.value = _uiState.value.copy(isFindingLocation = true, locationErrorMessage = null)
        viewModelScope.launch {
            runCatchingCancellable { locationSource.getCurrentLocation() }
                .fold(
                    onSuccess = { location ->
                        val address = runCatchingCancellable {
                            locationSource.reverseGeocode(location)
                        }.getOrNull()
                        _uiState.value = _uiState.value.copy(
                            currentLocation = location,
                            locationAddress = address,
                            isFindingLocation = false,
                            locationErrorMessage = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isFindingLocation = false,
                            locationErrorMessage = error.message ?: "Could not find your location."
                        )
                    }
                )
        }
    }

    fun openBookingDialog(court: Court) {
        _uiState.value = _uiState.value.copy(
            selectedCourtForBooking = court,
            selectedTimeSlot = court.availableTimeSlots.firstOrNull(),
            bookingErrorMessage = null
        )
    }

    fun selectTimeSlot(timeSlot: String) {
        _uiState.value = _uiState.value.copy(selectedTimeSlot = timeSlot)
    }

    fun dismissBookingDialog() {
        _uiState.value = _uiState.value.copy(
            selectedCourtForBooking = null,
            selectedTimeSlot = null,
            bookingErrorMessage = null,
            isBookingInProgress = false
        )
    }

    fun confirmBooking() {
        val court = _uiState.value.selectedCourtForBooking ?: return
        val slot = _uiState.value.selectedTimeSlot ?: return

        _uiState.value = _uiState.value.copy(isBookingInProgress = true, bookingErrorMessage = null)
        viewModelScope.launch {
            runCatchingCancellable { repository.bookCourt(court.id, slot) }
                .fold(
                    onSuccess = { result ->
                        if (result.isSuccess) {
                            _uiState.value = _uiState.value.copy(
                                isBookingInProgress = false,
                                selectedCourtForBooking = null,
                                bookingSuccessConfirmation = result.getOrNull(),
                                bookingErrorMessage = null
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isBookingInProgress = false,
                                bookingErrorMessage = result.exceptionOrNull()?.message ?: "Booking failed. Please try again."
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isBookingInProgress = false,
                            bookingErrorMessage = error.message ?: "Booking failed. Please try again."
                        )
                    }
                )
        }
    }

    fun dismissBookingConfirmation() {
        _uiState.value = _uiState.value.copy(bookingSuccessConfirmation = null)
    }

    private fun loadInitialCourts() {
        searchCourts()
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val locationRepository: LocationRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, locationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private suspend fun <T> runCatchingCancellable(
    action: suspend () -> T
): Result<T> {
    return try {
        Result.success(action())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Result.failure(error)
    }
}
