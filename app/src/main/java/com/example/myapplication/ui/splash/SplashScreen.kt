package com.example.myapplication.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Parchment = Color(0xFFF5F0E5)
private val DeepTeal = Color(0xFF123E43)
private val CourtTeal = Color(0xFF287C78)
private val Bronze = Color(0xFF9B6849)
private val Ink = Color(0xFF1E3032)
private val MutedInk = Color(0xFF566365)

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.9f) }
    val alpha = remember { Animatable(0f) }
    val transition = rememberInfiniteTransition(label = "splashProgress")
    val progress by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(tween(1600), repeatMode = RepeatMode.Reverse),
        label = "splashProgressValue"
    )

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Parchment)
    ) {
        ParchmentPattern(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .alpha(alpha.value)
                .scale(scale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(8.dp))
            CompassCourtMark(Modifier.size(244.dp))
            Spacer(Modifier.height(8.dp))

            Text(
                text = "CourtFinder",
                color = DeepTeal,
                fontSize = 39.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(9.dp))
            Text(
                text = "DISCOVER. BOOK. PLAY.",
                color = Bronze,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Elevate your game, from court to crown.",
                color = MutedInk,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(Modifier.height(25.dp))
            ShuttleProgress(progress)
            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(60.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.horizontalGradient(listOf(DeepTeal, CourtTeal)))
                    .clickable(role = Role.Button, onClick = onTimeout)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "BEGIN YOUR GAME",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.width(9.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFD9B38F),
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                androidx.compose.material3.HorizontalDivider(Modifier.weight(1f), color = Bronze.copy(alpha = 0.28f))
                Surface(
                    modifier = Modifier.padding(horizontal = 12.dp).size(38.dp),
                    shape = CircleShape,
                    color = DeepTeal
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("CF", color = Parchment, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
                androidx.compose.material3.HorizontalDivider(Modifier.weight(1f), color = Bronze.copy(alpha = 0.28f))
            }
            Text(
                text = "COURTFINDER  •  FIND YOUR PLACE TO PLAY",
                color = MutedInk,
                fontSize = 9.sp,
                letterSpacing = 0.7.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 9.dp)
            )
        }
    }
}

@Composable
private fun CompassCourtMark(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val unit = size.minDimension / 2.5f
        val outerRadius = unit * 0.84f
        val ring = DeepTeal
        val gold = Bronze

        // Compass rose with eight alternating points.
        val points = Path()
        repeat(16) { index ->
            val angle = Math.PI * index / 8.0 - Math.PI / 2.0
            val radius = if (index % 2 == 0) unit else unit * 0.82f
            val point = androidx.compose.ui.geometry.Offset(
                center.x + (kotlin.math.cos(angle) * radius).toFloat(),
                center.y + (kotlin.math.sin(angle) * radius).toFloat()
            )
            if (index == 0) points.moveTo(point.x, point.y) else points.lineTo(point.x, point.y)
        }
        points.close()
        drawPath(points, color = gold)
        drawPath(points, color = ring, style = Stroke(width = 2.dp.toPx()))

        drawCircle(Brush.radialGradient(listOf(Color(0xFF4D8D84), DeepTeal), center, outerRadius), outerRadius, center)
        drawCircle(gold.copy(alpha = 0.95f), outerRadius, center, style = Stroke(width = 2.dp.toPx()))
        drawCircle(Parchment, outerRadius * 0.82f, center, style = Stroke(width = 2.dp.toPx()))
        drawCircle(CourtTeal, outerRadius * 0.75f, center)
        drawCircle(gold, outerRadius * 0.75f, center, style = Stroke(width = 1.dp.toPx()))

        // Court markings inside the compass.
        val halfWidth = outerRadius * 0.54f
        val halfHeight = outerRadius * 0.68f
        val courtTopLeft = androidx.compose.ui.geometry.Offset(center.x - halfWidth, center.y - halfHeight)
        val courtSize = androidx.compose.ui.geometry.Size(halfWidth * 2f, halfHeight * 2f)
        drawRoundRect(Parchment.copy(alpha = 0.84f), courtTopLeft, courtSize, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()), style = Stroke(width = 1.6.dp.toPx()))
        drawLine(Parchment.copy(alpha = 0.84f), androidx.compose.ui.geometry.Offset(center.x, courtTopLeft.y), androidx.compose.ui.geometry.Offset(center.x, courtTopLeft.y + courtSize.height), strokeWidth = 1.3.dp.toPx())
        drawLine(Parchment.copy(alpha = 0.84f), androidx.compose.ui.geometry.Offset(courtTopLeft.x, center.y), androidx.compose.ui.geometry.Offset(courtTopLeft.x + courtSize.width, center.y), strokeWidth = 1.3.dp.toPx())
        drawLine(gold, androidx.compose.ui.geometry.Offset(center.x - halfWidth, center.y - halfHeight * 0.42f), androidx.compose.ui.geometry.Offset(center.x + halfWidth, center.y - halfHeight * 0.42f), strokeWidth = 1.dp.toPx())
        drawLine(gold, androidx.compose.ui.geometry.Offset(center.x - halfWidth, center.y + halfHeight * 0.42f), androidx.compose.ui.geometry.Offset(center.x + halfWidth, center.y + halfHeight * 0.42f), strokeWidth = 1.dp.toPx())

        // Center medallion and crown-like star.
        drawCircle(Brush.linearGradient(listOf(Color(0xFFD8B68D), Color(0xFF9A6749))), radius = outerRadius * 0.22f, center = center)
        drawCircle(DeepTeal, radius = outerRadius * 0.15f, center = center)
        val star = Path()
        repeat(10) { index ->
            val angle = Math.PI * index / 5.0 - Math.PI / 2.0
            val radius = if (index % 2 == 0) outerRadius * 0.105f else outerRadius * 0.045f
            val point = androidx.compose.ui.geometry.Offset(
                center.x + (kotlin.math.cos(angle) * radius).toFloat(),
                center.y + (kotlin.math.sin(angle) * radius).toFloat()
            )
            if (index == 0) star.moveTo(point.x, point.y) else star.lineTo(point.x, point.y)
        }
        star.close()
        drawPath(star, Color(0xFFF4E7D1))
    }
}

@Composable
private fun ShuttleProgress(progress: Float) {
    Row(
        modifier = Modifier.widthIn(max = 230.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(CircleShape)
                .background(Color(0xFFD5D0C3))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(DeepTeal, CourtTeal)))
            )
        }
        Spacer(Modifier.width(7.dp))
        Canvas(Modifier.size(width = 25.dp, height = 19.dp)) {
            val shuttle = Path().apply {
                moveTo(size.width * 0.12f, size.height * 0.5f)
                lineTo(size.width * 0.68f, size.height * 0.12f)
                lineTo(size.width * 0.68f, size.height * 0.88f)
                close()
            }
            drawPath(shuttle, Bronze)
            drawLine(DeepTeal, androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.5f), androidx.compose.ui.geometry.Offset(size.width, size.height * 0.5f), strokeWidth = 2.dp.toPx())
            drawLine(Parchment, androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.3f), androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.7f), strokeWidth = 1.dp.toPx())
        }
    }
}

@Composable
private fun ParchmentPattern(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val line = Color(0xFFBBA98C).copy(alpha = 0.12f)
        val step = 34.dp.toPx()
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(line, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x + size.height, size.height), strokeWidth = 0.7.dp.toPx())
            x += step * 2f
        }
        var y = 0f
        while (y < size.height) {
            drawLine(line.copy(alpha = 0.07f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 0.5.dp.toPx())
            y += step * 3f
        }
    }
}
