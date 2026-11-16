package com.pawar47.issuerank.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentsTest {

    @Test
    fun encodeAndDecodeRoundTrip() {
        val values = listOf("login timeout", "export crash", "priya")
        assertEquals(values, Recents.decode(Recents.encode(values)))
    }

    @Test
    fun blankQueriesAreIgnored() {
        assertEquals(listOf("a"), Recents.add(listOf("a"), "   "))
    }

    @Test
    fun newestEntryGoesFirstAndDuplicatesMoveUp() {
        val first = Recents.add(emptyList(), "login")
        val second = Recents.add(first, "export")
        assertEquals(listOf("export", "login"), second)

        val repeated = Recents.add(second, "login")
        assertEquals(listOf("login", "export"), repeated)
    }

    @Test
    fun duplicatesAreCaseInsensitive() {
        val values = Recents.add(listOf("Login"), "login")
        assertEquals(listOf("login"), values)
    }

    @Test
    fun historyIsCapped() {
        var values = emptyList<String>()
        repeat(Recents.MAX + 4) { index -> values = Recents.add(values, "query $index") }
        assertEquals(Recents.MAX, values.size)
        assertEquals("query ${Recents.MAX + 3}", values.first())
    }

    @Test
    fun removalIsCaseInsensitive() {
        assertEquals(listOf("export"), Recents.remove(listOf("Login", "export"), "login"))
    }

    @Test
    fun decodeHandlesNullAndBlank() {
        assertTrue(Recents.decode(null).isEmpty())
        assertTrue(Recents.decode("").isEmpty())
    }
}
