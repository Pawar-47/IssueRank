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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.pawar47.issuerank.data.Entry
import com.pawar47.issuerank.data.Resolution
import com.pawar47.issuerank.data.Status

/**
 * iOS-style sheet used for both creating and editing an entry.
 * Presented inside the app's own hierarchy so it can slide over the tab bar.
 */
@Composable
fun EntryEditor(
    entry: Entry?,
    onDismiss: () -> Unit,
    onSave: (name: String, notes: String, status: Status, resolution: Resolution) -> Unit,
    onDelete: () -> Unit
) {
    val colors = Ios.colors
    var name by remember { mutableStateOf(entry?.name ?: "") }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }
    var status by remember { mutableStateOf(entry?.status ?: Status.Pending) }
    var resolution by remember { mutableStateOf(entry?.resolution ?: Resolution.NotResolved) }
    var confirmingDelete by remember { mutableStateOf(false) }

    BackHandler { onDismiss() }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 14.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(colors.sheetBackground)
            .imePadding()
            // Swallow taps on the sheet body so only the dimmed area behind it dismisses.
            .iosClick { }
    ) {
        // grabber
        Box(Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(colors.tertiaryLabel)
            )
        }

        // nav bar
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
                    .iosClick { onDismiss() }
                    .padding(vertical = 8.dp),
                style = IosType.body,
                color = colors.blue
            )
            IText(
                if (entry == null) "New Entry" else "Edit Entry",
                modifier = Modifier.align(Alignment.Center),
                style = IosType.headline,
                color = colors.label
            )
            Box(Modifier.align(Alignment.CenterEnd)) {
                FilledCapsuleButton(
                    text = "Save",
                    enabled = name.isNotBlank(),
                    onClick = {
                        onSave(
                            name.trim(),
                            notes.trim(),
                            status,
                            resolution
                        )
                    }
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
                ListRow(
                    showDivider = false,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    IosTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = "Describe the issue, plus any short notes…",
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        minHeight = 92.dp,
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
                            if (resolution == Resolution.Resolved) "Issue resolved" else "Issue not resolved",
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

            ListRow(
                onClick = {
                    if (confirmingDelete) {
                        onDelete()
                    } else {
                        confirmingDelete = true
                    }
                },
                showDivider = false,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.card)
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    IText(
                        if (confirmingDelete) "Confirm Delete" else "Delete Entry",
                        style = IosType.body.copy(fontWeight = FontWeight.Medium),
                        color = colors.red,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            IText(
                "Ranks follow list order. Move an entry on the Rank tab to change its position.",
                modifier = Modifier.fillMaxWidth(),
                style = IosType.footnote,
                color = colors.secondaryLabel,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}
