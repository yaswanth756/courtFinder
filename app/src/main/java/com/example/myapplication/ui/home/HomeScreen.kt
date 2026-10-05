package com.example.myapplication.ui.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Court
import com.example.myapplication.data.User
import com.example.myapplication.ui.theme.CourtGreen
import com.example.myapplication.ui.theme.CourtGreenLight
import com.example.myapplication.ui.theme.CourtLime
import com.example.myapplication.ui.theme.DarkSlateBackground
import com.example.myapplication.ui.theme.DarkSlateCard
import com.example.myapplication.ui.theme.DarkSlateSurface

@Composable
fun HomeScreen(
    user: User?,
    searchQuery: String,
    searchResults: List<Court>,
    isSearching: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onPerformSearch: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "🎾 Tennis", "🏸 Badminton", "🏀 Basketball", "🎾 Padel", "🏓 Pickleball")

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
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row: Logo & User Info / Logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(CourtGreen, CourtLime)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "CourtFinder AI",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Welcome, ${user?.name ?: "Player"}",
                            fontSize = 12.sp,
                            color = CourtLime,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                TextButton(onClick = onLogout) {
                    Text(
                        text = "Sign Out",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Search Hero Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CourtLime.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CourtLime.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "AI Prompt",
                                    tint = CourtLime,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart AI Court Search",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CourtLime
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                "e.g. Find Tennis courts near Downtown at 6 PM...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CourtLime
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onPerformSearch) {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        color = CourtLime,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Run AI Search",
                                        tint = CourtLime
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CourtLime,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = CourtLime,
                            focusedContainerColor = DarkSlateBackground.copy(alpha = 0.6f),
                            unfocusedContainerColor = DarkSlateBackground.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onPerformSearch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CourtGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "✨ Search Available Courts with AI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sports Category Chips
            Text(
                text = "Sports Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) CourtGreen else DarkSlateCard,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = category }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CourtLime else Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Results Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Recommended Courts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CourtLime.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${searchResults.size} Found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CourtLime,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Court Recommendations List
            searchResults.forEach { court ->
                CourtItemCard(court = court)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // API Integration Notice Banner
            Surface(
                color = CourtGreenLight.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CourtGreenLight.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚀 API & Backend Integration Ready",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CourtGreenLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Splash Screen, Auth Flow, and AI Court Finder UI are complete. Connect Retrofit/Ktor endpoints in CourtFinderApi.kt to go live!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CourtItemCard(court: Court) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSlateSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Badge Row: Sport Type + AI Match Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSlateCard
                ) {
                    Text(
                        text = court.sportType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CourtLime,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CourtGreen.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CourtLime,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${court.aiMatchScore}% AI Match",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CourtLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Court Title
            Text(
                text = court.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Location & Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${court.location} • ${court.distanceKm} km away",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Slots
            Text(
                text = "Available Today:",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                court.availableTimeSlots.forEach { slot ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSlateCard
                    ) {
                        Text(
                            text = slot,
                            fontSize = 11.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price & Book Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Price",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = court.pricePerHour,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CourtLime
                    )
                }

                Button(
                    onClick = { /* TODO: Book Court API endpoint */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CourtGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Book Court",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
