package com.pawar47.issuerank.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawar47.issuerank.EntriesViewModel
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.SearchScope
import com.pawar47.issuerank.domain.summaryText
import kotlinx.coroutines.delay

enum class AppTab(val label: String) {
    Rank("Rank"),
    Name("Name"),
    Issues("Issues"),
    Status("Status"),
    Resolved("Resolved")
}

@Composable
fun IssueRankApp(viewModel: EntriesViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lastDeleted by viewModel.lastDeleted.collectAsStateWithLifecycle()
    val colors = Ios.colors
    val context = LocalContext.current

    var tabName by rememberSaveable { mutableStateOf(AppTab.Rank.name) }
    val tab = AppTab.valueOf(tabName)

    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var editorOpen by remember { mutableStateOf(false) }
    var edited by remember { mutableStateOf<Entry?>(null) }

    // Relative timestamps refresh whenever the data does.
    val now = remember(state) { System.currentTimeMillis() }

    val openEntry: (Entry) -> Unit = { entry ->
        edited = entry
        editorOpen = true
    }
    val addEntry: () -> Unit = {
        edited = null
        editorOpen = true
    }
    val openSearch: (AppTab) -> Unit = { source ->
        viewModel.setSearchScope(
            when (source) {
                AppTab.Name -> SearchScope.Name
                AppTab.Issues -> SearchScope.Issues
                else -> SearchScope.All
            }
        )
        searchOpen = true
    }
    val share: () -> Unit = {
        val text = summaryText(
            entries = state.entries,
            stats = state.dashboard.stats,
            now = System.currentTimeMillis()
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share summary"))
    }

    LaunchedEffect(lastDeleted) {
        if (lastDeleted != null) {
            delay(4_000)
            viewModel.dismissUndo()
        }
    }

    Box(Modifier.fillMaxSize().background(colors.background)) {
        when (tab) {
            AppTab.Rank -> RankScreen(
                state = state,
                now = now,
                onOpen = openEntry,
                onMove = { id, delta -> viewModel.move(id, delta) },
                onAdd = addEntry,
                onSearch = { openSearch(AppTab.Rank) },
                onShare = share,
                onOpenTab = { target -> tabName = target.name }
            )

            AppTab.Name -> NameScreen(
                entries = state.entries,
                now = now,
                onOpen = openEntry,
                onAdd = addEntry,
                onSearch = { openSearch(AppTab.Name) }
            )

            AppTab.Issues -> IssuesScreen(
                entries = state.entries,
                now = now,
                onOpen = openEntry,
                onAdd = addEntry,
                onSearch = { openSearch(AppTab.Issues) }
            )

            AppTab.Status -> StatusScreen(
                entries = state.entries,
                now = now,
                onOpen = openEntry,
                onSetStatus = { entry, status -> viewModel.setStatus(entry, status) },
                onAdd = addEntry,
                onSearch = { openSearch(AppTab.Status) }
            )

            AppTab.Resolved -> ResolvedScreen(
                entries = state.entries,
                now = now,
                onOpen = openEntry,
                onToggleResolution = { entry -> viewModel.toggleResolution(entry) },
                onDelete = { entry -> viewModel.delete(entry) },
                onAdd = addEntry,
                onSearch = { openSearch(AppTab.Resolved) }
            )
        }

        FloatingTabBar(
            selected = tab,
            onSelect = { tabName = it.name },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        AnimatedVisibility(
            visible = lastDeleted != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp),
            enter = fadeIn(tween(160)) + slideInVertically(tween(220)) { it },
            exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { it }
        ) {
            UndoBanner(
                name = lastDeleted?.name ?: "",
                onUndo = { viewModel.undoDelete() },
                onDismiss = { viewModel.dismissUndo() }
            )
        }

        AnimatedVisibility(
            visible = searchOpen,
            enter = fadeIn(tween(140)),
            exit = fadeOut(tween(120))
        ) {
            SearchOverlay(
                state = state.search,
                recents = state.recents,
                now = now,
                onQueryChange = { viewModel.setSearchText(it) },
                onScopeChange = { viewModel.setSearchScope(it) },
                onFilterChange = { viewModel.setSearchFilter(it) },
                onSortChange = { viewModel.setSearchSort(it) },
                onSubmit = { viewModel.submitSearch() },
                onRecent = { viewModel.applyRecentSearch(it) },
                onRemoveRecent = { viewModel.removeRecent(it) },
                onClearRecents = { viewModel.clearRecents() },
                onOpenEntry = { entry ->
                    edited = entry
                    editorOpen = true
                },
                onClose = {
                    searchOpen = false
                    viewModel.resetSearch()
                }
            )
        }

        AnimatedVisibility(
            visible = editorOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(300)) { it } + fadeIn(tween(180)),
            exit = slideOutVertically(tween(240)) { it } + fadeOut(tween(180))
        ) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                        // The scrim absorbs taps; the sheet closes only through Cancel, Save
                        // or Back, so a stray tap can never discard typed text.
                        .iosClick { }
                )
                EntryEditor(
                    entry = edited,
                    onDismiss = { editorOpen = false },
                    onSave = { draft ->
                        val existing = edited
                        if (existing == null) {
                            viewModel.create(draft)
                        } else {
                            viewModel.update(existing, draft)
                        }
                        editorOpen = false
                    },
                    onDelete = {
                        edited?.let { viewModel.delete(it) }
                        editorOpen = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UndoBanner(name: String, onUndo: () -> Unit, onDismiss: () -> Unit) {
    val colors = Ios.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .glass(RoundedCornerShape(14.dp), colors)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertGlyph(colors.red, size = 14.dp)
        Spacer(Modifier.width(8.dp))
        IText(
            "Deleted “$name”",
            modifier = Modifier.weight(1f),
            style = IosType.footnote,
            color = colors.label,
            maxLines = 1
        )
        Spacer(Modifier.width(10.dp))
        IText(
            "Undo",
            modifier = Modifier.iosClick(onClick = onUndo),
            style = IosType.footnote.copy(fontWeight = FontWeight.SemiBold),
            color = colors.blue,
            maxLines = 1
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.size(22.dp).iosClick { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            CloseGlyph(colors.tertiaryLabel, size = 10.dp)
        }
    }
}
