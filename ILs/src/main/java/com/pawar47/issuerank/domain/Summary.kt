package com.pawar47.issuerank.domain

/** Plain-text report used by the dashboard's Share action, so data is never trapped. */
fun summaryText(
    entries: List<Entry>,
    stats: ListStats,
    now: Long,
    appName: String = "ILs"
): String {
    val header = "$appName — ${Dates.day(now)}"
    val counts = buildString {
        append("Records: ").append(stats.total)
        append(" · Pending ").append(stats.pending)
        append(" · Completed ").append(stats.completed)
        append(" · Resolved ").append(stats.resolved)
        append(" · Open ").append(stats.notResolved)
        if (stats.overdue > 0) append(" · Overdue ").append(stats.overdue)
    }
    if (entries.isEmpty()) return "$header\n$counts\n\nNo records yet."

    val lines = entries.map { entry ->
        val flags = buildString {
            append(entry.status.label)
            append(", ")
            append(entry.resolution.label)
            if (entry.isOverdue(now)) append(", overdue")
        }
        "${entry.rank}. ${entry.name} — ${entry.issueSummary.ifBlank { "No issue described" }} ($flags)"
    }
    return (listOf(header, counts, "") + lines).joinToString("\n")
}
