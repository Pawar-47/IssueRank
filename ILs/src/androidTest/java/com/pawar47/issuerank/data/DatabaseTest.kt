package com.pawar47.issuerank.data

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Data-integrity tests against a real SQLite database (in memory, so each test starts
 * clean): ranks stay contiguous through every kind of change, and timestamps behave.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var database: IssueRankDatabase
    private lateinit var dao: EntryDao

    @Before
    fun open() {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, IssueRankDatabase::class.java).build()
        dao = database.entryDao()
    }

    @After
    fun close() {
        database.close()
    }

    private fun record(name: String, at: Long = 1_000L) =
        EntryEntity(name = name, createdAt = at, updatedAt = at)

    private suspend fun names() = dao.observeAll().first().map { it.name }

    private suspend fun ranks() = dao.observeAll().first().map { it.position }

    @Test
    fun newRecordsAppendWithTheNextRank() = runBlocking {
        dao.add(record("A"))
        dao.add(record("B"))
        dao.add(record("C"))
        assertEquals(listOf("A", "B", "C"), names())
        assertEquals(listOf(1, 2, 3), ranks())
    }

    @Test
    fun movingRenumbersEverythingItDisplaced() = runBlocking {
        val a = dao.add(record("A"))
        dao.add(record("B"))
        dao.add(record("C"))

        dao.move(a, 1)
        assertEquals(listOf("B", "A", "C"), names())
        assertEquals(listOf(1, 2, 3), ranks())

        dao.move(a, 5)
        assertEquals(listOf("B", "C", "A"), names())
        assertEquals(listOf(1, 2, 3), ranks())
    }

    @Test
    fun deletingClosesTheGap() = runBlocking {
        dao.add(record("A"))
        val b = dao.add(record("B"))
        dao.add(record("C"))

        dao.deleteAndResequence(b)
        assertEquals(listOf("A", "C"), names())
        assertEquals(listOf(1, 2), ranks())
    }

    @Test
    fun undoRestoresTheRecordWithItsIdentityAndPosition() = runBlocking {
        dao.add(record("A"))
        val b = dao.add(record("B"))
        dao.add(record("C"))

        val original = dao.find(b)
        assertNotNull(original)
        dao.deleteAndResequence(b)
        assertNull(dao.find(b))

        dao.restore(original!!, position = original.position - 1)
        assertEquals(listOf("A", "B", "C"), names())
        assertEquals(listOf(1, 2, 3), ranks())
        assertEquals(b, dao.find(b)?.id)
    }

    @Test
    fun updatingKeepsTheCreationTimestamp() = runBlocking {
        val id = dao.add(record("A", at = 1_000L))
        val stored = dao.find(id)!!

        dao.update(stored.copy(name = "A2", updatedAt = 9_000L))

        val after = dao.find(id)!!
        assertEquals("A2", after.name)
        assertEquals(1_000L, after.createdAt)
        assertEquals(9_000L, after.updatedAt)
    }

    @Test
    fun idsAreStableAcrossReordering() = runBlocking {
        val a = dao.add(record("A"))
        val b = dao.add(record("B"))
        dao.move(a, 1)
        assertEquals("A", dao.find(a)?.name)
        assertEquals("B", dao.find(b)?.name)
    }
}
