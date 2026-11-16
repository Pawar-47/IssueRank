package com.pawar47.issuerank.domain

/**
 * Rank rules, kept pure so both the database transactions and the tests use the same code.
 * Ranks are 1-based and contiguous; the list order is the ranking.
 */

/** Moves [id] by [delta] (−1 up, +1 down) and renumbers the whole list. */
fun reordered(items: List<Entry>, id: Long, delta: Int): List<Entry> {
    val from = items.indexOfFirst { it.id == id }
    if (from < 0) return items
    val to = (from + delta).coerceIn(0, items.lastIndex)
    if (to == from) return items
    val list = items.toMutableList()
    val moved = list.removeAt(from)
    list.add(to, moved)
    return list.mapIndexed { index, entry -> entry.copy(rank = index + 1) }
}

/** Inserts [entry] at [position] (used by undo) and renumbers the list. */
fun insertedAt(items: List<Entry>, entry: Entry, position: Int): List<Entry> {
    val list = items.toMutableList()
    list.add(position.coerceIn(0, list.size), entry)
    return list.mapIndexed { index, item -> item.copy(rank = index + 1) }
}

/** Renumbers a list whose ranks may have gaps (after a delete, or an import). */
fun renumbered(items: List<Entry>): List<Entry> =
    items.mapIndexed { index, entry -> entry.copy(rank = index + 1) }
