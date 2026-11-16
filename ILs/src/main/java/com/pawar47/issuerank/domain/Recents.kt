package com.pawar47.issuerank.domain

/**
 * Recent search history: stored as one delimited string so it survives DataStore without
 * any JSON dependency, and can be unit tested as pure text in and out.
 */
object Recents {
    const val MAX = 8
    private const val SEPARATOR = "\u001F"

    fun encode(values: List<String>): String = values.joinToString(SEPARATOR)

    fun decode(value: String?): List<String> =
        value?.split(SEPARATOR)?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

    /** Adds [query] to the front, case-insensitively de-duplicated and capped at [MAX]. */
    fun add(values: List<String>, query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return values
        return (listOf(trimmed) + values.filterNot { it.equals(trimmed, ignoreCase = true) })
            .take(MAX)
    }

    fun remove(values: List<String>, query: String): List<String> =
        values.filterNot { it.equals(query, ignoreCase = true) }
}
