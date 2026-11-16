package com.pawar47.issuerank

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pawar47.issuerank.data.EntryRepository
import com.pawar47.issuerank.data.IssueRankDatabase
import com.pawar47.issuerank.data.LegacyStore
import com.pawar47.issuerank.data.SettingsRepository
import com.pawar47.issuerank.domain.Dashboard
import com.pawar47.issuerank.domain.Entry
import com.pawar47.issuerank.domain.EntryDraft
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.SearchFilter
import com.pawar47.issuerank.domain.SearchHit
import com.pawar47.issuerank.domain.SearchQuery
import com.pawar47.issuerank.domain.SearchScope
import com.pawar47.issuerank.domain.SearchSort
import com.pawar47.issuerank.domain.Status
import com.pawar47.issuerank.domain.buildDashboard
import com.pawar47.issuerank.domain.search
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Everything a screen needs, derived once per change. */
data class SearchUiState(
    val query: SearchQuery = SearchQuery(),
    val results: List<SearchHit> = emptyList()
)

data class AppState(
    val loaded: Boolean = false,
    val entries: List<Entry> = emptyList(),
    val dashboard: Dashboard = Dashboard(),
    val search: SearchUiState = SearchUiState(),
    val recents: List<String> = emptyList()
)

class EntriesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = IssueRankDatabase.get(application)
    private val settingsRepository = SettingsRepository(application)
    private val repository = EntryRepository(database.entryDao(), LegacyStore(application))

    private val searchQuery = MutableStateFlow(SearchQuery())
    private val deleted = MutableStateFlow<Entry?>(null)

    init {
        viewModelScope.launch { repository.importLegacyIfNeeded() }
    }

    val state: StateFlow<AppState> = combine(
        repository.entries,
        searchQuery,
        settingsRepository.settings
    ) { entries, query, settings ->
        val now = System.currentTimeMillis()
        AppState(
            loaded = true,
            entries = entries,
            dashboard = buildDashboard(entries, now),
            search = SearchUiState(query = query, results = search(entries, query)),
            recents = settings.recentSearches
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppState()
    )

    /** The last deleted record, kept so the banner can undo it. */
    val lastDeleted: StateFlow<Entry?> = deleted.asStateFlow()

    // ---------------------------------------------------------------- records

    fun create(draft: EntryDraft) = viewModelScope.launch { repository.create(draft) }

    fun update(entry: Entry, draft: EntryDraft) =
        viewModelScope.launch { repository.update(entry, draft) }

    fun move(id: Long, delta: Int) = viewModelScope.launch { repository.move(id, delta) }

    fun setStatus(entry: Entry, status: Status) =
        viewModelScope.launch { repository.setStatus(entry, status) }

    fun setResolution(entry: Entry, resolution: Resolution) =
        viewModelScope.launch { repository.setResolution(entry, resolution) }

    fun toggleStatus(entry: Entry) = setStatus(entry, entry.status.toggled())

    fun toggleResolution(entry: Entry) = setResolution(entry, entry.resolution.toggled())

    fun delete(entry: Entry) {
        deleted.value = entry
        viewModelScope.launch { repository.delete(entry.id) }
    }

    fun undoDelete() {
        val entry = deleted.value ?: return
        deleted.value = null
        viewModelScope.launch { repository.restore(entry) }
    }

    fun dismissUndo() {
        deleted.value = null
    }

    // ---------------------------------------------------------------- search

    fun setSearchText(text: String) = searchQuery.update { it.copy(text = text) }

    fun setSearchScope(scope: SearchScope) = searchQuery.update { it.copy(scope = scope) }

    fun setSearchFilter(filter: SearchFilter) = searchQuery.update { it.copy(filter = filter) }

    fun setSearchSort(sort: SearchSort) = searchQuery.update { it.copy(sort = sort) }

    fun applyRecentSearch(text: String) {
        searchQuery.update { it.copy(text = text) }
        submitSearch()
    }

    fun resetSearch() {
        searchQuery.value = SearchQuery()
    }

    /** Records the current query in history when the user commits it. */
    fun submitSearch() {
        val text = searchQuery.value.text
        if (text.isBlank()) return
        viewModelScope.launch { settingsRepository.addRecentSearch(text) }
    }

    fun removeRecent(query: String) =
        viewModelScope.launch { settingsRepository.removeRecentSearch(query) }

    fun clearRecents() = viewModelScope.launch { settingsRepository.clearRecentSearches() }
}
