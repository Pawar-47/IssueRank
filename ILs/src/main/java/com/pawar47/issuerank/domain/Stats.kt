package com.pawar47.issuerank.domain

/** Everything the dashboard shows, derived in one pass from the record list. */
data class ListStats(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0,
    val resolved: Int = 0,
    val notResolved: Int = 0,
    val overdue: Int = 0
) {
    val completionPercent: Int get() = percent(completed, total)
    val resolutionPercent: Int get() = percent(resolved, total)
    val openPercent: Int get() = percent(notResolved, total)

    val isEmpty: Boolean get() = total == 0

    private fun percent(part: Int, whole: Int): Int =
        if (whole <= 0) 0 else Math.round(part * 100f / whole)
}

data class Dashboard(
    val stats: ListStats = ListStats(),
    val priority: List<Entry> = emptyList(),
    val recentlyUpdated: List<Entry> = emptyList(),
    val needsAttention: List<Entry> = emptyList()
)

fun computeStats(entries: List<Entry>, now: Long): ListStats = ListStats(
    total = entries.size,
    pending = entries.count { it.status == Status.Pending },
    completed = entries.count { it.status == Status.Completed },
    resolved = entries.count { it.resolution == Resolution.Resolved },
    notResolved = entries.count { it.resolution == Resolution.NotResolved },
    overdue = entries.count { it.isOverdue(now) }
)

/**
 * Builds the dashboard blocks. Priority is the highest-ranked open record; needs-attention
 * prefers overdue records, then the ones that have sat untouched longest, without repeating
 * anything already shown as a priority.
 */
fun buildDashboard(entries: List<Entry>, now: Long, limit: Int = 3): Dashboard {
    val priority = entries
        .asSequence()
        .filter { it.isOpen }
        .sortedBy { it.rank }
        .take(limit)
        .toList()

    val recentlyUpdated = entries
        .asSequence()
        .filter { it.updatedAt > 0L }
        .sortedWith(compareByDescending<Entry> { it.updatedAt }.thenBy { it.rank })
        .take(limit)
        .toList()

    val priorityIds = priority.mapTo(HashSet()) { it.id }
    val needsAttention = entries
        .asSequence()
        .filter { it.isOpen && it.id !in priorityIds }
        .sortedWith(
            compareByDescending<Entry> { it.isOverdue(now) }
                .thenBy { it.dueAt ?: Long.MAX_VALUE }
                .thenBy { it.updatedAt }
        )
        .take(limit)
        .toList()

    return Dashboard(
        stats = computeStats(entries, now),
        priority = priority,
        recentlyUpdated = recentlyUpdated,
        needsAttention = needsAttention
    )
}
