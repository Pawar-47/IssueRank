package com.pawar47.issuerank.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.pawar47.issuerank.AppState
import com.pawar47.issuerank.domain.Dates
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.Status

// ---------------------------------------------------------------- shared pieces

@Composable
private fun EntryBody(
    entry: Entry,
    now: Long,
    terms: List<String>,
    modifier: Modifier = Modifier
) {
    val colors = Ios.colors
    Column(modifier) {
        HighlightedText(
            text = entry.name,
            terms = terms,
            style = IosType.headline,
            color = colors.label,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        HighlightedText(
            text = entry.issueSummary.ifBlank { "No issue described" },
            terms = terms,
            style = IosType.subhead,
            color = if (entry.issueSummary.isBlank()) colors.tertiaryLabel else colors.secondaryLabel,
            maxLines = 2
        )
        Spacer(Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MetaLine(Dates.relative(entry.updatedAt, now), overdue = entry.isOverdue(now))
            Spacer(Modifier.width(6.dp))
            IText("·", style = IosType.footnote, color = colors.tertiaryLabel, maxLines = 1)
            Spacer(Modifier.width(6.dp))
            IText(
                entry.resolution.label,
                style = IosType.footnote,
                color = if (entry.resolution == Resolution.Resolved) colors.green else colors.orange,
                maxLines = 1
            )
        }
    }
}

/** One record in a grouped list; shared by every tab and by search results. */
@Composable
fun EntryRow(
    entry: Entry,
    now: Long,
    showDivider: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    terms: List<String> = emptyList(),
    trailing: (@Composable () -> Unit)? = null
) {
    ListRow(onClick = onClick, showDivider = showDivider, modifier = modifier) {
        RankBadge(entry.rank)
        Spacer(Modifier.width(12.dp))
        EntryBody(entry, now, terms, Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        if (trailing != null) {
            trailing()
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusTag(entry.status)
                Spacer(Modifier.width(6.dp))
                Chevron()
            }
        }
    }
}

private fun rowShape(index: Int, count: Int): Shape = when {
    count <= 1 -> RoundedCornerShape(16.dp)
    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    index == count - 1 -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    else -> RectangleShape
}

private fun recordCount(count: Int): String =
    "$count ${if (count == 1) "record" else "records"}"

// ---------------------------------------------------------------- rank / home

@Composable
fun RankScreen(
    state: AppState,
    now: Long,
    onOpen: (Entry) -> Unit,
    onMove: (Long, Int) -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit,
    onShare: () -> Unit,
    onOpenTab: (AppTab) -> Unit
) {
    val colors = Ios.colors
    val entries = state.entries
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
        subtitle = "${recordCount(entries.size)} · rank 1 is highest priority",
        onAdd = onAdd,
        onSearch = onSearch,
        leading = leadingSlot
    ) {
        item(key = "dashboard") {
            DashboardOverview(
                state = state,
                now = now,
                onSearch = onSearch,
                onAdd = onAdd,
                onShare = onShare,
                onOpenTab = onOpenTab,
                onOpenEntry = onOpen
            )
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Rank,
                    title = "Nothing ranked yet",
                    message = "Tap + to add the first record. Rank 1 is whatever matters most."
                )
            }
        } else {
            item(key = "records-header") {
                SectionHeader(
                    if (editing) "Use the arrows to reorder" else "All records",
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                )
            }
            item(key = "records") {
                GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                    entries.forEachIndexed { index, entry ->
                        RankRow(
                            entry = entry,
                            editing = editing,
                            now = now,
                            canMoveUp = index > 0,
                            canMoveDown = index < entries.lastIndex,
                            showDivider = index != entries.lastIndex,
                            onOpen = { onOpen(entry) },
                            onMoveUp = { onMove(entry.id, -1) },
                            onMoveDown = { onMove(entry.id, 1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankRow(
    entry: Entry,
    editing: Boolean,
    now: Long,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    showDivider: Boolean,
    onOpen: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val colors = Ios.colors
    ListRow(onClick = if (editing) null else onOpen, showDivider = showDivider) {
        RankBadge(entry.rank)
        Spacer(Modifier.width(12.dp))
        EntryBody(entry, now, emptyList(), Modifier.weight(1f))
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
            StatusTag(entry.status)
        }
    }
}

// ---------------------------------------------------------------- name

@Composable
fun NameScreen(
    entries: List<Entry>,
    now: Long,
    onOpen: (Entry) -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit
) {
    val colors = Ios.colors
    val groups: List<Pair<String, List<Entry>>> = remember(entries) {
        entries
            .groupBy { entry ->
                val initial = entry.name.trim().firstOrNull()?.uppercaseChar()
                if (initial != null && initial in 'A'..'Z') initial.toString() else "#"
            }
            .entries
            .sortedBy { it.key }
            .map { it.key to it.value }
    }

    LargeTitleScreen(
        title = "Name",
        subtitle = "Alphabetical index of ${recordCount(entries.size)}",
        onAdd = onAdd,
        onSearch = onSearch
    ) {
        item(key = "search") {
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 6.dp)) {
                SearchFieldButton(onClick = onSearch, hint = "Search by name")
            }
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Name,
                    title = "No names yet",
                    message = "Records you add appear here, grouped A–Z."
                )
            }
        } else {
            groups.forEach { (letter, groupEntries) ->
                item(key = "header-$letter") {
                    SectionHeader(letter, Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp))
                }
                item(key = "card-$letter") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        groupEntries.forEachIndexed { index, entry ->
                            ListRow(
                                onClick = { onOpen(entry) },
                                showDivider = index != groupEntries.lastIndex
                            ) {
                                InitialAvatar(entry.name, entry.id)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    IText(
                                        entry.name,
                                        style = IosType.headline,
                                        color = colors.label,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    IText(
                                        "Rank #${entry.rank} · ${entry.status.label}",
                                        style = IosType.footnote,
                                        color = colors.secondaryLabel,
                                        maxLines = 1
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    ResolutionTag(entry.resolution == Resolution.Resolved)
                                    Spacer(Modifier.height(3.dp))
                                    MetaLine(
                                        Dates.relative(entry.updatedAt, now),
                                        overdue = entry.isOverdue(now)
                                    )
                                }
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
    now: Long,
    onOpen: (Entry) -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf(0) }
    val open = entries.count { it.resolution == Resolution.NotResolved }
    val shown = when (filter) {
        1 -> entries.filter { it.resolution == Resolution.NotResolved }
        2 -> entries.filter { it.resolution == Resolution.Resolved }
        else -> entries
    }

    LargeTitleScreen(
        title = "Issues",
        subtitle = "$open open · ${entries.size - open} resolved",
        onAdd = onAdd,
        onSearch = onSearch
    ) {
        item(key = "search") {
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 6.dp)) {
                SearchFieldButton(
                    onClick = onSearch,
                    hint = "Search issue titles, descriptions and notes"
                )
            }
        }
        item(key = "filter") {
            SegmentedControl(
                options = listOf("All", "Open", "Resolved"),
                selectedIndex = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
            )
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Issues,
                    title = "No issues recorded",
                    message = "Every record keeps an issue title, a description and short notes."
                )
            }
        } else if (shown.isEmpty()) {
            item(key = "none") {
                EmptyState(
                    tab = AppTab.Issues,
                    title = "Nothing here",
                    message = "This filter has no records right now."
                )
            }
        } else {
            shown.forEach { entry ->
                item(key = "issue-${entry.id}") {
                    IssueCard(entry = entry, now = now, onOpen = { onOpen(entry) })
                }
            }
        }
    }
}

@Composable
private fun IssueCard(entry: Entry, now: Long, onOpen: () -> Unit) {
    val colors = Ios.colors
    Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 5.dp)) {
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
                    entry.issueTitle.ifBlank { entry.name },
                    modifier = Modifier.weight(1f),
                    style = IosType.headline,
                    color = colors.label,
                    maxLines = 1
                )
                StatusTag(entry.status)
            }
            if (entry.issueDescription.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                IText(
                    entry.issueDescription,
                    style = IosType.subhead,
                    color = colors.label,
                    maxLines = 5
                )
            }
            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                IText(
                    entry.notes,
                    style = IosType.footnote,
                    color = colors.secondaryLabel,
                    maxLines = 3
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IText(
                    entry.name,
                    style = IosType.footnote,
                    color = colors.secondaryLabel,
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                ResolutionTag(entry.resolution == Resolution.Resolved)
                Spacer(Modifier.width(8.dp))
                MetaLine(Dates.relative(entry.updatedAt, now), overdue = entry.isOverdue(now))
            }
        }
    }
}

// ---------------------------------------------------------------- status

@Composable
fun StatusScreen(
    entries: List<Entry>,
    now: Long,
    onOpen: (Entry) -> Unit,
    onSetStatus: (Entry, Status) -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit
) {
    val colors = Ios.colors
    var index by rememberSaveable { mutableStateOf(0) }
    val pending = entries.filter { it.status == Status.Pending }
    val completed = entries.filter { it.status == Status.Completed }
    val shown = if (index == 0) pending else completed

    LargeTitleScreen(
        title = "Status",
        subtitle = "${pending.size} pending · ${completed.size} completed",
        onAdd = onAdd,
        onSearch = onSearch
    ) {
        item(key = "filter") {
            SegmentedControl(
                options = listOf("Pending ${pending.size}", "Completed ${completed.size}"),
                selectedIndex = index,
                onSelect = { index = it },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 6.dp)
            )
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Status,
                    title = "Nothing to track",
                    message = "Records start as pending and move to completed when you finish them."
                )
            }
        } else if (shown.isEmpty()) {
            item(key = "none") {
                EmptyState(
                    tab = AppTab.Status,
                    title = if (index == 0) "All caught up" else "Nothing completed yet",
                    message = if (index == 0) {
                        "Every record is marked completed."
                    } else {
                        "Complete a record and it moves here."
                    }
                )
            }
        } else {
            item(key = "status-card") {
                GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                    shown.forEachIndexed { rowIndex, entry ->
                        ListRow(showDivider = rowIndex != shown.lastIndex) {
                            Column(Modifier.weight(1f)) {
                                IText(
                                    entry.name,
                                    style = IosType.headline,
                                    color = colors.label,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(2.dp))
                                MetaLine(
                                    "Rank #${entry.rank} · updated ${Dates.relative(entry.updatedAt, now)}",
                                    overdue = entry.isOverdue(now)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            IosSwitch(
                                checked = entry.status == Status.Completed,
                                onCheckedChange = { checked ->
                                    onSetStatus(
                                        entry,
                                        if (checked) Status.Completed else Status.Pending
                                    )
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
    now: Long,
    onOpen: (Entry) -> Unit,
    onToggleResolution: (Entry) -> Unit,
    onDelete: (Entry) -> Unit,
    onAdd: () -> Unit,
    onSearch: () -> Unit
) {
    val colors = Ios.colors
    val resolved = entries.filter { it.resolution == Resolution.Resolved }
    val unresolved = entries.filter { it.resolution == Resolution.NotResolved }

    LargeTitleScreen(
        title = "Resolved",
        subtitle = "${resolved.size} of ${entries.size} issues resolved",
        onAdd = onAdd,
        onSearch = onSearch
    ) {
        item(key = "stats") {
            Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp)) {
                StatTile(
                    label = "Resolved",
                    value = resolved.size,
                    tint = colors.green,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                StatTile(
                    label = "Not Resolved",
                    value = unresolved.size,
                    tint = colors.orange,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (entries.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    tab = AppTab.Resolved,
                    title = "No issues to resolve",
                    message = "Mark a record resolved once its issue is actually worked through."
                )
            }
        } else {
            if (unresolved.isNotEmpty()) {
                item(key = "hdr-open") {
                    SectionHeader(
                        "Not resolved",
                        Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    )
                }
                item(key = "card-open") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        unresolved.forEachIndexed { index, entry ->
                            SwipeRow(
                                actionLabel = "Resolve",
                                actionTint = colors.green,
                                onAction = { onToggleResolution(entry) },
                                onDelete = { onDelete(entry) },
                                shape = rowShape(index, unresolved.size)
                            ) {
                                ListRow(
                                    onClick = { onOpen(entry) },
                                    showDivider = index != unresolved.lastIndex
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        IText(
                                            entry.name,
                                            style = IosType.headline,
                                            color = colors.label,
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        IText(
                                            entry.issueSummary.ifBlank { "No issue described" },
                                            style = IosType.footnote,
                                            color = colors.secondaryLabel,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    IosSwitch(
                                        checked = false,
                                        onCheckedChange = { onToggleResolution(entry) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (resolved.isNotEmpty()) {
                item(key = "hdr-done") {
                    SectionHeader(
                        "Resolved",
                        Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    )
                }
                item(key = "card-done") {
                    GroupedCard(Modifier.padding(horizontal = 16.dp)) {
                        resolved.forEachIndexed { index, entry ->
                            SwipeRow(
                                actionLabel = "Reopen",
                                actionTint = colors.orange,
                                onAction = { onToggleResolution(entry) },
                                onDelete = { onDelete(entry) },
                                shape = rowShape(index, resolved.size)
                            ) {
                                ListRow(
                                    onClick = { onOpen(entry) },
                                    showDivider = index != resolved.lastIndex
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        IText(
                                            entry.name,
                                            style = IosType.headline,
                                            color = colors.label,
                                            maxLines = 1
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        MetaLine(
                                            "Resolved ${Dates.relative(entry.resolvedAt ?: entry.updatedAt, now)}",
                                            overdue = false
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    IosSwitch(
                                        checked = true,
                                        onCheckedChange = { onToggleResolution(entry) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
