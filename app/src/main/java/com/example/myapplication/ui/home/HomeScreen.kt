package com.example.myapplication.ui.home

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.BookingConfirmation
import com.example.myapplication.data.Court
import com.example.myapplication.data.User
import com.example.myapplication.data.DeviceLocation
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.core.content.ContextCompat

private val HomeBackground = Color(0xFF17130F)
private val HomeBackgroundWarm = Color(0xFF241A13)
private val HomeSurface = Color(0xD92D251E)
private val HomeGlass = Color(0xB348382B)
private val HomeAccent = Color(0xFFD1A66D)
private val HomeAccentBright = Color(0xFFF0D6AB)
private val HomeAccentSoft = Color(0xFFE6C28D)
private val HomeText = Color(0xFFF8F3EA)
private val HomeMuted = Color(0xFFBEB2A3)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    user: User?,
    searchQuery: String,
    searchResults: List<Court>,
    isSearching: Boolean,
    searchErrorMessage: String?,
    selectedCourtForBooking: Court?,
    selectedTimeSlot: String?,
    isBookingInProgress: Boolean,
    bookingSuccessConfirmation: BookingConfirmation?,
    bookingErrorMessage: String?,
    onlyFavoritesFilter: Boolean,
    minRatingFilter: Double,
    alertToastMessage: String?,
    currentLocation: DeviceLocation?,
    locationAddress: String?,
    isFindingLocation: Boolean,
    locationErrorMessage: String?,
    isDeletingAccount: Boolean,
    accountDeletionError: String?,
    onSearchQueryChanged: (String) -> Unit,
    onPerformSearch: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleAlert: (String) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onToggleMinRatingFilter: () -> Unit,
    onDismissAlertToast: () -> Unit,
    onFindCurrentLocation: () -> Unit,
    onOpenBookingDialog: (Court) -> Unit,
    onSelectTimeSlot: (String) -> Unit,
    onConfirmBooking: () -> Unit,
    onDismissBookingDialog: () -> Unit,
    onDismissBookingConfirmation: () -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedCategory by remember { mutableStateOf("All") }
    var isMapVisible by remember { mutableStateOf(false) }
    var locationPermissionMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteAccountConfirmation by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            locationPermissionMessage = null
            onFindCurrentLocation()
        } else {
            locationPermissionMessage = "Allow approximate location to sort nearby courts."
        }
    }
    val categories = listOf("All", "Tennis", "Badminton", "Basketball", "Padel", "Pickleball")
    val quickPrompts = listOf(
        "Tennis",
        "Badminton",
        "Basketball",
        "Padel"
    )
    val visibleCourts = searchResults.filter { court ->
        (!onlyFavoritesFilter || court.isFavorite) && court.rating >= minRatingFilter
    }.map { court ->
        if (currentLocation != null && (court.latitude != 0.0 || court.longitude != 0.0)) {
            court.copy(distanceKm = distanceBetweenKm(currentLocation, court.latitude, court.longitude))
        } else {
            court
        }
    }.sortedBy { it.distanceKm }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        HomeBackground,
                        HomeBackgroundWarm,
                        HomeBackground
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 110.dp, y = (-100).dp)
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        listOf(HomeAccent.copy(alpha = 0.16f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(HomeAccentBright, HomeAccent, HomeAccent.copy(alpha = 0.8f))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Logo",
                            tint = HomeBackground,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "CourtFinder",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeText
                        )
                        Text(
                            text = "DISCOVER · BOOK · PLAY",
                            fontSize = 10.sp,
                            color = HomeMuted,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.1.sp
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showAccountMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Account menu",
                            tint = HomeText
                        )
                    }
                    DropdownMenu(
                        expanded = showAccountMenu,
                        onDismissRequest = { showAccountMenu = false },
                        containerColor = HomeSurface,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sign out", color = HomeText) },
                            onClick = { showAccountMenu = false; onLogout() }
                        )
                        if (user?.isGuest == false) {
                            DropdownMenuItem(
                                text = { Text("Delete account", color = Color(0xFFFFA69E)) },
                                enabled = !isDeletingAccount,
                                onClick = {
                                    showAccountMenu = false
                                    showDeleteAccountConfirmation = true
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Privacy policy", color = HomeText) },
                            onClick = { showAccountMenu = false; onOpenPrivacyPolicy() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome back, ${user?.name?.substringBefore(' ') ?: "Player"}",
                color = HomeMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Find your court.",
                color = HomeText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Make room for your next great game.",
                color = HomeMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    GlassActionChip(
                        text = "All",
                        selected = selectedCategory == "All",
                        onClick = {
                            selectedCategory = "All"
                            onSearchQueryChanged("Find courts available near me")
                            onPerformSearch()
                        }
                    )
                }
                item {
                    GlassActionChip(
                        text = if (isFindingLocation) "Finding..." else "Near me",
                        icon = Icons.Default.LocationOn,
                        onClick = {
                            val permissionGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (permissionGranted) onFindCurrentLocation()
                            else locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    )
                }
                item { GlassActionChip(text = "Explore map", onClick = { isMapVisible = true }) }
                item {
                    GlassActionChip(
                        text = "Favorites",
                        icon = if (onlyFavoritesFilter) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        selected = onlyFavoritesFilter,
                        onClick = onToggleFavoritesFilter
                    )
                }
                item {
                    GlassActionChip(
                        text = "4.8+",
                        icon = Icons.Default.Star,
                        selected = minRatingFilter > 0,
                        onClick = onToggleMinRatingFilter
                    )
                }
            }
            (locationErrorMessage ?: locationPermissionMessage)?.let { message ->
                Text(message, color = HomeMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            locationAddress?.let { address ->
                Text("Searching near $address", color = HomeAccentBright, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (alertToastMessage != null) {
                Surface(
                    modifier = Modifier.padding(top = 10.dp).clickable(onClick = onDismissAlertToast),
                    color = HomeAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HomeAccent.copy(alpha = 0.26f))
                ) {
                    Text(alertToastMessage, color = HomeAccentBright, fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                }
            }

            // Court Search Card
            Card(
                colors = CardDefaults.cardColors(containerColor = HomeGlass),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = HomeAccentSoft.copy(alpha = 0.2f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Court search prompt",
                                    tint = HomeAccentSoft,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YOUR NEXT GAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeAccentBright,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = {
                            Text(
                                "Search courts, sports or locations",
                                color = HomeMuted.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = HomeAccentSoft
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = onPerformSearch) {
                                if (isSearching) {
                                    CircularProgressIndicator(
                                        color = HomeAccentSoft,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Search courts",
                                        tint = HomeAccentSoft
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HomeAccentBright,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                            focusedTextColor = HomeText,
                            unfocusedTextColor = HomeText,
                            cursorColor = HomeAccentBright,
                            focusedContainerColor = HomeBackground.copy(alpha = 0.46f),
                            unfocusedContainerColor = HomeBackground.copy(alpha = 0.32f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search suggestion chips
                    Text(
                        text = "Search suggestions:",
                        fontSize = 11.sp,
                        color = HomeMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickPrompts) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = HomeSurface.copy(alpha = 0.75f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSearchQueryChanged(prompt)
                                        onPerformSearch()
                                    }
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 11.sp,
                                    color = HomeAccentBright,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassActionButton(
                        text = if (isSearching) "Searching courts..." else "Find available courts",
                        onClick = onPerformSearch,
                        enabled = !isSearching,
                        loading = isSearching,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sports Category Chips
            Text(
                text = "Choose a sport",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = HomeText,
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
                        color = if (isSelected) HomeAccent.copy(alpha = 0.26f) else HomeSurface.copy(alpha = 0.72f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedCategory = category
                                val query = if (category == "All") "Find courts available near me" else "Find ${category.substringAfter(" ")} courts near me"
                                onSearchQueryChanged(query)
                                onPerformSearch()
                            }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) HomeAccentBright.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) HomeAccentBright else HomeMuted,
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
                    text = "Courts for your next game",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeText
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HomeAccent.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "${visibleCourts.size} ${if (visibleCourts.size == 1) "court" else "courts"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeAccentBright,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Court Recommendations List
            if (searchErrorMessage != null) {
                Text(searchErrorMessage, color = HomeMuted, modifier = Modifier.padding(vertical = 8.dp))
            }
            if (visibleCourts.isEmpty() && !isSearching && searchErrorMessage == null) {
                Surface(
                    color = HomeSurface.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = HomeAccentBright, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Your next court is out there", color = HomeText, fontWeight = FontWeight.SemiBold)
                        Text("Search a sport or use a suggestion to explore.", color = HomeMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            visibleCourts.forEach { court ->
                CourtItemCard(
                    court = court,
                    onToggleFavorite = { onToggleFavorite(court.id) },
                    onToggleAlert = { onToggleAlert(court.id) },
                    onBookClick = { onOpenBookingDialog(court) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (isMapVisible) {
            ModalBottomSheet(
                onDismissRequest = { isMapVisible = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = HomeSurface
            ) {
                CourtMapContent(
                    courts = visibleCourts,
                    currentLocation = currentLocation
                )
            }
        }

        // Interactive Court Booking Modal Dialog
        selectedCourtForBooking?.let { court ->
            AlertDialog(
                onDismissRequest = onDismissBookingDialog,
                containerColor = HomeSurface,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = court.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "${court.sportType} · ${court.location}",
                            fontSize = 12.sp,
                            color = HomeAccentSoft
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Court Amenities:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            court.amenities.forEach { amenity ->
                                Text(
                                    text = "• $amenity",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select Time Slot Today:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (bookingErrorMessage != null) {
                            Text(bookingErrorMessage, color = Color(0xFFEF4444), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            court.availableTimeSlots.forEach { slot ->
                                val isSelected = selectedTimeSlot == slot
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) HomeAccent.copy(alpha = 0.28f) else HomeGlass,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) HomeAccentBright else Color.White.copy(alpha = 0.16f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onSelectTimeSlot(slot) }
                                ) {
                                    Text(
                                        text = slot,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) HomeAccentBright else HomeText.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Price per hour:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(text = court.pricePerHour, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HomeAccentSoft)
                        }
                    }
                },
                confirmButton = {
                    GlassActionButton(
                        text = "Confirm booking",
                        onClick = onConfirmBooking,
                        enabled = !isBookingInProgress && selectedTimeSlot != null,
                        loading = isBookingInProgress,
                        compact = true
                    )
                },
                dismissButton = {
                    TextButton(onClick = onDismissBookingDialog) {
                        Text(text = "Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Booking Success Confirmation Alert Dialog
        bookingSuccessConfirmation?.let { booking ->
            AlertDialog(
                onDismissRequest = onDismissBookingConfirmation,
                containerColor = HomeSurface,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = HomeAccentBright,
                        modifier = Modifier.size(40.dp)
                    )
                },
                title = {
                    Text(
                        text = "Booking confirmed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Your booking was accepted by the CourtFinder service.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = HomeGlass,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Booking ID: ${booking.bookingId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HomeAccentSoft)
                                Text(text = "Time: ${booking.timeSlot} (${booking.date})", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = {
                    GlassActionButton("Done", onDismissBookingConfirmation, compact = true)
                }
            )
        }
        if (showDeleteAccountConfirmation) {
            AlertDialog(
                onDismissRequest = { if (!isDeletingAccount) showDeleteAccountConfirmation = false },
                containerColor = HomeSurface,
                title = { Text("Delete your account?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(
                            "This permanently deletes your CourtFinder account and booking history. You will be signed out. This cannot be undone.",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        accountDeletionError?.let { error ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(error, color = Color(0xFFFF8A80), fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onDeleteAccount,
                        enabled = !isDeletingAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                    ) {
                        if (isDeletingAccount) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Delete permanently")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteAccountConfirmation = false },
                        enabled = !isDeletingAccount
                    ) { Text("Cancel", color = Color.White.copy(alpha = 0.7f)) }
                }
            )
        }
    }
}

@Composable
private fun CourtMapContent(
    courts: List<Court>,
    currentLocation: DeviceLocation?
) {
    val firstCourt = courts.firstOrNull { it.latitude != 0.0 || it.longitude != 0.0 }
    val center = currentLocation?.let { LatLng(it.latitude, it.longitude) }
        ?: firstCourt?.let { LatLng(it.latitude, it.longitude) }
        ?: LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 11f)
    }
    LaunchedEffect(center) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(center, 11f)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Nearby Courts", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        GoogleMap(
            modifier = Modifier.fillMaxWidth().height(420.dp),
            cameraPositionState = cameraPositionState
        ) {
            courts.filter { it.latitude != 0.0 || it.longitude != 0.0 }.forEach { court ->
                val markerState = remember(court.id, court.latitude, court.longitude) {
                    MarkerState(position = LatLng(court.latitude, court.longitude))
                }
                Marker(
                    state = markerState,
                    title = court.name,
                    snippet = "${court.distanceKm} km · ${court.pricePerHour}"
                )
            }
            currentLocation?.let { location ->
                val userMarkerState = remember(location.latitude, location.longitude) {
                    MarkerState(position = LatLng(location.latitude, location.longitude))
                }
                Marker(
                    state = userMarkerState,
                    title = "You are here"
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

private fun distanceBetweenKm(
    currentLocation: DeviceLocation,
    destinationLatitude: Double,
    destinationLongitude: Double
): Double {
    val earthRadiusKm = 6371.0
    val latitudeDelta = Math.toRadians(destinationLatitude - currentLocation.latitude)
    val longitudeDelta = Math.toRadians(destinationLongitude - currentLocation.longitude)
    val startLatitude = Math.toRadians(currentLocation.latitude)
    val endLatitude = Math.toRadians(destinationLatitude)
    val a = Math.sin(latitudeDelta / 2) * Math.sin(latitudeDelta / 2) +
        Math.cos(startLatitude) * Math.cos(endLatitude) *
        Math.sin(longitudeDelta / 2) * Math.sin(longitudeDelta / 2)
    return 2 * earthRadiusKm * Math.asin(Math.sqrt(a))
}

@Composable
private fun GlassActionChip(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    selected: Boolean = false,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(50)
    Surface(
        modifier = Modifier.clip(shape).clickable(onClick = onClick),
        shape = shape,
        color = if (selected) HomeAccent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.055f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) HomeAccentBright.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.14f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = if (compact) 9.dp else 14.dp,
                vertical = if (compact) 7.dp else 10.dp
            )
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) HomeAccentBright else HomeAccentSoft,
                    modifier = Modifier.size(if (compact) 14.dp else 16.dp)
                )
            }
            Text(
                text = text,
                color = if (selected) HomeAccentBright else HomeText.copy(alpha = 0.9f),
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GlassActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    compact: Boolean = false
) {
    val shape = RoundedCornerShape(if (compact) 14.dp else 17.dp)
    val height = if (compact) 46.dp else 54.dp
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.15f else 0.05f),
                        HomeAccent.copy(alpha = if (enabled) 0.24f else 0.08f),
                        HomeAccent.copy(alpha = if (enabled) 0.11f else 0.04f)
                    )
                )
            )
            .border(
                1.dp,
                HomeAccentBright.copy(alpha = if (enabled) 0.56f else 0.2f),
                shape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.42f), Color.Transparent)
                    )
                )
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    strokeWidth = 2.dp,
                    color = HomeText
                )
            }
            Text(
                text = text,
                color = HomeText.copy(alpha = if (enabled) 1f else 0.55f),
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!loading) {
                Text("→", color = HomeAccentBright, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun CourtItemCard(
    court: Court,
    onToggleFavorite: () -> Unit,
    onToggleAlert: () -> Unit,
    onBookClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HomeSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Keep sport, rating, availability, and favorite controls on one aligned row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HomeGlass
                    ) {
                        Text(
                            text = court.sportType,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeAccentSoft,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HomeAccent.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = HomeAccentSoft,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${court.rating} rating",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HomeAccentSoft
                            )
                        }
                    }
                }

                GlassActionChip(
                    text = if (court.hasAlertSet) "Saved" else "Save availability",
                    onClick = onToggleAlert,
                    icon = Icons.Default.CheckCircle,
                    selected = court.hasAlertSet,
                    compact = true
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (court.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (court.isFavorite) Color(0xFFEF4444) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Court Title
            Text(
                text = court.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = HomeText
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
                    tint = HomeAccentSoft,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${court.location} · ${court.distanceKm} km away",
                    fontSize = 12.sp,
                    color = HomeMuted,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Amenities Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                court.amenities.take(3).forEach { amenity ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HomeGlass.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = amenity,
                            fontSize = 10.sp,
                            color = HomeMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Slots
            Text(
                text = "Available Today:",
                fontSize = 11.sp,
                color = HomeMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                court.availableTimeSlots.forEach { slot ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HomeGlass
                    ) {
                        Text(
                            text = slot,
                            fontSize = 11.sp,
                            color = HomeText,
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
                        color = HomeMuted
                    )
                    Text(
                        text = court.pricePerHour,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                    color = HomeAccentBright
                    )
                }

                GlassActionButton(
                    text = "Book court",
                    onClick = onBookClick,
                    compact = true
                )
            }
        }
    }
}
