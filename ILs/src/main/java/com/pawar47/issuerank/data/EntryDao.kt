package com.pawar47.issuerank.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pawar47.issuerank.domain.insertedAt
import com.pawar47.issuerank.domain.reordered
import kotlinx.coroutines.flow.Flow

/**
 * Every write that touches more than one row runs inside a transaction, so the rank column
 * can never be left with gaps or duplicates, and a crash mid-write rolls back cleanly.
 */
@Dao
abstract class EntryDao {

    @Query("SELECT * FROM entries ORDER BY position ASC")
    abstract fun observeAll(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries ORDER BY position ASC")
    abstract suspend fun listAll(): List<EntryEntity>

    @Query("SELECT * FROM entries WHERE id = :id")
    abstract suspend fun find(id: Long): EntryEntity?

    @Query("SELECT COUNT(*) FROM entries")
    abstract suspend fun count(): Int

    @Insert
    abstract suspend fun insert(entity: EntryEntity): Long

    @Update
    abstract suspend fun update(entity: EntryEntity)

    @Query("DELETE FROM entries WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Query("UPDATE entries SET position = :position WHERE id = :id")
    abstract suspend fun setPosition(id: Long, position: Int)

    /** Appends at the end of the list, assigning the next rank. */
    @Transaction
    open suspend fun add(entity: EntryEntity): Long {
        val next = (listAll().maxOfOrNull { it.position } ?: 0) + 1
        return insert(entity.copy(id = 0L, position = next))
    }

    /** Moves a record one step and renumbers everything it displaced. */
    @Transaction
    open suspend fun move(id: Long, delta: Int) {
        val before = listAll().map { it.toEntry() }
        val after = reordered(before, id, delta)
        if (after.map { it.id } == before.map { it.id }) return
        after.forEach { entry ->
            if (entry.rank != before.first { it.id == entry.id }.rank) {
                setPosition(entry.id, entry.rank)
            }
        }
    }

    @Transaction
    open suspend fun deleteAndResequence(id: Long) {
        deleteById(id)
        listAll().forEachIndexed { index, entity ->
            if (entity.position != index + 1) setPosition(entity.id, index + 1)
        }
    }

    /** Puts a deleted record back where it was, keeping its original identifier. */
    @Transaction
    open suspend fun restore(entity: EntryEntity, position: Int) {
        val existing = listAll().map { it.toEntry() }
        val merged = insertedAt(existing, entity.toEntry(), position)
        merged.forEach { entry ->
            if (entry.id != entity.id) setPosition(entry.id, entry.rank)
        }
        val restoredRank = merged.firstOrNull { it.id == entity.id }?.rank ?: (position + 1)
        insert(entity.copy(position = restoredRank))
    }

    /** Bulk insert used once, when upgrading from the pre-database version of the app. */
    @Transaction
    open suspend fun insertAll(entities: List<EntryEntity>) {
        entities.forEachIndexed { index, entity ->
            insert(entity.copy(id = 0L, position = index + 1))
        }
    }
}
