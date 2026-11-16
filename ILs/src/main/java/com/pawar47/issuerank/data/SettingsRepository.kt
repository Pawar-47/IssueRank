package com.pawar47.issuerank.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pawar47.issuerank.domain.Recents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ils_settings"
)

data class Settings(val recentSearches: List<String> = emptyList())

/** Small preferences store: only search history today, ready for appearance options. */
class SettingsRepository(private val context: Context) {

    private val recentSearchesKey = stringPreferencesKey("recent_searches")

    val settings: Flow<Settings> = context.settingsDataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            Settings(recentSearches = Recents.decode(preferences[recentSearchesKey]))
        }

    suspend fun addRecentSearch(query: String) {
        context.settingsDataStore.edit { preferences ->
            val current = Recents.decode(preferences[recentSearchesKey])
            preferences[recentSearchesKey] = Recents.encode(Recents.add(current, query))
        }
    }

    suspend fun removeRecentSearch(query: String) {
        context.settingsDataStore.edit { preferences ->
            val current = Recents.decode(preferences[recentSearchesKey])
            preferences[recentSearchesKey] = Recents.encode(Recents.remove(current, query))
        }
    }

    suspend fun clearRecentSearches() {
        context.settingsDataStore.edit { preferences ->
            preferences.remove(recentSearchesKey)
        }
    }
}
