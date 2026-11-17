package com.example.myapplication.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.ErrorRed

private val NightInk = Color(0xFF11100F)
private val WarmGold = Color(0xFF9B7542)
private val WarmGoldLight = Color(0xFFC8A979)
private val CreamText = Color(0xFFF7F0E2)
private val MutedCream = Color(0xFFCDC2B0)
private val Glass = Color(0xB82B2A27)

@Composable
fun LoginScreen(
    state: AuthUiState,
    onEmailOrPhoneChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onAuthModeChanged: (AuthMode) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onGuestMode: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    googleSignInConfigured: Boolean
) {
    val backgroundMotion by androidx.compose.animation.core.rememberInfiniteTransition(label = "courtLights")
        .animateFloat(
            initialValue = 0.82f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                androidx.compose.animation.core.tween(2400),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "lightPulse"
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightInk)
    ) {
        NightCourtBackdrop(Modifier.fillMaxSize(), backgroundMotion)

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            CourtFinderBrand()
            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = Glass,
                shadowElevation = 18.dp
            ) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 20.dp)) {
                    Text(
                        text = if (state.authMode == AuthMode.Login) "Welcome back 👋" else "Join CourtFinder",
                        color = CreamText,
                        fontSize = 26.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = if (state.authMode == AuthMode.Login)
                            "Sign in to continue to your account"
                        else
                            "Create an account and find your next game",
                        color = MutedCream,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 17.dp)
                    )

                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.errorMessage?.let { error ->
                            Surface(
                                color = ErrorRed.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(13.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = error,
                                    color = Color(0xFFFFC2B8),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = state.authMode == AuthMode.SignUp) {
                        Column {
                            WarmTextField(
                                value = state.fullNameInput,
                                onValueChange = onFullNameChanged,
                                label = "Full name",
                                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                            Spacer(Modifier.height(11.dp))
                        }
                    }

                    WarmTextField(
                        value = state.emailOrPhoneInput,
                        onValueChange = onEmailOrPhoneChanged,
                        label = if (state.authMode == AuthMode.Login) "Email or phone" else "Email address",
                        icon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardType = if (state.authMode == AuthMode.Login) KeyboardType.Text else KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )

                    AnimatedVisibility(visible = state.authMode == AuthMode.SignUp) {
                        Column {
                            Spacer(Modifier.height(11.dp))
                            WarmTextField(
                                value = state.phoneInput,
                                onValueChange = onPhoneChanged,
                                label = "Phone number (optional)",
                                icon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        }
                    }

                    Spacer(Modifier.height(11.dp))
                    WarmTextField(
                        value = state.passwordInput,
                        onValueChange = onPasswordChanged,
                        label = "Password",
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        onDone = onSubmit,
                        trailingContent = {
                            androidx.compose.material3.IconButton(onClick = onTogglePasswordVisibility) {
                                Text(
                                    if (state.isPasswordVisible) "Hide" else "Show",
                                    color = WarmGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    )

                    if (state.authMode == AuthMode.SignUp) {
                        Text(
                            "Use at least 10 characters.",
                            color = MutedCream.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 7.dp, bottom = 4.dp)
                        )
                    }

                    WarmActionButton(
                        text = if (state.authMode == AuthMode.Login) "Sign In" else "Create Account",
                        isLoading = state.isLoading,
                        onClick = onSubmit,
                        modifier = Modifier.fillMaxWidth().padding(top = 15.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.18f))
                        Text("  or continue with  ", color = MutedCream, fontSize = 12.sp)
                        HorizontalDivider(Modifier.weight(1f), color = Color.White.copy(alpha = 0.18f))
                    }
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGoogleSignIn,
                        enabled = googleSignInConfigured && !state.isLoading,
                        shape = RoundedCornerShape(15.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = if (googleSignInConfigured) 0.62f else 0.2f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White.copy(alpha = if (googleSignInConfigured) 0.96f else 0.18f),
                            contentColor = Color(0xFF382F27),
                            disabledContentColor = MutedCream
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(23.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                            Spacer(Modifier.width(9.dp))
                            Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (!googleSignInConfigured) {
                        Text(
                            "Google sign-in requires OAuth and a connected backend.",
                            color = MutedCream.copy(alpha = 0.84f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (state.authMode == AuthMode.Login) "Don't have an account?" else "Already have an account?",
                            color = MutedCream,
                            fontSize = 13.sp
                        )
                        TextButton(onClick = {
                            onAuthModeChanged(if (state.authMode == AuthMode.Login) AuthMode.SignUp else AuthMode.Login)
                        }) {
                            Text(
                                if (state.authMode == AuthMode.Login) "Sign Up" else "Sign In",
                                color = WarmGoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onGuestMode, enabled = !state.isLoading) {
                Text("Explore as guest", color = Color(0xFFF0D49F), fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = onOpenPrivacyPolicy) {
                Text("Privacy policy", color = MutedCream.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CourtFinderBrand() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CourtBrandMark(Modifier.size(76.dp))
        Spacer(Modifier.height(7.dp))
        Text("CourtFinder", color = CreamText, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
        Text("Find. Book. Attend.", color = MutedCream, fontSize = 15.sp, letterSpacing = 0.2.sp)
    }
}

@Composable
private fun CourtBrandMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension * 0.43f
        val stroke = 1.5.dp.toPx()
        drawCircle(Color(0xFFD8C9AD).copy(alpha = 0.85f), r, c, style = Stroke(stroke))
        drawCircle(Color(0xFFD8C9AD).copy(alpha = 0.6f), r * 0.78f, c, style = Stroke(stroke))
        drawCircle(Color(0xFFD8C9AD).copy(alpha = 0.45f), r * 0.55f, c, style = Stroke(stroke))
        val courtWidth = r * 0.9f
        val courtHeight = r * 1.25f
        val topLeft = androidx.compose.ui.geometry.Offset(c.x - courtWidth / 2f, c.y - courtHeight / 2f)
        val courtSize = androidx.compose.ui.geometry.Size(courtWidth, courtHeight)
        drawRoundRect(Color(0xFFD8C9AD), topLeft, courtSize, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = Stroke(stroke))
        drawLine(Color(0xFFD8C9AD), androidx.compose.ui.geometry.Offset(c.x, topLeft.y), androidx.compose.ui.geometry.Offset(c.x, topLeft.y + courtHeight), stroke)
        drawLine(Color(0xFFD8C9AD), androidx.compose.ui.geometry.Offset(topLeft.x, c.y), androidx.compose.ui.geometry.Offset(topLeft.x + courtWidth, c.y), stroke)
        drawCircle(Color(0xFFD8C9AD), r * 0.09f, c)
        drawCircle(NightInk, r * 0.035f, c)
    }
}

@Composable
private fun WarmActionButton(text: String, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF765427), WarmGold, Color(0xFF765427))))
            .border(1.dp, WarmGoldLight.copy(alpha = 0.66f), RoundedCornerShape(16.dp))
            .clickable(enabled = !isLoading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(21.dp), color = CreamText, strokeWidth = 2.dp)
        } else {
            Text(text, color = CreamText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(9.dp))
            Text("→", color = Color(0xFFF0D49F), fontSize = 21.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun WarmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: @Composable () -> Unit,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = icon,
        trailingIcon = trailingContent,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        singleLine = true,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = WarmGoldLight,
            unfocusedBorderColor = Color(0xFFC9AC7E).copy(alpha = 0.58f),
            focusedLabelColor = WarmGoldLight,
            unfocusedLabelColor = MutedCream.copy(alpha = 0.72f),
            focusedTextColor = CreamText,
            unfocusedTextColor = CreamText,
            focusedLeadingIconColor = WarmGoldLight,
            unfocusedLeadingIconColor = Color(0xFFD8C6A6),
            cursorColor = WarmGoldLight,
            focusedContainerColor = Color(0xFF191714).copy(alpha = 0.76f),
            unfocusedContainerColor = Color(0xFF191714).copy(alpha = 0.70f)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun NightCourtBackdrop(modifier: Modifier = Modifier, lightPulse: Float) {
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(Color(0xFF34251C), Color(0xFF171716), NightInk)))
        val glowCenterY = size.height * 0.34f
        val glowRadius = size.width * 0.85f
        drawCircle(
            Brush.radialGradient(
                colors = listOf(Color(0xFFB9793C).copy(alpha = 0.23f * lightPulse), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, glowCenterY),
                radius = glowRadius
            ),
            radius = glowRadius,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, glowCenterY)
        )

        val courtLine = Color(0xFFE6CFAB).copy(alpha = 0.16f)
        val horizon = size.height * 0.57f
        // Perspective court boundary and service lines.
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.06f, horizon), androidx.compose.ui.geometry.Offset(size.width * 0.94f, horizon), 1.2.dp.toPx())
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.06f, horizon), androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.98f), 1.2.dp.toPx())
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.94f, horizon), androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.98f), 1.2.dp.toPx())
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.25f, horizon), androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.98f), 1.dp.toPx())
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.75f, horizon), androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.98f), 1.dp.toPx())
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.68f), androidx.compose.ui.geometry.Offset(size.width * 0.80f, size.height * 0.68f), 1.dp.toPx())
        drawLine(courtLine, androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.80f), androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.80f), 1.dp.toPx())

        // Warm stadium lights at the horizon.
        listOf(0.08f, 0.91f).forEach { fraction ->
            val lamp = androidx.compose.ui.geometry.Offset(size.width * fraction, size.height * 0.22f)
            val radius = 50.dp.toPx()
            drawCircle(
                Brush.radialGradient(listOf(Color(0xFFFFD38A).copy(alpha = 0.24f * lightPulse), Color.Transparent), lamp, radius),
                radius,
                lamp
            )
            drawCircle(Color(0xFFFFE4B0).copy(alpha = 0.9f), 2.5.dp.toPx(), lamp)
            drawLine(Color(0xFFE8D0AB).copy(alpha = 0.2f), androidx.compose.ui.geometry.Offset(lamp.x, lamp.y + 5.dp.toPx()), androidx.compose.ui.geometry.Offset(lamp.x, size.height * 0.48f), 1.dp.toPx())
        }

        // Gentle dark vignette keeps the text legible over the illustrated court.
        drawRect(
            Brush.verticalGradient(
                0f to Color.Black.copy(alpha = 0.14f),
                0.52f to Color.Transparent,
                1f to Color.Black.copy(alpha = 0.68f)
            )
        )
    }
}
