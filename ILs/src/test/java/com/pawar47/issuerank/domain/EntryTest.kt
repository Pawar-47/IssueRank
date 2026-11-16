package com.pawar47.issuerank.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryTest {

    @Test
    fun issueSummaryPrefersTitleThenDescriptionThenNotes() {
        assertEquals("Title", Entry(issueTitle = "Title", issueDescription = "Body").issueSummary)
        assertEquals(
            "First line",
            Entry(issueDescription = "\nFirst line\nSecond line").issueSummary
        )
        assertEquals("Only a note", Entry(notes = "Only a note").issueSummary)
        assertEquals("", Entry().issueSummary)
    }

    @Test
    fun hasIssueReflectsAnyIssueText() {
        assertFalse(Entry().hasIssue)
        assertTrue(Entry(notes = "x").hasIssue)
    }

    @Test
    fun statusAndResolutionAreIndependent() {
        val entry = Entry(status = Status.Completed, resolution = Resolution.NotResolved)
        assertTrue(entry.isOpen)
        assertEquals(0.5f, entry.progress, 0.0001f)
    }

    @Test
    fun togglesFlipBetweenTheTwoStates() {
        assertEquals(Status.Completed, Status.Pending.toggled())
        assertEquals(Status.Pending, Status.Completed.toggled())
        assertEquals(Resolution.Resolved, Resolution.NotResolved.toggled())
        assertEquals(Resolution.NotResolved, Resolution.Resolved.toggled())
    }

    @Test
    fun unknownStoredValuesFallBackSafely() {
        assertEquals(Status.Pending, Status.fromName("garbage"))
        assertEquals(Status.Pending, Status.fromName(null))
        assertEquals(Resolution.NotResolved, Resolution.fromName("garbage"))
    }

    @Test
    fun overdueNeedsAnOpenRecordWithAPastDate() {
        val now = 10_000L
        assertTrue(Entry(dueAt = 5_000L).isOverdue(now))
        assertFalse(Entry(dueAt = 20_000L).isOverdue(now))
        assertFalse(Entry().isOverdue(now))
        assertFalse(Entry(dueAt = 5_000L, resolution = Resolution.Resolved).isOverdue(now))
    }

    @Test
    fun draftRequiresANameAndRoundTripsAnEntry() {
        assertFalse(EntryDraft(name = "  ").isValid)
        assertTrue(EntryDraft(name = "Priya").isValid)

        val entry = Entry(
            id = 7,
            name = "Priya",
            issueTitle = "Login",
            notes = "n",
            dueAt = 123L
        )
        val draft = EntryDraft.from(entry)
        assertEquals("Priya", draft.name)
        assertEquals(123L, draft.dueAt)
    }

    @Test
    fun summaryTextListsEveryRecordInRankOrder() {
        val entries = listOf(
            Entry(id = 1, rank = 1, name = "Priya", issueTitle = "Login fails"),
            Entry(id = 2, rank = 2, name = "Rohit", status = Status.Completed)
        )
        val text = summaryText(entries, computeStats(entries, 0L), now = 0L)
        assertTrue(text.contains("1. Priya — Login fails"))
        assertTrue(text.contains("2. Rohit — No issue described (Completed, Not Resolved)"))
        assertTrue(text.contains("Records: 2"))
    }
}
