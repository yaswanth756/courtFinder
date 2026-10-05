package com.example.myapplication.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.CourtGreen
import com.example.myapplication.ui.theme.CourtGreenLight
import com.example.myapplication.ui.theme.CourtLime
import com.example.myapplication.ui.theme.DarkSlateBackground
import com.example.myapplication.ui.theme.DarkSlateSurface
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        pulseScale.animateTo(
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // Auto navigate after 2.5 seconds
    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Court & AI Logo Graphic
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Background Glowing Circle
                Surface(
                    shape = CircleShape,
                    color = CourtGreenLight.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale.value)
                ) {}

                // Canvas Sports Court Drawing
                Canvas(modifier = Modifier.size(120.dp)) {
                    val width = size.width
                    val height = size.height

                    // Outer Court Boundary
                    drawRoundRect(
                        color = CourtGreenLight,
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 6f)
                    )

                    // Center Line
                    drawLine(
                        color = CourtGreenLight.copy(alpha = 0.7f),
                        start = Offset(0f, height / 2),
                        end = Offset(width, height / 2),
                        strokeWidth = 4f
                    )

                    // Service Lines
                    drawRect(
                        color = CourtLime.copy(alpha = 0.3f),
                        topLeft = Offset(width * 0.2f, height * 0.2f),
                        size = Size(width * 0.6f, height * 0.6f),
                        style = Stroke(width = 3f)
                    )
                }

                // Center Icon Overlay
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(CourtGreen, CourtLime)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Sports Court",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // AI Spark Badge Overlay
                Surface(
                    shape = CircleShape,
                    color = CourtLime,
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI Powered",
                            tint = DarkSlateBackground,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlateBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Title
            Text(
                text = "CourtFinder",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "AI-Powered Smart Court Search",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = CourtLime,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Find & book Tennis, Badminton, Basketball & Padel courts in seconds",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Skip / Quick Action Button
            Button(
                onClick = onTimeout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CourtGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Continue",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Bottom Footer
        Text(
            text = "v1.0 • AI Court Matching Engine",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}
