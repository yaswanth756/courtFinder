package com.example.myapplication

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.FirebaseGoogleAuthRepository
import com.example.myapplication.data.CourtDatabase
import com.example.myapplication.data.LocationRepository
import com.example.myapplication.data.GoogleSignInManager
import com.example.myapplication.data.MockCourtFinderApi
import com.example.myapplication.data.RetrofitCourtFinderApi
import com.example.myapplication.data.SessionStore
import com.example.myapplication.data.UnavailableCourtFinderApi
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.myapplication.ui.auth.AuthViewModelFactory
import com.example.myapplication.ui.auth.AppScreen
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.splash.SplashScreen
import com.example.myapplication.ui.theme.CourtFinderTheme
import com.example.myapplication.ui.theme.DarkSlateBackground
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = CourtDatabase.getDatabase(applicationContext)
        val sessionStore = SessionStore(applicationContext)
        val api = when {
            BuildConfig.COURTFINDER_API_BASE_URL.isNotBlank() ->
                RetrofitCourtFinderApi(BuildConfig.COURTFINDER_API_BASE_URL, sessionStore)
            BuildConfig.DEBUG -> MockCourtFinderApi()
            else -> UnavailableCourtFinderApi()
        }
        val firebaseGoogleAuth = runCatching {
            FirebaseGoogleAuthRepository(FirebaseAuth.getInstance())
        }.getOrNull()
        val repository = AuthRepository(
            api = api,
            courtDao = database.courtDao(),
            firebaseGoogleAuth = firebaseGoogleAuth
        )
        val generatedWebClientId = resources.getIdentifier(
            "default_web_client_id", "string", packageName
        ).takeIf { it != 0 }?.let(::getString).orEmpty()
        val googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID.ifBlank { generatedWebClientId }
        val googleSignInConfigured = googleWebClientId.isNotBlank() &&
            BuildConfig.COURTFINDER_API_BASE_URL.isNotBlank() &&
            firebaseGoogleAuth != null
        val locationRepository = LocationRepository(applicationContext)
        val viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(repository, locationRepository)
        )[AuthViewModel::class.java]
        val startGoogleSignIn: () -> Unit = {
            viewModel.beginGoogleSignIn()
            lifecycleScope.launch {
                try {
                    val idToken = GoogleSignInManager(this@MainActivity)
                        .getIdToken(googleWebClientId)
                    val firebaseIdToken = firebaseGoogleAuth
                        ?.signInWithGoogleIdToken(idToken)
                        ?: error("Firebase Authentication is unavailable. Check app/google-services.json.")
                    viewModel.continueWithGoogle(firebaseIdToken)
                } catch (_: GetCredentialCancellationException) {
                    viewModel.cancelGoogleSignIn()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    viewModel.showGoogleSignInError(
                        error.message ?: "Google sign-in could not be completed."
                    )
                }
            }
        }
        setContent {
            CourtFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkSlateBackground
                ) {
                    CourtFinderApp(viewModel, startGoogleSignIn, ::openPrivacyPolicy, googleSignInConfigured)
                }
            }
        }
    }

    private fun openPrivacyPolicy() {
        val policyUrl = BuildConfig.COURTFINDER_PRIVACY_POLICY_URL
        if (policyUrl.isBlank()) {
            Toast.makeText(this, "Privacy policy link is not configured.", Toast.LENGTH_LONG).show()
            return
        }
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl)))
    }
}

@Composable
fun CourtFinderApp(
    viewModel: AuthViewModel,
    onGoogleSignIn: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    googleSignInConfigured: Boolean
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    SideEffect {
        val window = (view.context as ComponentActivity).window
        val useDarkSystemBarIcons = state.currentScreen == AppScreen.Splash
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = useDarkSystemBarIcons
            isAppearanceLightNavigationBars = useDarkSystemBarIcons
        }
    }

    Crossfade(
        targetState = state.currentScreen,
        animationSpec = tween(durationMillis = 500),
        label = "Screen Crossfade"
    ) { screen ->
        when (screen) {
            AppScreen.Splash -> {
                SplashScreen(
                    onTimeout = {
                        viewModel.navigateTo(AppScreen.Auth)
                    }
                )
            }

            AppScreen.Auth -> {
                LoginScreen(
                    state = state,
                    onEmailOrPhoneChanged = viewModel::onEmailOrPhoneChanged,
                    onPhoneChanged = viewModel::onPhoneChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onFullNameChanged = viewModel::onFullNameChanged,
                    onAuthModeChanged = viewModel::switchAuthMode,
                    onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                    onSubmit = viewModel::submitAuth,
                    onGoogleSignIn = onGoogleSignIn,
                    onGuestMode = viewModel::continueAsGuest,
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                    googleSignInConfigured = googleSignInConfigured
                )
            }

            AppScreen.Home -> {
                HomeScreen(
                    user = state.loggedInUser,
                    searchQuery = state.searchAiQuery,
                    searchResults = state.searchResults,
                    isSearching = state.isSearchingCourts,
                    searchErrorMessage = state.searchErrorMessage,
                    selectedCourtForBooking = state.selectedCourtForBooking,
                    selectedTimeSlot = state.selectedTimeSlot,
                    isBookingInProgress = state.isBookingInProgress,
                    bookingSuccessConfirmation = state.bookingSuccessConfirmation,
                    bookingErrorMessage = state.bookingErrorMessage,
                    onlyFavoritesFilter = state.onlyFavoritesFilter,
                    minRatingFilter = state.minRatingFilter,
                    alertToastMessage = state.alertToastMessage,
                    currentLocation = state.currentLocation,
                    locationAddress = state.locationAddress,
                    isFindingLocation = state.isFindingLocation,
                    locationErrorMessage = state.locationErrorMessage,
                    isDeletingAccount = state.isDeletingAccount,
                    accountDeletionError = state.accountDeletionError,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onPerformSearch = viewModel::searchCourts,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onToggleAlert = viewModel::toggleAlert,
                    onToggleFavoritesFilter = viewModel::toggleFavoritesFilter,
                    onToggleMinRatingFilter = viewModel::toggleMinRatingFilter,
                    onDismissAlertToast = viewModel::dismissAlertToast,
                    onFindCurrentLocation = viewModel::findCurrentLocation,
                    onOpenBookingDialog = viewModel::openBookingDialog,
                    onSelectTimeSlot = viewModel::selectTimeSlot,
                    onConfirmBooking = viewModel::confirmBooking,
                    onDismissBookingDialog = viewModel::dismissBookingDialog,
                    onDismissBookingConfirmation = viewModel::dismissBookingConfirmation,
                    onDeleteAccount = viewModel::deleteAccount,
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}
