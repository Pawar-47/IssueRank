package com.pawar47.issuerank.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RankingTest {

    private fun entry(id: Long, rank: Int) = Entry(id = id, rank = rank, name = "Record $id")

    private val list = listOf(entry(10, 1), entry(20, 2), entry(30, 3))

    @Test
    fun movingDownSwapsAndRenumbers() {
        val result = reordered(list, 10, 1)
        assertEquals(listOf(20L, 10L, 30L), result.map { it.id })
        assertEquals(listOf(1, 2, 3), result.map { it.rank })
    }

    @Test
    fun movingUpSwapsAndRenumbers() {
        val result = reordered(list, 30, -1)
        assertEquals(listOf(10L, 30L, 20L), result.map { it.id })
        assertEquals(listOf(1, 2, 3), result.map { it.rank })
    }

    @Test
    fun movingPastTheEdgesIsIgnored() {
        assertEquals(list.map { it.id }, reordered(list, 10, -1).map { it.id })
        assertEquals(list.map { it.id }, reordered(list, 30, 1).map { it.id })
        assertEquals(list.map { it.id }, reordered(list, 999, 1).map { it.id })
    }

    @Test
    fun undoInsertPutsTheRecordBackInPlace() {
        val withoutMiddle = listOf(entry(10, 1), entry(30, 2))
        val restored = insertedAt(withoutMiddle, entry(20, 2), 1)
        assertEquals(listOf(10L, 20L, 30L), restored.map { it.id })
        assertEquals(listOf(1, 2, 3), restored.map { it.rank })
    }

    @Test
    fun renumberClosesGapsAfterADelete() {
        val gapped = listOf(entry(10, 1), entry(20, 4), entry(30, 9))
        assertEquals(listOf(1, 2, 3), renumbered(gapped).map { it.rank })
    }
}
