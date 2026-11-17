package com.example.myapplication

import com.example.myapplication.data.CourtTypeConverters
import com.example.myapplication.data.MockCourtFinderApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockCourtFinderApiTest {
    @Test
    fun searchReturnsPickleballCourtsForPickleballQuery() = runBlocking {
        val courts = MockCourtFinderApi()
            .searchCourts("Pickleball")
            .getOrThrow()

        assertTrue(courts.isNotEmpty())
        assertTrue(courts.all { it.sportType == "Pickleball" })
    }

    @Test
    fun courtListTypeConverterKeepsTheValues() {
        val values = listOf("Free Parking", "Changing Rooms", "Water Station")
        val converter = CourtTypeConverters()

        assertEquals(values, converter.toStringList(converter.fromStringList(values)))
        assertTrue(converter.toStringList("").isEmpty())
    }
}
