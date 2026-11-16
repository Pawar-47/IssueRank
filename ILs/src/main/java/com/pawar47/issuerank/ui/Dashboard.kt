package com.pawar47.issuerank.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pawar47.issuerank.AppState
import com.pawar47.issuerank.domain.Dates
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.ListStats
import com.pawar47.issuerank.domain.matchRanges

// ---------------------------------------------------------------- primitives

/** The dashboard's search entry point: looks like a field, behaves like a button. */
@Composable
fun SearchFieldButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "Search names, issues, notes"
) {
    val colors = Ios.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(colors.fill)
            .iosClick(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchGlyph(colors.secondaryLabel, size = 15.dp)
        Spacer(Modifier.width(6.dp))
        IText(hint, style = IosType.subhead, color = colors.secondaryLabel, maxLines = 1)
    }
}

/** "5 min ago" with a clock, or a red alert when the record is overdue. */
@Composable
fun MetaLine(text: String, overdue: Boolean = false, modifier: Modifier = Modifier) {
    val colors = Ios.colors
    val tint = if (overdue) colors.red else colors.tertiaryLabel
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (overdue) AlertGlyph(tint, size = 11.dp) else ClockGlyph(tint, size = 11.dp)
        Spacer(Modifier.width(4.dp))
        IText(
            text,
            style = IosType.footnote,
            color = if (overdue) colors.red else colors.secondaryLabel,
            maxLines = 1
        )
    }
}

@Composable
fun ProgressRing(
    percent: Int,
    tint: Color,
    modifier: Modifier = Modifier,
    diameter: Dp = 84.dp,
    strokeWidth: Dp = 9.dp,
    content: @Composable () -> Unit
) {
    val colors = Ios.colors
    Box(modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = colors.fill,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            val sweep = 360f * percent.coerceIn(0, 100) / 100f
            if (sweep > 0f) {
                drawArc(
                    color = tint,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

@Composable
fun ProgressBarRow(
    label: String,
    percent: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val colors = Ios.colors
    val clamped = percent.coerceIn(0, 100)
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IText(
                label,
                modifier = Modifier.weight(1f),
                style = IosType.footnote,
                color = colors.secondaryLabel,
                maxLines = 1
            )
            IText(
                "$clamped%",
                style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
                color = colors.label,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.fill)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(clamped / 100f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(3.dp))
                    .background(tint)
            )
        }
    }
}

@Composable
fun StatTile(
    label: String,
    value: Int,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = Ios.colors
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.card)
            .iosClick(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.width(6.dp))
            IText(label, style = IosType.footnote, color = colors.secondaryLabel, maxLines = 1)
        }
        Spacer(Modifier.height(4.dp))
        IText(
            value.toString(),
            style = IosType.title.copy(fontWeight = FontWeight.Bold),
            color = colors.label,
            maxLines = 1
        )
    }
}

@Composable
fun QuickAction(
    text: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: @Composable () -> Unit
) {
    Row(
        modifier
            .height(34.dp)
            .glass(RoundedCornerShape(17.dp), Ios.colors)
            .iosClick(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        glyph()
        Spacer(Modifier.width(6.dp))
        IText(text, style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold), color = tint, maxLines = 1)
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Ios.colors
    Box(
        Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (selected) colors.blue.copy(alpha = 0.16f) else colors.fill)
            .iosClick(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        IText(
            label,
            style = IosType.footnote.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) colors.blue else colors.secondaryLabel,
            maxLines = 1
        )
    }
}

/** Renders text with every search term highlighted, used by the results list. */
@Composable
fun HighlightedText(
    text: String,
    terms: List<String>,
    style: TextStyle = IosType.subhead,
    color: Color = Ios.colors.label,
    maxLines: Int = 2
) {
    val colors = Ios.colors
    val annotated = remember(text, terms) {
        buildAnnotatedString {
            append(text)
            matchRanges(text, terms).forEach { range ->
                val start = range.first.coerceIn(0, text.length)
                val end = (range.last + 1).coerceIn(start, text.length)
                if (start < end) {
                    addStyle(
                        SpanStyle(
                            background = colors.blue.copy(alpha = 0.18f),
                            fontWeight = FontWeight.SemiBold
                        ),
                        start,
                        end
                    )
                }
            }
        }
    }
    Text(annotated, style = style, color = color, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
}

// ---------------------------------------------------------------- dashboard blocks

@Composable
private fun CountLine(label: String, value: Int, tint: Color, onOpenTab: (AppTab) -> Unit, tab: AppTab) {
    val colors = Ios.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .iosClick { onOpenTab(tab) }
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(tint))
        Spacer(Modifier.width(8.dp))
        IText(
            label,
            modifier = Modifier.weight(1f),
            style = IosType.footnote,
            color = colors.secondaryLabel,
            maxLines = 1
        )
        IText(
            value.toString(),
            style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
            color = colors.label,
            maxLines = 1
        )
    }
}

@Composable
private fun OverviewCard(stats: ListStats, onOpenTab: (AppTab) -> Unit) {
    val colors = Ios.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProgressRing(percent = stats.resolutionPercent, tint = colors.green) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IText(
                    "${stats.resolutionPercent}%",
                    style = IosType.title.copy(fontWeight = FontWeight.Bold),
                    color = colors.label,
                    maxLines = 1
                )
                IText("resolved", style = IosType.caption, color = colors.secondaryLabel, maxLines = 1)
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            CountLine("Total", stats.total, colors.blue, onOpenTab, AppTab.Rank)
            CountLine("Pending", stats.pending, colors.orange, onOpenTab, AppTab.Status)
            CountLine("Completed", stats.completed, colors.green, onOpenTab, AppTab.Status)
            CountLine("Overdue", stats.overdue, colors.red, onOpenTab, AppTab.Rank)
        }
    }
}

@Composable
private fun StatisticGrid(stats: ListStats, onOpenTab: (AppTab) -> Unit) {
    val colors = Ios.colors
    Column {
        Row(Modifier.fillMaxWidth()) {
            StatTile(
                label = "Records",
                value = stats.total,
                tint = colors.blue,
                onClick = { onOpenTab(AppTab.Rank) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            StatTile(
                label = "Open",
                value = stats.notResolved,
                tint = colors.orange,
                onClick = { onOpenTab(AppTab.Resolved) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth()) {
            StatTile(
                label = "Pending",
                value = stats.pending,
                tint = colors.orange,
                onClick = { onOpenTab(AppTab.Status) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            StatTile(
                label = "Resolved",
                value = stats.resolved,
                tint = colors.green,
                onClick = { onOpenTab(AppTab.Resolved) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProgressCard(stats: ListStats) {
    val colors = Ios.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .padding(14.dp)
    ) {
        ProgressBarRow("Completion", stats.completionPercent, colors.blue)
        Spacer(Modifier.height(12.dp))
        ProgressBarRow("Resolution", stats.resolutionPercent, colors.green)
    }
}

@Composable
private fun WelcomeCard(onAdd: () -> Unit, onSearch: () -> Unit) {
    val colors = Ios.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.card)
            .padding(18.dp)
    ) {
        IText("Start your list", style = IosType.title, color = colors.label)
        Spacer(Modifier.height(6.dp))
        IText(
            "Add a record, rank it, and track whether its issue is still open. " +
                "Everything stays on this device.",
            style = IosType.subhead,
            color = colors.secondaryLabel
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledCapsuleButton("Add record", onClick = onAdd)
            Spacer(Modifier.width(10.dp))
            QuickAction(text = "Search", tint = colors.secondaryLabel, onClick = onSearch) {
                SearchGlyph(colors.secondaryLabel, size = 14.dp)
            }
        }
    }
}

@Composable
private fun DashboardHeader(text: String) {
    val colors = Ios.colors
    IText(
        text.uppercase(),
        modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
        style = IosType.footnote.copy(fontWeight = FontWeight.Medium),
        color = colors.secondaryLabel
    )
}

@Composable
fun DashboardEntryRow(
    entry: Entry,
    now: Long,
    showDivider: Boolean,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = Ios.colors
    ListRow(
        onClick = onClick,
        showDivider = showDivider,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            IText(
                entry.name,
                style = IosType.subhead.copy(fontWeight = FontWeight.SemiBold),
                color = colors.label,
                maxLines = 1
            )
            Spacer(Modifier.height(1.dp))
            IText(
                entry.issueSummary.ifBlank { "No issue described" },
                style = IosType.footnote,
                color = colors.secondaryLabel,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        if (trailing != null) {
            trailing()
        } else {
            MetaLine(Dates.relative(entry.updatedAt, now), overdue = entry.isOverdue(now))
        }
    }
}

@Composable
private fun DashboardBlock(
    title: String,
    entries: List<Entry>,
    now: Long,
    onOpenEntry: (Entry) -> Unit
) {
    Column {
        DashboardHeader(title)
        GroupedCard {
            entries.forEachIndexed { index, entry ->
                DashboardEntryRow(
                    entry = entry,
                    now = now,
                    showDivider = index != entries.lastIndex,
                    onClick = { onOpenEntry(entry) }
                )
            }
        }
    }
}

/**
 * The overview above the ranked list: search, health at a glance, and the three short
 * lists that answer "what next", "what changed" and "what is slipping".
 */
@Composable
fun DashboardOverview(
    state: AppState,
    now: Long,
    onSearch: () -> Unit,
    onAdd: () -> Unit,
    onShare: () -> Unit,
    onOpenTab: (AppTab) -> Unit,
    onOpenEntry: (Entry) -> Unit
) {
    val colors = Ios.colors
    val dashboard = state.dashboard
    val stats = dashboard.stats

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        SearchFieldButton(onClick = onSearch)
        Spacer(Modifier.height(14.dp))

        if (stats.isEmpty) {
            WelcomeCard(onAdd = onAdd, onSearch = onSearch)
            Spacer(Modifier.height(18.dp))
        } else {
            OverviewCard(stats = stats, onOpenTab = onOpenTab)
            Spacer(Modifier.height(12.dp))
            StatisticGrid(stats = stats, onOpenTab = onOpenTab)
            Spacer(Modifier.height(12.dp))
            ProgressCard(stats = stats)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction(text = "New", tint = colors.blue, onClick = onAdd) {
                    PlusGlyph(colors.blue, size = 13.dp)
                }
                QuickAction(text = "Search", tint = colors.secondaryLabel, onClick = onSearch) {
                    SearchGlyph(colors.secondaryLabel, size = 13.dp)
                }
                QuickAction(text = "Share", tint = colors.secondaryLabel, onClick = onShare) {
                    ShareGlyph(colors.secondaryLabel, size = 13.dp)
                }
            }
            Spacer(Modifier.height(18.dp))

            if (dashboard.priority.isNotEmpty()) {
                DashboardBlock("Top priority", dashboard.priority, now, onOpenEntry)
                Spacer(Modifier.height(16.dp))
            }
            if (dashboard.needsAttention.isNotEmpty()) {
                DashboardBlock("Needs attention", dashboard.needsAttention, now, onOpenEntry)
                Spacer(Modifier.height(16.dp))
            }
            if (dashboard.recentlyUpdated.isNotEmpty()) {
                DashboardBlock("Recently updated", dashboard.recentlyUpdated, now, onOpenEntry)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
