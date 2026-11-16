package com.pawar47.issuerank.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {

    private val now = 1_700_000_000_000L

    private fun entry(
        id: Long,
        status: Status = Status.Pending,
        resolution: Resolution = Resolution.NotResolved,
        dueAt: Long? = null,
        updatedAt: Long = 1_000L
    ) = Entry(
        id = id,
        rank = id.toInt(),
        name = "Record $id",
        status = status,
        resolution = resolution,
        createdAt = 1_000L,
        updatedAt = updatedAt,
        dueAt = dueAt
    )

    @Test
    fun countsEveryAxis() {
        val entries = listOf(
            entry(1),
            entry(2, status = Status.Completed),
            entry(3, resolution = Resolution.Resolved),
            entry(4, status = Status.Completed, resolution = Resolution.Resolved)
        )
        val stats = computeStats(entries, now)

        assertEquals(4, stats.total)
        assertEquals(2, stats.pending)
        assertEquals(2, stats.completed)
        assertEquals(2, stats.resolved)
        assertEquals(2, stats.notResolved)
        assertEquals(50, stats.completionPercent)
        assertEquals(50, stats.resolutionPercent)
    }

    @Test
    fun emptyListGivesZeroPercentagesNotDivideByZero() {
        val stats = computeStats(emptyList(), now)
        assertEquals(0, stats.total)
        assertEquals(0, stats.completionPercent)
        assertEquals(0, stats.resolutionPercent)
        assertTrue(stats.isEmpty)
    }

    @Test
    fun overdueCountsOnlyOpenRecordsPastTheirDate() {
        val entries = listOf(
            entry(1, dueAt = now - 1_000L),
            entry(2, dueAt = now + 1_000L),
            entry(3, dueAt = now - 1_000L, resolution = Resolution.Resolved)
        )
        assertEquals(1, computeStats(entries, now).overdue)
    }

    @Test
    fun dashboardPriorityIsHighestRankedOpenRecord() {
        val entries = listOf(
            entry(1, resolution = Resolution.Resolved),
            entry(2),
            entry(3),
            entry(4),
            entry(5)
        )
        val dashboard = buildDashboard(entries, now, limit = 2)
        assertEquals(listOf(2L, 3L), dashboard.priority.map { it.id })
    }

    @Test
    fun dashboardRecentlyUpdatedIsNewestFirst() {
        val entries = listOf(
            entry(1, updatedAt = 100L),
            entry(2, updatedAt = 300L),
            entry(3, updatedAt = 200L)
        )
        val dashboard = buildDashboard(entries, now)
        assertEquals(listOf(2L, 3L, 1L), dashboard.recentlyUpdated.map { it.id })
    }

    @Test
    fun needsAttentionSkipsPriorityEntriesAndPrefersOverdue() {
        val entries = listOf(
            entry(1, dueAt = now - 10_000L),
            entry(2),
            entry(3, dueAt = now - 5_000L)
        )
        val dashboard = buildDashboard(entries, now, limit = 1)
        // Record 1 is the top priority, so attention skips it and surfaces the overdue record 3.
        assertEquals(listOf(1L), dashboard.priority.map { it.id })
        assertEquals(listOf(3L), dashboard.needsAttention.map { it.id })

        // With room for everything, attention never repeats a priority record.
        val wider = buildDashboard(entries, now, limit = 3)
        assertTrue(wider.needsAttention.isEmpty())
    }
}
