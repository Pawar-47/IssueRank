package com.pawar47.issuerank.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.Status

/**
 * Database row. `position` mirrors [Entry.rank]; it is named away from `rank` because that
 * word is a SQL window function in newer SQLite builds.
 */
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "position") val position: Int = 0,
    val name: String = "",
    val issueTitle: String = "",
    val issueDescription: String = "",
    val notes: String = "",
    val status: String = Status.Pending.name,
    val resolution: String = Resolution.NotResolved.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val reportedAt: Long? = null,
    val dueAt: Long? = null,
    val resolvedAt: Long? = null
)

fun EntryEntity.toEntry(): Entry = Entry(
    id = id,
    rank = position,
    name = name,
    issueTitle = issueTitle,
    issueDescription = issueDescription,
    notes = notes,
    status = Status.fromName(status),
    resolution = Resolution.fromName(resolution),
    createdAt = createdAt,
    updatedAt = updatedAt,
    reportedAt = reportedAt,
    dueAt = dueAt,
    resolvedAt = resolvedAt
)

fun Entry.toEntity(): EntryEntity = EntryEntity(
    id = id,
    position = rank,
    name = name,
    issueTitle = issueTitle,
    issueDescription = issueDescription,
    notes = notes,
    status = status.name,
    resolution = resolution.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    reportedAt = reportedAt,
    dueAt = dueAt,
    resolvedAt = resolvedAt
)
