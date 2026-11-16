package com.pawar47.issuerank.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * All date presentation in one place. Timestamps are stored as epoch milliseconds in the
 * device's own clock; only this object turns them into text, so the app has one date
 * language instead of a different format per screen.
 */
object Dates {
    const val MINUTE = 60_000L
    const val HOUR = 3_600_000L
    const val DAY = 86_400_000L

    /** 16 Nov 2026 */
    fun day(millis: Long): String = format("d MMM yyyy", millis)

    /** 16 Nov */
    fun short(millis: Long): String = format("d MMM", millis)

    /** 16 Nov 2026, 14:05 */
    fun full(millis: Long): String = format("d MMM yyyy, HH:mm", millis)

    /** Monday, 16 November */
    fun weekday(millis: Long): String = format("EEEE, d MMMM", millis)

    /** Just now · 12 min ago · 3 hr ago · 2 d ago · 16 Nov 2026 */
    fun relative(millis: Long, now: Long = System.currentTimeMillis()): String {
        if (millis <= 0L) return "—"
        val elapsed = (now - millis).coerceAtLeast(0L)
        return when {
            elapsed < MINUTE -> "Just now"
            elapsed < HOUR -> "${elapsed / MINUTE} min ago"
            elapsed < DAY -> "${elapsed / HOUR} hr ago"
            elapsed < 7 * DAY -> "${elapsed / DAY} d ago"
            isSameYear(millis, now) -> short(millis)
            else -> day(millis)
        }
    }

    fun startOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    fun isSameDay(a: Long, b: Long): Boolean = startOfDay(a) == startOfDay(b)

    fun isSameYear(a: Long, b: Long): Boolean {
        val first = Calendar.getInstance().apply { timeInMillis = a }
        val second = Calendar.getInstance().apply { timeInMillis = b }
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
    }

    /** Whole days from [from] to [to]; negative when [to] is in the past. */
    fun daysBetween(from: Long, to: Long): Long = (startOfDay(to) - startOfDay(from)) / DAY

    /**
     * Local timestamp to the UTC-midnight value the system date picker works in, and back.
     * Without this round trip a date picked in, say, UTC+5:30 would come back as the day before.
     */
    fun toPickerMillis(localMillis: Long): Long {
        val local = Calendar.getInstance().apply { timeInMillis = localMillis }
        return utcMidnight(
            local.get(Calendar.YEAR),
            local.get(Calendar.MONTH),
            local.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun fromPickerMillis(pickerMillis: Long): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = pickerMillis
        }
        return localMidnight(
            utc.get(Calendar.YEAR),
            utc.get(Calendar.MONTH),
            utc.get(Calendar.DAY_OF_MONTH)
        )
    }

    private fun utcMidnight(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day)
        }.timeInMillis

    private fun localMidnight(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day)
        }.timeInMillis


    private fun format(pattern: String, millis: Long): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
}
