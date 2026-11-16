package com.pawar47.issuerank.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.pawar47.issuerank.SearchUiState
import com.pawar47.issuerank.domain.Dates
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.SearchFilter
import com.pawar47.issuerank.domain.SearchHit
import com.pawar47.issuerank.domain.SearchScope
import com.pawar47.issuerank.domain.SearchSort
import com.pawar47.issuerank.domain.snippet
import com.pawar47.issuerank.domain.searchTerms

private fun resultShape(index: Int, count: Int): Shape = when {
    count <= 1 -> RoundedCornerShape(16.dp)
    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    index == count - 1 -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    else -> RectangleShape
}

/**
 * Global search. One field, three scopes (everything / name / issues), five filters and four
 * sort orders; results are highlighted and open straight into the editor sheet.
 */
@Composable
fun SearchOverlay(
    state: SearchUiState,
    recents: List<String>,
    now: Long,
    onQueryChange: (String) -> Unit,
    onScopeChange: (SearchScope) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onSortChange: (SearchSort) -> Unit,
    onSubmit: () -> Unit,
    onRecent: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearRecents: () -> Unit,
    onOpenEntry: (Entry) -> Unit,
    onClose: () -> Unit
) {
    val colors = Ios.colors
    val query = state.query
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val terms = remember(query.text) { searchTerms(query.text) }

    BackHandler { onClose() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        Column(Modifier.fillMaxWidth().statusBarsPadding()) {
            // search field
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(colors.fill)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchGlyph(colors.secondaryLabel, size = 15.dp)
                    Spacer(Modifier.width(6.dp))
                    BasicTextField(
                        value = query.text,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        textStyle = IosType.body.copy(color = colors.label),
                        cursorBrush = SolidColor(colors.blue),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                        decorationBox = { innerTextField ->
                            Box(Modifier.fillMaxWidth()) {
                                if (query.text.isEmpty()) {
                                    IText(
                                        "Search names and issues",
                                        style = IosType.body,
                                        color = colors.tertiaryLabel,
                                        maxLines = 1
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (query.text.isNotEmpty()) {
                        Box(
                            Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(colors.tertiaryLabel.copy(alpha = 0.35f))
                                .iosClick { onQueryChange("") },
                            contentAlignment = Alignment.Center
                        ) {
                            CloseGlyph(colors.card, size = 8.dp)
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                IText(
                    "Cancel",
                    modifier = Modifier
                        .iosClick { onClose() }
                        .padding(vertical = 8.dp),
                    style = IosType.body,
                    color = colors.blue
                )
            }

            SegmentedControl(
                options = SearchScope.entries.map { it.label },
                selectedIndex = query.scope.ordinal,
                onSelect = { onScopeChange(SearchScope.entries[it]) },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchFilter.entries.forEach { filter ->
                    FilterChip(
                        label = filter.label,
                        selected = query.filter == filter,
                        onClick = { onFilterChange(filter) }
                    )
                }
                val nextSort = SearchSort.entries[(query.sort.ordinal + 1) % SearchSort.entries.size]
                FilterChip(
                    label = "Sort: ${query.sort.label}",
                    selected = query.sort != SearchSort.Relevance,
                    onClick = { onSortChange(nextSort) }
                )
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (query.isEmpty && query.filter == SearchFilter.Any) {
                item(key = "recents-header") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IText(
                            "RECENT SEARCHES",
                            modifier = Modifier.weight(1f),
                            style = IosType.footnote.copy(fontWeight = FontWeight.Medium),
                            color = colors.secondaryLabel,
                            maxLines = 1
                        )
                        if (recents.isNotEmpty()) {
                            IText(
                                "Clear",
                                modifier = Modifier.iosClick { onClearRecents() },
                                style = IosType.footnote,
                                color = colors.blue,
                                maxLines = 1
                            )
                        }
                    }
                }
                if (recents.isEmpty()) {
                    item(key = "recents-empty") {
                        EmptyState(
                            tab = AppTab.Issues,
                            title = "Search your records",
                            message = "Try a name, an issue title, words from a description or a note. " +
                                "Searches you run are kept here."
                        )
                    }
                } else {
                    items(recents.size, key = { "recent-$it" }) { index ->
                        val recent = recents[index]
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .iosClick { onRecent(recent) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SearchGlyph(colors.tertiaryLabel, size = 14.dp)
                                Spacer(Modifier.width(10.dp))
                                IText(
                                    recent,
                                    modifier = Modifier.weight(1f),
                                    style = IosType.body,
                                    color = colors.label,
                                    maxLines = 1
                                )
                                Box(
                                    Modifier
                                        .size(22.dp)
                                        .iosClick { onRemoveRecent(recent) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    CloseGlyph(colors.tertiaryLabel, size = 10.dp)
                                }
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 40.dp)
                                    .height(0.5.dp)
                                    .background(colors.separator)
                            )
                        }
                    }
                }
            } else {
                item(key = "results-header") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IText(
                            "${state.results.size} ${if (state.results.size == 1) "result" else "results"}",
                            modifier = Modifier.weight(1f),
                            style = IosType.footnote.copy(fontWeight = FontWeight.Medium),
                            color = colors.secondaryLabel,
                            maxLines = 1
                        )
                        if (query.filter != SearchFilter.Any || query.scope != SearchScope.All) {
                            IText(
                                "Reset",
                                modifier = Modifier.iosClick {
                                    onFilterChange(SearchFilter.Any)
                                    onScopeChange(SearchScope.All)
                                },
                                style = IosType.footnote,
                                color = colors.blue,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (state.results.isEmpty()) {
                    item(key = "no-results") {
                        EmptyState(
                            tab = AppTab.Issues,
                            title = if (query.isEmpty) {
                                "Nothing matches this filter"
                            } else {
                                "No results for “${query.text.trim()}”"
                            },
                            message = "Check the spelling, search All fields instead of just one, " +
                                "or clear the filters."
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.results,
                        key = { _, hit -> "result-${hit.entry.id}" }
                    ) { index, hit ->
                        SearchResultRow(
                            hit = hit,
                            terms = terms,
                            now = now,
                            index = index,
                            count = state.results.size,
                            onOpen = { onOpenEntry(hit.entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    hit: SearchHit,
    terms: List<String>,
    now: Long,
    index: Int,
    count: Int,
    onOpen: () -> Unit
) {
    val colors = Ios.colors
    val entry = hit.entry
    Box(
        Modifier
            .padding(start = 16.dp, end = 16.dp)
            .clip(resultShape(index, count))
            .background(colors.card)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .iosClick(onClick = onOpen)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    HighlightedText(
                        text = entry.name,
                        terms = terms,
                        style = IosType.headline,
                        color = colors.label,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusTag(entry.status)
            }
            Spacer(Modifier.height(3.dp))
            HighlightedText(
                text = snippet(entry.issueSummary.ifBlank { "No issue described" }, terms),
                terms = terms,
                style = IosType.subhead,
                color = colors.secondaryLabel,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IText(
                    "#${entry.rank}",
                    style = IosType.footnote,
                    color = colors.secondaryLabel,
                    maxLines = 1
                )
                Spacer(Modifier.width(6.dp))
                IText("·", style = IosType.footnote, color = colors.tertiaryLabel, maxLines = 1)
                Spacer(Modifier.width(6.dp))
                MetaLine(Dates.relative(entry.updatedAt, now), overdue = entry.isOverdue(now))
                Spacer(Modifier.weight(1f))
                ResolutionTag(entry.resolution == Resolution.Resolved)
            }
        }
        if (index != count - 1) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 16.dp)
                    .height(0.5.dp)
                    .background(colors.separator)
            )
        }
    }
}
