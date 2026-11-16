package com.pawar47.issuerank.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pawar47.issuerank.data.Entry
import com.pawar47.issuerank.data.Resolution
import com.pawar47.issuerank.data.Status

// ---------------------------------------------------------------- rank

@Composable
fun RankScreen(
    entries: List<Entry>,
    onOpen: (Long) -> Unit,
    onMove: (Long, Int) -> Unit,
    onAdd: () -> Unit
) {
    val colors = Ios.colors
    var editing by rememberSaveable { mutableStateOf(false) }

    val leadingSlot: (@Composable () -> Unit) = {
        if (entries.size > 1) {
            GlassTextButton(
                text = if (editing) "Done" else "Edit",
                onClick = { editing = !editing }
            )
        }
    }

    LargeTitleScreen(
        title = "Rank",
        subtitle = "${entries.size} ${if (entries.size == 1) "entry" else "entries"} · highest first",
        onAdd = onAdd,
        leading = leadingSlot
    ) {
        when {
            entries.isEmpty() -> item(key = "empty") {
                EmptyState(
                    tab = AppTab.Rank,
                    title = "No entries yet",
                    message = "Tap + to add the first person, ticket or issue to rank."
                )
            }

            else -> {
                item(key = "rank-section") {
                    SectionHeader(
                        if (editing) "Use the arrows to reorder" else "Top priority first",
                        Modifier.padding(horizontal = 16.dp)
                    )
                }
                item(key = "rank-card") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        entries.forEachIndexed { index, entry ->
                            RankRow(
                                entry = entry,
                                rank = index + 1,
                                editing = editing,
                                canMoveUp = index > 0,
                                canMoveDown = index < entries.lastIndex,
                                showDivider = index != entries.lastIndex,
                                onOpen = { onOpen(entry.id) },
                                onMoveUp = { onMove(entry.id, -1) },
                                onMoveDown = { onMove(entry.id, 1) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankRow(
    entry: Entry,
    rank: Int,
    editing: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    showDivider: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val colors = Ios.colors
    ListRow(onClick = if (editing) null else onOpen, showDivider = showDivider) {
        RankBadge(rank)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            IText(entry.name, style = IosType.headline, color = colors.label, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            IText(
                entry.notes.ifBlank { entry.status.label },
                style = IosType.footnote,
                color = colors.secondaryLabel,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        if (editing) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canMoveUp) {
                    GlassCircleButton(onClick = onMoveUp, size = 30.dp) {
                        ChevronGlyph(colors.label, rotation = 180f, size = 12.dp)
                    }
                }
                if (canMoveUp && canMoveDown) Spacer(Modifier.width(6.dp))
                if (canMoveDown) {
                    GlassCircleButton(onClick = onMoveDown, size = 30.dp) {
                        ChevronGlyph(colors.label, rotation = 0f, size = 12.dp)
                    }
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                StatusTag(entry.status)
                Spacer(Modifier.height(3.dp))
                ResolutionTag(entry.resolution == Resolution.Resolved)
            }
        }
    }
}

// ---------------------------------------------------------------- name

@Composable
fun NameScreen(
    entries: List<Entry>,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit
) {
    val colors = Ios.colors
    var query by rememberSaveable { mutableStateOf("") }

    val ranks: Map<Long, Int> = remember(entries.toList()) {
        entries.mapIndexed { index, entry -> entry.id to index + 1 }.toMap()
    }
    val groups: List<Pair<String, List<Entry>>> = remember(entries.toList(), query) {
        entries
            .filter { entry ->
                query.isBlank() ||
                    entry.name.contains(query, ignoreCase = true) ||
                    entry.notes.contains(query, ignoreCase = true)
            }
            .groupBy { entry ->
                val c = entry.name.trim().firstOrNull()?.uppercaseChar()
                if (c != null && c in 'A'..'Z') c.toString() else "#"
            }
            .entries
            .sortedBy { it.key }
            .map { it.key to it.value }
    }

    LargeTitleScreen(
        title = "Name",
        subtitle = "Alphabetical index of every entry",
        onAdd = onAdd
    ) {
        item(key = "search") {
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.fill)
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchGlyph(colors.secondaryLabel, size = 16.dp)
                Spacer(Modifier.width(8.dp))
                IosTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search",
                    modifier = Modifier.weight(1f),
                    minHeight = 20.dp
                )
            }
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Name,
                    title = "No names yet",
                    message = "Entries you add appear here, grouped A–Z."
                )
            }
        } else if (groups.isEmpty()) {
            item(key = "no-results") {
                EmptyState(
                    tab = AppTab.Name,
                    title = "No matches",
                    message = "Nothing matches “$query”."
                )
            }
        } else {
            groups.forEach { (letter, groupEntries) ->
                item(key = "header-$letter") {
                    SectionHeader(letter, Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp))
                }
                item(key = "card-$letter") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        groupEntries.forEachIndexed { index, entry ->
                            ListRow(
                                onClick = { onOpen(entry.id) },
                                showDivider = index != groupEntries.lastIndex
                            ) {
                                InitialAvatar(entry.name, entry.id)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    IText(entry.name, style = IosType.headline, color = colors.label, maxLines = 1)
                                    Spacer(Modifier.height(2.dp))
                                    IText(
                                        "Rank #${ranks[entry.id] ?: 0} · ${entry.status.label}",
                                        style = IosType.footnote,
                                        color = colors.secondaryLabel,
                                        maxLines = 1
                                    )
                                }
                                ResolutionTag(entry.resolution == Resolution.Resolved)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- issues

@Composable
fun IssuesScreen(
    entries: List<Entry>,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf(0) }
    val shown = when (filter) {
        1 -> entries.filter { it.resolution == Resolution.NotResolved }
        2 -> entries.filter { it.resolution == Resolution.Resolved }
        else -> entries
    }
    val unresolved = entries.count { it.resolution == Resolution.NotResolved }

    LargeTitleScreen(
        title = "Issues",
        subtitle = "$unresolved open · ${entries.size - unresolved} resolved",
        onAdd = onAdd
    ) {
        item(key = "filter") {
            SegmentedControl(
                options = listOf("All", "Open", "Resolved"),
                selectedIndex = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Issues,
                    title = "No issues recorded",
                    message = "Every entry gets an issue description and room for short notes."
                )
            }
        } else if (shown.isEmpty()) {
            item(key = "none") {
                EmptyState(
                    tab = AppTab.Issues,
                    title = "Nothing here",
                    message = "This filter has no entries right now."
                )
            }
        } else {
            shown.forEach { entry ->
                item(key = "issue-${entry.id}") {
                    IssueCard(entry = entry, onOpen = { onOpen(entry.id) })
                }
            }
        }
    }
}

@Composable
private fun IssueCard(entry: Entry, onOpen: () -> Unit) {
    val colors = Ios.colors
    Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card)
                .iosClick(onClick = onOpen)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IText(
                    entry.name,
                    modifier = Modifier.weight(1f),
                    style = IosType.headline,
                    color = colors.label,
                    maxLines = 1
                )
                StatusTag(entry.status)
            }
            Spacer(Modifier.height(6.dp))
            IText(
                entry.notes.ifBlank { "No issue description yet — tap to write one." },
                style = IosType.subhead,
                color = if (entry.notes.isBlank()) colors.tertiaryLabel else colors.label,
                maxLines = 5
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResolutionTag(entry.resolution == Resolution.Resolved)
                Spacer(Modifier.weight(1f))
                Chevron()
            }
        }
    }
}

// ---------------------------------------------------------------- status

@Composable
fun StatusScreen(
    entries: List<Entry>,
    onOpen: (Long) -> Unit,
    onSetStatus: (Long, Status) -> Unit,
    onAdd: () -> Unit
) {
    val colors = Ios.colors
    var index by rememberSaveable { mutableStateOf(0) }
    val ranks: Map<Long, Int> = remember(entries.toList()) {
        entries.mapIndexed { index0, entry -> entry.id to index0 + 1 }.toMap()
    }
    val pending = entries.filter { it.status == Status.Pending }
    val completed = entries.filter { it.status == Status.Completed }
    val shown = if (index == 0) pending else completed

    LargeTitleScreen(
        title = "Status",
        subtitle = "${pending.size} pending · ${completed.size} completed",
        onAdd = onAdd
    ) {
        item(key = "filter") {
            SegmentedControl(
                options = listOf("Pending ${pending.size}", "Completed ${completed.size}"),
                selectedIndex = index,
                onSelect = { index = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Status,
                    title = "Nothing to track",
                    message = "Entries start as pending and move to completed when you finish them."
                )
            }
        } else if (shown.isEmpty()) {
            item(key = "none") {
                EmptyState(
                    tab = AppTab.Status,
                    title = if (index == 0) "All caught up" else "Nothing completed yet",
                    message = if (index == 0) {
                        "Every entry is marked completed."
                    } else {
                        "Switch an entry to completed to see it here."
                    }
                )
            }
        } else {
            item(key = "status-card") {
                GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                    shown.forEachIndexed { rowIndex, entry ->
                        ListRow(showDivider = rowIndex != shown.lastIndex) {
                            IText(
                                entry.name,
                                modifier = Modifier.weight(1f),
                                style = IosType.headline,
                                color = colors.label,
                                maxLines = 1
                            )
                            IText(
                                "#${ranks[entry.id] ?: 0}",
                                style = IosType.footnote,
                                color = colors.secondaryLabel
                            )
                            Spacer(Modifier.width(10.dp))
                            IosSwitch(
                                checked = entry.status == Status.Completed,
                                onCheckedChange = { checked ->
                                    onSetStatus(entry.id, if (checked) Status.Completed else Status.Pending)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- resolved

@Composable
fun ResolvedScreen(
    entries: List<Entry>,
    onOpen: (Long) -> Unit,
    onSetResolution: (Long, Resolution) -> Unit,
    onAdd: () -> Unit
) {
    val colors = Ios.colors
    val ranks: Map<Long, Int> = remember(entries.toList()) {
        entries.mapIndexed { index, entry -> entry.id to index + 1 }.toMap()
    }
    val resolved = entries.filter { it.resolution == Resolution.Resolved }
    val unresolved = entries.filter { it.resolution == Resolution.NotResolved }

    LargeTitleScreen(
        title = "Resolved",
        subtitle = "${resolved.size} of ${entries.size} issues resolved",
        onAdd = onAdd
    ) {
        item(key = "stats") {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                StatTile("Resolved", resolved.size, colors.green, Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                StatTile("Not Resolved", unresolved.size, colors.orange, Modifier.weight(1f))
            }
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Resolved,
                    title = "No issues to resolve",
                    message = "Mark an entry resolved once its issue is worked through."
                )
            }
        } else {
            if (unresolved.isNotEmpty()) {
                item(key = "hdr-open") { SectionHeader("Not Resolved", Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) }
                item(key = "card-open") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        unresolved.forEachIndexed { index, entry ->
                            ResolutionRow(
                                entry = entry,
                                rank = ranks[entry.id] ?: 0,
                                resolved = false,
                                showDivider = index != unresolved.lastIndex,
                                onOpen = { onOpen(entry.id) },
                                onToggle = { onSetResolution(entry.id, Resolution.Resolved) }
                            )
                        }
                    }
                }
            }
            if (resolved.isNotEmpty()) {
                item(key = "hdr-done") { SectionHeader("Resolved", Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) }
                item(key = "card-done") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        resolved.forEachIndexed { index, entry ->
                            ResolutionRow(
                                entry = entry,
                                rank = ranks[entry.id] ?: 0,
                                resolved = true,
                                showDivider = index != resolved.lastIndex,
                                onOpen = { onOpen(entry.id) },
                                onToggle = { onSetResolution(entry.id, Resolution.NotResolved) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResolutionRow(
    entry: Entry,
    rank: Int,
    resolved: Boolean,
    showDivider: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit
) {
    val colors = Ios.colors
    ListRow(onClick = onOpen, showDivider = showDivider) {
        RankBadge(rank)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            IText(entry.name, style = IosType.headline, color = colors.label, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            IText(
                entry.notes.ifBlank { entry.status.label },
                style = IosType.footnote,
                color = colors.secondaryLabel,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        IosSwitch(checked = resolved, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun StatTile(label: String, value: Int, tint: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    val colors = Ios.colors
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        IText(
            value.toString(),
            style = IosType.largeTitle.copy(fontWeight = FontWeight.Bold),
            color = tint,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        IText(label, style = IosType.footnote, color = colors.secondaryLabel, maxLines = 1)
    }
}
