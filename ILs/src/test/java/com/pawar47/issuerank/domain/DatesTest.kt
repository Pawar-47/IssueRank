package com.pawar47.issuerank.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class DatesTest {

    @Before
    fun fixClock() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }

    /** 14 Nov 2023, 22:13:20 UTC */
    private val now = 1_700_000_000_000L

    @Test
    fun relativeUsesReadableBuckets() {
        assertEquals("Just now", Dates.relative(now - 5_000L, now))
        assertEquals("12 min ago", Dates.relative(now - 12 * Dates.MINUTE, now))
        assertEquals("3 hr ago", Dates.relative(now - 3 * Dates.HOUR, now))
        assertEquals("2 d ago", Dates.relative(now - 2 * Dates.DAY, now))
    }

    @Test
    fun relativeFallsBackToADateForOlderRecords() {
        assertEquals("20 Oct", Dates.relative(now - 25 * Dates.DAY, now))
    }

    @Test
    fun missingTimestampsRenderAsADash() {
        assertEquals("—", Dates.relative(0L, now))
    }

    @Test
    fun dayAndFullFormatsAreStable() {
        assertEquals("14 Nov 2023", Dates.day(now))
        assertTrue(Dates.full(now).startsWith("14 Nov 2023,"))
    }

    @Test
    fun sameDayIgnoresTheTimeOfDay() {
        assertTrue(Dates.isSameDay(now, now + 3_600_000L))
        assertFalse(Dates.isSameDay(now, now + Dates.DAY))
    }

    @Test
    fun daysBetweenCountsWholeDays() {
        assertEquals(2L, Dates.daysBetween(now, now + 2 * Dates.DAY))
        assertEquals(-2L, Dates.daysBetween(now, now - 2 * Dates.DAY))
    }

    @Test
    fun pickerRoundTripKeepsTheCalendarDayInAnyTimeZone() {
        val original = TimeZone.getDefault()
        try {
            listOf("UTC", "Asia/Kolkata", "America/Los_Angeles", "Pacific/Auckland").forEach { id ->
                TimeZone.setDefault(TimeZone.getTimeZone(id))
                val picked = Dates.toPickerMillis(now)
                assertEquals(0L, picked % Dates.DAY)
                assertEquals(Dates.startOfDay(now), Dates.fromPickerMillis(picked))
            }
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
