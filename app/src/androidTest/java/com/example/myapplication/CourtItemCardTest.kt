package com.example.myapplication

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.Court
import com.example.myapplication.ui.home.CourtItemCard
import com.example.myapplication.ui.theme.CourtFinderTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CourtItemCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun favoriteAndAvailabilityButtonsCallTheirActions() {
        var favoriteClicked = false
        var alertClicked = false

        composeRule.setContent {
            CourtFinderTheme {
                CourtItemCard(
                    court = Court(
                        id = "test-court",
                        name = "Test Court",
                        sportType = "Tennis",
                        location = "Campus",
                        distanceKm = 1.0,
                        pricePerHour = "$10/hr",
                        rating = 4.8,
                        availableTimeSlots = listOf("06:00 PM"),
                        aiMatchScore = 90
                    ),
                    onToggleFavorite = { favoriteClicked = true },
                    onToggleAlert = { alertClicked = true },
                    onBookClick = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Favorite").performClick()
        composeRule.onNodeWithText("Notify me when available").performClick()

        assertTrue(favoriteClicked)
        assertTrue(alertClicked)
    }
}
