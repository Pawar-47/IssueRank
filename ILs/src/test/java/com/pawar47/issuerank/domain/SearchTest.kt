package com.pawar47.issuerank.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {

    private fun entry(
        id: Long,
        name: String,
        title: String = "",
        description: String = "",
        notes: String = "",
        status: Status = Status.Pending,
        resolution: Resolution = Resolution.NotResolved
    ) = Entry(
        id = id,
        rank = id.toInt(),
        name = name,
        issueTitle = title,
        issueDescription = description,
        notes = notes,
        status = status,
        resolution = resolution,
        createdAt = 1_000L,
        updatedAt = 1_000L * id
    )

    private val sample = listOf(
        entry(
            1,
            "Priya Sharma",
            title = "Login fails",
            description = "Fails on the second attempt",
            status = Status.Completed
        ),
        entry(
            2,
            "Rohit Verma",
            title = "Export crash",
            description = "PDF export crashes on large files",
            notes = "Reproduced on the tablet"
        ),
        entry(
            3,
            "Jose Alvarez",
            title = "Login timeout",
            description = "Timeout in staging",
            resolution = Resolution.Resolved
        )
    )

    @Test
    fun findsByName() {
        val hits = search(sample, SearchQuery("priya"))
        assertEquals(1, hits.size)
        assertEquals("Priya Sharma", hits[0].entry.name)
        assertTrue(hits[0].nameMatch)
    }

    @Test
    fun findsByIssueTitle() {
        val hits = search(sample, SearchQuery("export crash"))
        assertEquals(1, hits.size)
        assertEquals("Rohit Verma", hits[0].entry.name)
        assertTrue(hits[0].issueMatch)
    }

    @Test
    fun findsByDescriptionAndNotes() {
        assertEquals(1, search(sample, SearchQuery("tablet")).size)
        assertEquals(1, search(sample, SearchQuery("second attempt")).size)
    }

    @Test
    fun requiresEveryTerm() {
        assertEquals(2, search(sample, SearchQuery("login")).size)
        assertEquals(1, search(sample, SearchQuery("login staging")).size)
        assertEquals(0, search(sample, SearchQuery("login export")).size)
    }

    @Test
    fun ignoresCaseAndAccents() {
        assertEquals(1, search(sample, SearchQuery("JOSE")).size)
        assertEquals(1, search(sample, SearchQuery("josé")).size)
    }

    @Test
    fun scopeRestrictsFields() {
        assertEquals(0, search(sample, SearchQuery("login", scope = SearchScope.Name)).size)
        assertEquals(2, search(sample, SearchQuery("login", scope = SearchScope.Issues)).size)
        assertEquals(1, search(sample, SearchQuery("priya", scope = SearchScope.Name)).size)
    }

    @Test
    fun nameHitsOutrankIssueOnlyHits() {
        val entries = listOf(
            entry(1, "Export queue", title = "unrelated"),
            entry(2, "Alpha", title = "export fails")
        )
        val hits = search(entries, SearchQuery("export"))
        assertEquals("Export queue", hits[0].entry.name)
    }

    @Test
    fun filtersByResolutionAndStatus() {
        assertEquals(1, search(sample, SearchQuery("login", filter = SearchFilter.Open)).size)
        assertEquals(1, search(sample, SearchQuery("login", filter = SearchFilter.Resolved)).size)
        assertEquals(1, search(sample, SearchQuery("login", filter = SearchFilter.Completed)).size)
        assertEquals(1, search(sample, SearchQuery("login", filter = SearchFilter.Pending)).size)
    }

    @Test
    fun emptyQueryListsEverythingThatPassesTheFilter() {
        assertEquals(3, search(sample, SearchQuery("   ")).size)
        assertEquals(2, search(sample, SearchQuery("", filter = SearchFilter.Open)).size)
        assertEquals(3, search(sample, SearchQuery("", filter = SearchFilter.Any)).size)
    }

    @Test
    fun sortOrdersAreApplied() {
        val byRank = search(sample, SearchQuery("login", sort = SearchSort.Rank))
        assertEquals(listOf(1L, 3L), byRank.map { it.entry.id })

        val byUpdated = search(sample, SearchQuery("login", sort = SearchSort.Updated))
        assertEquals(listOf(3L, 1L), byUpdated.map { it.entry.id })

        val byName = search(sample, SearchQuery("login", sort = SearchSort.Name))
        assertEquals(listOf(3L, 1L), byName.map { it.entry.id })
    }

    @Test
    fun matchRangesAreFoundForHighlighting() {
        assertEquals(listOf(0..2), matchRanges("Login fails", listOf("log")))
        assertEquals(listOf(6..10), matchRanges("Login fails", listOf("fails")))
        assertTrue(matchRanges("Login", emptyList()).isEmpty())
    }

    @Test
    fun snippetKeepsTheMatchInView() {
        val long = "a".repeat(120) + " needle " + "b".repeat(120)
        val result = snippet(long, listOf("needle"), radius = 10)
        assertTrue(result.contains("needle"))
        assertTrue(result.startsWith("…"))
        assertTrue(result.length < long.length)
    }
}
