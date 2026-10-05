package com.example.myapplication.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.CourtGreen
import com.example.myapplication.ui.theme.CourtGreenLight
import com.example.myapplication.ui.theme.CourtLime
import com.example.myapplication.ui.theme.DarkSlateBackground
import com.example.myapplication.ui.theme.DarkSlateCard
import com.example.myapplication.ui.theme.DarkSlateSurface
import com.example.myapplication.ui.theme.ErrorRed

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailOrPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onAuthModeChanged: (AuthMode) -> Unit,
    onToggleRememberMe: (Boolean) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGuestMode: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkSlateBackground,
                        DarkSlateSurface,
                        DarkSlateBackground
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Header & Logo Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(CourtGreen, CourtLime)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Court Finder",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "CourtFinder",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            // AI Badge Subtitle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CourtLime.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CourtLime.copy(alpha = 0.3f)),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI Powered",
                        tint = CourtLime,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🤖 AI-Powered Sports Court Search",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CourtLime
                    )
                }
            }

            // Auth Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = DarkSlateSurface.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Tab Switcher (Log In vs Sign Up)
                    TabRow(
                        selectedTabIndex = if (state.authMode == AuthMode.Login) 0 else 1,
                        containerColor = DarkSlateCard,
                        contentColor = CourtLime,
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(
                                        tabPositions[if (state.authMode == AuthMode.Login) 0 else 1]
                                    ),
                                    color = CourtLime,
                                    height = 3.dp
                                )
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Tab(
                            selected = state.authMode == AuthMode.Login,
                            onClick = { onAuthModeChanged(AuthMode.Login) },
                            text = {
                                Text(
                                    text = "Log In",
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.authMode == AuthMode.Login) CourtLime else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        )
                        Tab(
                            selected = state.authMode == AuthMode.SignUp,
                            onClick = { onAuthModeChanged(AuthMode.SignUp) },
                            text = {
                                Text(
                                    text = "Sign Up",
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.authMode == AuthMode.SignUp) CourtLime else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = if (state.authMode == AuthMode.Login) "Welcome Back!" else "Create CourtFinder Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = if (state.authMode == AuthMode.Login)
                            "Log in to search and book local sports courts using AI."
                        else
                            "Sign up to unlock instant AI court search and bookings.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Error Message Banner
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.errorMessage?.let { error ->
                            Surface(
                                color = ErrorRed.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Error",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = error,
                                        color = ErrorRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Full Name Input (Sign Up Mode Only)
                    if (state.authMode == AuthMode.SignUp) {
                        OutlinedTextField(
                            value = state.fullNameInput,
                            onValueChange = onFullNameChanged,
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = CourtLime
                                )
                            },
                            singleLine = true,
                            colors = customTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

                    // Email or Phone Input
                    OutlinedTextField(
                        value = state.emailOrPhoneInput,
                        onValueChange = onEmailOrPhoneChanged,
                        label = { Text(if (state.authMode == AuthMode.Login) "Email or Phone Number" else "Email Address") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (state.authMode == AuthMode.Login) Icons.Default.Email else Icons.Default.Email,
                                contentDescription = null,
                                tint = CourtLime
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = customTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    // Password Input
                    OutlinedTextField(
                        value = state.passwordInput,
                        onValueChange = onPasswordChanged,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = CourtLime
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (state.isPasswordVisible) Icons.Default.Check else Icons.Default.Lock,
                                    contentDescription = "Toggle password",
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        },
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onSubmit() }
                        ),
                        singleLine = true,
                        colors = customTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Remember Me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onToggleRememberMe(!state.rememberMe) }
                        ) {
                            Checkbox(
                                checked = state.rememberMe,
                                onCheckedChange = onToggleRememberMe,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CourtGreenLight,
                                    uncheckedColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                text = "Remember me",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        if (state.authMode == AuthMode.Login) {
                            TextButton(onClick = { /* TODO: Forgot password API endpoint */ }) {
                                Text(
                                    text = "Forgot password?",
                                    fontSize = 12.sp,
                                    color = CourtLime,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Action Submit Button
                    Button(
                        onClick = onSubmit,
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CourtGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (state.authMode == AuthMode.Login) "Log In" else "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // API Placeholder Info Note
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ API Placeholder: Auth endpoint ready for backend connection.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Social Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    text = "  OR  ",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Social Login Buttons
            OutlinedButton(
                onClick = onGoogleSignIn,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🌐 Continue with Google",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Continue as Guest Button
            TextButton(
                onClick = onGuestMode,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Skip for now & Continue as Guest →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CourtLime
                )
            }
        }
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CourtLime,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedLabelColor = CourtLime,
    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = CourtLime,
    focusedContainerColor = DarkSlateCard.copy(alpha = 0.6f),
    unfocusedContainerColor = DarkSlateCard.copy(alpha = 0.3f)
)
