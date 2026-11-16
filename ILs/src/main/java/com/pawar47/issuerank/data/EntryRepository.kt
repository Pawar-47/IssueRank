package com.pawar47.issuerank.data

import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.EntryDraft
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The single written path into storage. The UI never touches the DAO, so timestamps,
 * trimming and rank bookkeeping are applied in exactly one place.
 */
class EntryRepository(
    private val dao: EntryDao,
    private val legacyStore: LegacyStore
) {

    val entries: Flow<List<Entry>> = dao.observeAll().map { rows -> rows.map { it.toEntry() } }

    suspend fun create(draft: EntryDraft): Long {
        val now = System.currentTimeMillis()
        return dao.add(
            EntryEntity(
                name = draft.name.trim(),
                issueTitle = draft.issueTitle.trim(),
                issueDescription = draft.issueDescription.trim(),
                notes = draft.notes.trim(),
                status = draft.status.name,
                resolution = draft.resolution.name,
                createdAt = now,
                updatedAt = now,
                reportedAt = draft.reportedAt,
                dueAt = draft.dueAt,
                resolvedAt = resolvedAtFor(draft, previous = null, now = now)
            )
        )
    }

    suspend fun update(entry: Entry, draft: EntryDraft) {
        val now = System.currentTimeMillis()
        dao.update(
            entry.toEntity().copy(
                name = draft.name.trim(),
                issueTitle = draft.issueTitle.trim(),
                issueDescription = draft.issueDescription.trim(),
                notes = draft.notes.trim(),
                status = draft.status.name,
                resolution = draft.resolution.name,
                updatedAt = now,
                reportedAt = draft.reportedAt,
                dueAt = draft.dueAt,
                resolvedAt = resolvedAtFor(draft, previous = entry.resolvedAt, now = now)
            )
        )
    }

    suspend fun setStatus(entry: Entry, status: Status) {
        if (entry.status == status) return
        dao.update(entry.toEntity().copy(status = status.name, updatedAt = System.currentTimeMillis()))
    }

    suspend fun setResolution(entry: Entry, resolution: Resolution) {
        if (entry.resolution == resolution) return
        val now = System.currentTimeMillis()
        dao.update(
            entry.toEntity().copy(
                resolution = resolution.name,
                updatedAt = now,
                resolvedAt = when (resolution) {
                    Resolution.Resolved -> entry.resolvedAt ?: now
                    Resolution.NotResolved -> null
                }
            )
        )
    }

    suspend fun move(id: Long, delta: Int) = dao.move(id, delta)

    suspend fun delete(id: Long) = dao.deleteAndResequence(id)

    suspend fun restore(entry: Entry) = dao.restore(entry.toEntity(), entry.rank - 1)

    /**
     * Version 1.0 kept everything in SharedPreferences. Copy those records into the
     * database once, then drop the old document so it can never be imported twice.
     */
    suspend fun importLegacyIfNeeded() {
        if (!legacyStore.hasData()) return
        if (dao.count() > 0) {
            legacyStore.clear()
            return
        }
        val legacy = legacyStore.read()
        val now = System.currentTimeMillis()
        dao.insertAll(
            legacy.map { record ->
                EntryEntity(
                    name = record.name,
                    issueTitle = "",
                    issueDescription = record.notes,
                    notes = "",
                    status = record.status.name,
                    resolution = record.resolution.name,
                    createdAt = now,
                    updatedAt = now,
                    resolvedAt = if (record.resolution == Resolution.Resolved) now else null
                )
            }
        )
        legacyStore.clear()
    }

    private fun resolvedAtFor(draft: EntryDraft, previous: Long?, now: Long): Long? = when {
        draft.resolution == Resolution.NotResolved -> null
        draft.resolvedAt != null -> draft.resolvedAt
        previous != null -> previous
        else -> now
    }
}
