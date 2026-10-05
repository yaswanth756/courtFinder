package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import com.example.myapplication.ui.auth.AppScreen
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.LoginScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.splash.SplashScreen
import com.example.myapplication.ui.theme.CourtFinderTheme
import com.example.myapplication.ui.theme.DarkSlateBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourtFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkSlateBackground
                ) {
                    CourtFinderApp()
                }
            }
        }
    }
}

@Composable
fun CourtFinderApp(
    viewModel: AuthViewModel = remember { AuthViewModel() }
) {
    val state by viewModel.uiState.collectAsState()

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
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onFullNameChanged = viewModel::onFullNameChanged,
                    onAuthModeChanged = viewModel::switchAuthMode,
                    onToggleRememberMe = viewModel::toggleRememberMe,
                    onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
                    onSubmit = viewModel::submitAuth,
                    onGoogleSignIn = viewModel::continueWithGoogle,
                    onGuestMode = viewModel::continueAsGuest
                )
            }

            AppScreen.Home -> {
                HomeScreen(
                    user = state.loggedInUser,
                    searchQuery = state.searchAiQuery,
                    searchResults = state.searchResults,
                    isSearching = state.isSearchingCourts,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onPerformSearch = viewModel::searchCourtsWithAI,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}
