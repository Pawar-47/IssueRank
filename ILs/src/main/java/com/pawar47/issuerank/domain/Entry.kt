package com.pawar47.issuerank.domain

/** Completion state. Independent of [Resolution]: a completed record can still be unresolved. */
enum class Status(val label: String) {
    Pending("Pending"),
    Completed("Completed");

    fun toggled(): Status = if (this == Pending) Completed else Pending

    companion object {
        fun fromName(value: String?): Status =
            entries.firstOrNull { it.name == value } ?: Pending
    }
}

/** Whether the underlying issue is actually resolved. */
enum class Resolution(val label: String) {
    Resolved("Resolved"),
    NotResolved("Not Resolved");

    fun toggled(): Resolution = if (this == Resolved) NotResolved else Resolved

    companion object {
        fun fromName(value: String?): Resolution =
            entries.firstOrNull { it.name == value } ?: NotResolved
    }
}

/**
 * One tracked record.
 *
 * [rank] is 1-based and always contiguous; it is maintained by the data layer so it can
 * never drift out of order. [updatedAt] moves on every write, [createdAt] never does.
 */
data class Entry(
    val id: Long = 0L,
    val rank: Int = 0,
    val name: String = "",
    val issueTitle: String = "",
    val issueDescription: String = "",
    val notes: String = "",
    val status: Status = Status.Pending,
    val resolution: Resolution = Resolution.NotResolved,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val reportedAt: Long? = null,
    val dueAt: Long? = null,
    val resolvedAt: Long? = null
) {
    val isOpen: Boolean get() = resolution == Resolution.NotResolved

    val hasIssue: Boolean
        get() = issueTitle.isNotBlank() || issueDescription.isNotBlank() || notes.isNotBlank()

    /** One-line issue summary for dense rows: the title, else the first line of the body. */
    val issueSummary: String
        get() = when {
            issueTitle.isNotBlank() -> issueTitle
            issueDescription.isNotBlank() ->
                issueDescription.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
            notes.isNotBlank() -> notes.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
            else -> ""
        }

    fun isOverdue(now: Long): Boolean = isOpen && dueAt != null && dueAt < now

    /** Progress of this single record, 0..1, used by the row-level indicator. */
    val progress: Float
        get() = (if (status == Status.Completed) 0.5f else 0f) +
            (if (resolution == Resolution.Resolved) 0.5f else 0f)
}

/** The editable half of a record, produced by the editor sheet. */
data class EntryDraft(
    val name: String = "",
    val issueTitle: String = "",
    val issueDescription: String = "",
    val notes: String = "",
    val status: Status = Status.Pending,
    val resolution: Resolution = Resolution.NotResolved,
    val reportedAt: Long? = null,
    val dueAt: Long? = null,
    val resolvedAt: Long? = null
) {
    val isValid: Boolean get() = name.isNotBlank()

    companion object {
        fun from(entry: Entry): EntryDraft = EntryDraft(
            name = entry.name,
            issueTitle = entry.issueTitle,
            issueDescription = entry.issueDescription,
            notes = entry.notes,
            status = entry.status,
            resolution = entry.resolution,
            reportedAt = entry.reportedAt,
            dueAt = entry.dueAt,
            resolvedAt = entry.resolvedAt
        )
    }
}
