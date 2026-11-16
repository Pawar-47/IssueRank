package com.pawar47.issuerank.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pawar47.issuerank.domain.Dates
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.EntryDraft
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.Status

private enum class DateField { Reported, Due, Resolved }

/**
 * Create / edit sheet. Every field of a record is here, the sheet resists accidental loss
 * (Cancel asks before discarding changes), and optional dates use the system picker.
 */
@Composable
fun EntryEditor(
    entry: Entry?,
    onDismiss: () -> Unit,
    onSave: (EntryDraft) -> Unit,
    onDelete: () -> Unit
) {
    val colors = Ios.colors

    var name by remember { mutableStateOf(entry?.name ?: "") }
    var issueTitle by remember { mutableStateOf(entry?.issueTitle ?: "") }
    var description by remember { mutableStateOf(entry?.issueDescription ?: "") }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }
    var status by remember { mutableStateOf(entry?.status ?: Status.Pending) }
    var resolution by remember { mutableStateOf(entry?.resolution ?: Resolution.NotResolved) }
    var reportedAt by remember { mutableStateOf(entry?.reportedAt) }
    var dueAt by remember { mutableStateOf(entry?.dueAt) }
    var resolvedAt by remember { mutableStateOf(entry?.resolvedAt) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var confirmingDiscard by remember { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf<DateField?>(null) }

    val initial = remember(entry) { EntryDraft.from(entry ?: Entry()) }
    val draft = EntryDraft(
        name = name,
        issueTitle = issueTitle,
        issueDescription = description,
        notes = notes,
        status = status,
        resolution = resolution,
        reportedAt = reportedAt,
        dueAt = dueAt,
        resolvedAt = resolvedAt
    )
    val isDirty = draft != initial

    val requestClose: () -> Unit = {
        if (isDirty) confirmingDiscard = true else onDismiss()
    }

    BackHandler { requestClose() }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(colors.sheetBackground)
            .imePadding()
            // Swallow taps so only the dimmed area behind the sheet dismisses it.
            .iosClick { }
    ) {
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(colors.tertiaryLabel)
            )
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp)
        ) {
            IText(
                "Cancel",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .iosClick { requestClose() }
                    .padding(vertical = 8.dp),
                style = IosType.body,
                color = colors.blue
            )
            IText(
                if (entry == null) "New Record" else "Edit Record",
                modifier = Modifier.align(Alignment.Center),
                style = IosType.headline,
                color = colors.label
            )
            Box(Modifier.align(Alignment.CenterEnd)) {
                FilledCapsuleButton(
                    text = "Save",
                    enabled = draft.isValid,
                    onClick = { onSave(draft) }
                )
            }
        }

        if (confirmingDiscard) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.red.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IText(
                    "Discard changes?",
                    modifier = Modifier.weight(1f),
                    style = IosType.footnote,
                    color = colors.label,
                    maxLines = 1
                )
                IText(
                    "Keep editing",
                    modifier = Modifier.iosClick { confirmingDiscard = false },
                    style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.blue,
                    maxLines = 1
                )
                Spacer(Modifier.width(12.dp))
                IText(
                    "Discard",
                    modifier = Modifier.iosClick { onDismiss() },
                    style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.red,
                    maxLines = 1
                )
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            GroupedCard(header = "Name") {
                ListRow(
                    showDivider = false,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    IosTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Person, ticket or issue owner",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            GroupedCard(header = "Issue") {
                ListRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
                    IosTextField(
                        value = issueTitle,
                        onValueChange = { issueTitle = it },
                        placeholder = "Short title",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                }
                ListRow(
                    showDivider = false,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    IosTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Describe the issue in full…",
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        minHeight = 108.dp,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            GroupedCard(header = "Notes") {
                ListRow(
                    showDivider = false,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    IosTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Short notes, follow-ups, observations…",
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        minHeight = 68.dp,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            GroupedCard(header = "State") {
                ListRow {
                    IText(
                        "Status",
                        modifier = Modifier.weight(1f),
                        style = IosType.body,
                        color = colors.label,
                        maxLines = 1
                    )
                    SegmentedControl(
                        options = listOf("Pending", "Completed"),
                        selectedIndex = if (status == Status.Pending) 0 else 1,
                        onSelect = { index ->
                            status = if (index == 0) Status.Pending else Status.Completed
                        },
                        modifier = Modifier.width(172.dp)
                    )
                }
                ListRow(showDivider = false) {
                    Column(Modifier.weight(1f)) {
                        IText("Resolution", style = IosType.body, color = colors.label, maxLines = 1)
                        Spacer(Modifier.height(2.dp))
                        IText(
                            if (resolution == Resolution.Resolved) {
                                "Issue resolved"
                            } else {
                                "Issue not resolved"
                            },
                            style = IosType.footnote,
                            color = colors.secondaryLabel,
                            maxLines = 1
                        )
                    }
                    IosSwitch(
                        checked = resolution == Resolution.Resolved,
                        onCheckedChange = { checked ->
                            resolution = if (checked) Resolution.Resolved else Resolution.NotResolved
                        }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            GroupedCard(header = "Dates") {
                DateRow(
                    label = "Reported",
                    value = reportedAt,
                    showDivider = true,
                    onClick = { pickingDate = DateField.Reported }
                )
                DateRow(
                    label = "Expected",
                    value = dueAt,
                    showDivider = true,
                    onClick = { pickingDate = DateField.Due }
                )
                DateRow(
                    label = "Resolved on",
                    value = resolvedAt,
                    showDivider = false,
                    onClick = { pickingDate = DateField.Resolved }
                )
            }

            if (entry != null) {
                Spacer(Modifier.height(10.dp))
                IText(
                    "Created ${Dates.full(entry.createdAt)} · Updated ${Dates.full(entry.updatedAt)}",
                    modifier = Modifier.fillMaxWidth(),
                    style = IosType.footnote,
                    color = colors.secondaryLabel,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            ListRow(
                onClick = {
                    if (confirmingDelete) onDelete() else confirmingDelete = true
                },
                showDivider = false,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.card)
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    IText(
                        if (confirmingDelete) "Confirm delete" else "Delete record",
                        style = IosType.body.copy(fontWeight = FontWeight.Medium),
                        color = colors.red,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            IText(
                "Ranks follow list order. Move a record on the Rank tab to change its position.",
                modifier = Modifier.fillMaxWidth(),
                style = IosType.footnote,
                color = colors.secondaryLabel,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))
        }
    }

    val field = pickingDate
    if (field != null) {
        key(field) {
            DatePickerSheet(
                initialMillis = when (field) {
                    DateField.Reported -> reportedAt
                    DateField.Due -> dueAt
                    DateField.Resolved -> resolvedAt
                },
                onPick = { millis ->
                    when (field) {
                        DateField.Reported -> reportedAt = millis
                        DateField.Due -> dueAt = millis
                        DateField.Resolved -> resolvedAt = millis
                    }
                    pickingDate = null
                },
                onDismiss = { pickingDate = null }
            )
        }
    }
}

@Composable
private fun DateRow(
    label: String,
    value: Long?,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    val colors = Ios.colors
    ListRow(onClick = onClick, showDivider = showDivider) {
        IText(
            label,
            modifier = Modifier.weight(1f),
            style = IosType.body,
            color = colors.label,
            maxLines = 1
        )
        IText(
            if (value == null) "None" else Dates.day(value),
            style = IosType.body,
            color = if (value == null) colors.tertiaryLabel else colors.blue,
            maxLines = 1
        )
        Spacer(Modifier.width(4.dp))
        Chevron()
    }
}

/**
 * The system date picker, used as infrastructure. Clear removes the date instead of picking
 * today, so the optional dates stay genuinely optional.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initialMillis: Long?,
    onPick: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = Ios.colors
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis?.let { Dates.toPickerMillis(it) }
    )

    MaterialTheme(
        colorScheme = if (colors.isDark) {
            darkColorScheme(primary = colors.blue)
        } else {
            lightColorScheme(primary = colors.blue)
        }
    ) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onPick(null) }) {
                        Text("Clear", color = colors.red)
                    }
                    TextButton(
                        onClick = {
                            val picked = state.selectedDateMillis
                            onPick(picked?.let { Dates.fromPickerMillis(it) })
                        }
                    ) {
                        Text("Done")
                    }
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
