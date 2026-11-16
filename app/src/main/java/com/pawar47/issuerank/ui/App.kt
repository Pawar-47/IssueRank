package com.pawar47.issuerank.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawar47.issuerank.EntriesViewModel
import com.pawar47.issuerank.data.Entry

enum class AppTab(val label: String) {
    Rank("Rank"),
    Name("Name"),
    Issues("Issues"),
    Status("Status"),
    Resolved("Resolved")
}

@Composable
fun IssueRankApp(viewModel: EntriesViewModel = viewModel()) {
    val entries: List<Entry> = viewModel.entries
    val colors = Ios.colors

    var tabName by rememberSaveable { mutableStateOf(AppTab.Rank.name) }
    val tab = AppTab.valueOf(tabName)

    var editorOpen by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf<Entry?>(null) }

    val openEntry: (Long) -> Unit = { id ->
        edited = entries.firstOrNull { it.id == id }
        editorOpen = true
    }
    val addEntry: () -> Unit = {
        edited = null
        editorOpen = true
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        when (tab) {
            AppTab.Rank -> RankScreen(
                entries = entries,
                onOpen = openEntry,
                onMove = { id, delta -> viewModel.move(id, delta) },
                onAdd = addEntry
            )

            AppTab.Name -> NameScreen(
                entries = entries,
                onOpen = openEntry,
                onAdd = addEntry
            )

            AppTab.Issues -> IssuesScreen(
                entries = entries,
                onOpen = openEntry,
                onAdd = addEntry
            )

            AppTab.Status -> StatusScreen(
                entries = entries,
                onOpen = openEntry,
                onSetStatus = { id, status -> viewModel.setStatus(id, status) },
                onAdd = addEntry
            )

            AppTab.Resolved -> ResolvedScreen(
                entries = entries,
                onOpen = openEntry,
                onSetResolution = { id, resolution -> viewModel.setResolution(id, resolution) },
                onAdd = addEntry
            )
        }

        FloatingTabBar(
            selected = tab,
            onSelect = { tabName = it.name },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        AnimatedVisibility(
            visible = editorOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(240)
            ) + fadeOut(animationSpec = tween(180))
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                        .iosClick { editorOpen = false }
                )
                EntryEditor(
                    entry = edited,
                    onDismiss = { editorOpen = false },
                    onSave = { name, notes, status, resolution ->
                        val existing = edited
                        if (existing == null) {
                            viewModel.add(name, notes, status, resolution)
                        } else {
                            viewModel.update(Entry(existing.id, name, notes, status, resolution))
                        }
                        editorOpen = false
                    },
                    onDelete = {
                        edited?.let { viewModel.delete(it.id) }
                        editorOpen = false
                    }
                )
            }
        }
    }
}
