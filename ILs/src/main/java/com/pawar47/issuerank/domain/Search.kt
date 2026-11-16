package com.pawar47.issuerank.domain

import java.text.Normalizer

enum class SearchScope(val label: String) {
    All("All"),
    Name("Name"),
    Issues("Issues")
}

enum class SearchFilter(val label: String) {
    Any("Any"),
    Open("Open"),
    Resolved("Resolved"),
    Pending("Pending"),
    Completed("Completed")
}

enum class SearchSort(val label: String) {
    Relevance("Best match"),
    Rank("Rank"),
    Updated("Recently updated"),
    Name("Name")
}

data class SearchQuery(
    val text: String = "",
    val scope: SearchScope = SearchScope.All,
    val filter: SearchFilter = SearchFilter.Any,
    val sort: SearchSort = SearchSort.Relevance
) {
    val isEmpty: Boolean get() = text.isBlank()
}

/** One record that matched, with the inputs used to order and decorate the row. */
data class SearchHit(
    val entry: Entry,
    val rank: Int,
    val score: Int,
    val nameMatch: Boolean,
    val issueMatch: Boolean
)

/**
 * Folds text for matching: lower case, decomposed, with combining marks dropped, so
 * "jose" finds "José". Length is preserved for Latin text, which lets match offsets be
 * reused on the original string for highlighting.
 */
fun String.foldForSearch(): String {
    val decomposed = Normalizer.normalize(this, Normalizer.Form.NFD)
    val out = StringBuilder(decomposed.length)
    for (ch in decomposed) {
        when (Character.getType(ch)) {
            Character.NON_SPACING_MARK.toInt(),
            Character.COMBINING_SPACING_MARK.toInt(),
            Character.ENCLOSING_MARK.toInt() -> Unit

            else -> out.append(ch.lowercaseChar())
        }
    }
    return out.toString()
}

/** Splits a query into folded terms. Every term must match (AND), in any order. */
fun searchTerms(query: String): List<String> =
    query.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .map { it.foldForSearch() }

fun matchesFilter(entry: Entry, filter: SearchFilter): Boolean = when (filter) {
    SearchFilter.Any -> true
    SearchFilter.Open -> entry.resolution == Resolution.NotResolved
    SearchFilter.Resolved -> entry.resolution == Resolution.Resolved
    SearchFilter.Pending -> entry.status == Status.Pending
    SearchFilter.Completed -> entry.status == Status.Completed
}

private fun startsAnyWord(text: String, term: String): Boolean {
    if (term.isEmpty()) return false
    var from = 0
    while (from <= text.length - term.length) {
        val index = text.indexOf(term, from)
        if (index < 0) return false
        if (index == 0 || !text[index - 1].isLetterOrDigit()) return true
        from = index + 1
    }
    return false
}

/**
 * Searches name, issue title, issue description and notes. A name hit always outranks an
 * issue-only hit, and ties keep rank order, so results read like the list itself.
 */
fun search(entries: List<Entry>, query: SearchQuery): List<SearchHit> {
    val terms = searchTerms(query.text)
    val candidates = entries.filter { matchesFilter(it, query.filter) }

    if (terms.isEmpty()) {
        val hits = candidates.map { SearchHit(it, it.rank, 0, false, false) }
        return sortHits(hits, query.sort)
    }

    val results = ArrayList<SearchHit>(candidates.size)
    for (entry in candidates) {
        val name = entry.name.foldForSearch()
        val title = entry.issueTitle.foldForSearch()
        val description = entry.issueDescription.foldForSearch()
        val notes = entry.notes.foldForSearch()

        var score = 0
        var nameMatch = false
        var issueMatch = false
        var matchedEveryTerm = true

        for (term in terms) {
            val inName = query.scope != SearchScope.Issues && name.contains(term)
            val inIssue = query.scope != SearchScope.Name &&
                (title.contains(term) || description.contains(term) || notes.contains(term))

            if (!inName && !inIssue) {
                matchedEveryTerm = false
                break
            }
            if (inName) {
                nameMatch = true
                score += when {
                    name == term -> 40
                    name.startsWith(term) -> 24
                    startsAnyWord(name, term) -> 16
                    else -> 8
                }
            }
            if (inIssue) {
                issueMatch = true
                score += when {
                    title.contains(term) -> if (startsAnyWord(title, term)) 10 else 6
                    description.contains(term) -> if (startsAnyWord(description, term)) 5 else 3
                    notes.contains(term) -> 2
                    else -> 1
                }
            }
        }

        if (matchedEveryTerm) {
            results.add(SearchHit(entry, entry.rank, score, nameMatch, issueMatch))
        }
    }

    return sortHits(results, query.sort)
}

private fun sortHits(hits: List<SearchHit>, sort: SearchSort): List<SearchHit> = when (sort) {
    SearchSort.Relevance ->
        hits.sortedWith(compareByDescending<SearchHit> { it.score }.thenBy { it.rank })

    SearchSort.Rank -> hits.sortedBy { it.rank }
    SearchSort.Updated -> hits.sortedByDescending { it.entry.updatedAt }
    SearchSort.Name -> hits.sortedBy { it.entry.name.foldForSearch() }
}

/** Character ranges of [text] that match any term, for highlighting. */
fun matchRanges(text: String, terms: List<String>): List<IntRange> {
    if (text.isEmpty() || terms.isEmpty()) return emptyList()
    val folded = text.foldForSearch()
    val ranges = ArrayList<IntRange>()
    for (term in terms) {
        if (term.isEmpty()) continue
        var from = 0
        while (from <= folded.length - term.length) {
            val index = folded.indexOf(term, from)
            if (index < 0) break
            ranges.add(index until (index + term.length))
            from = index + term.length
        }
    }
    return ranges.sortedBy { it.first }
}

/** Narrows long body text to a window around the first match, with ellipses. */
fun snippet(text: String, terms: List<String>, radius: Int = 52): String {
    if (text.isEmpty() || terms.isEmpty() || text.length <= radius * 2) return text
    val folded = text.foldForSearch()
    var first = -1
    for (term in terms) {
        if (term.isEmpty()) continue
        val index = folded.indexOf(term)
        if (index >= 0 && (first < 0 || index < first)) first = index
    }
    if (first < 0) return text
    val start = (first - radius).coerceAtLeast(0)
    val end = (first + radius + 16).coerceAtMost(text.length)
    val prefix = if (start > 0) "…" else ""
    val suffix = if (end < text.length) "…" else ""
    return prefix + text.substring(start, end).trim() + suffix
}
