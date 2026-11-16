package com.pawar47.issuerank.data

import android.content.Context
import com.pawar47.issuerank.domain.Resolution
import com.pawar47.issuerank.domain.Status
import org.json.JSONArray

/** One record as it was stored by version 1.0 of the app, before the database existed. */
data class LegacyRecord(
    val name: String,
    val notes: String,
    val status: Status,
    val resolution: Resolution
)

/**
 * Reads the JSON document written by version 1.0 and then removes it, so upgrading keeps
 * every record and pays the migration cost exactly once.
 */
class LegacyStore(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("issuerank", Context.MODE_PRIVATE)

    fun hasData(): Boolean = prefs.contains(KEY)

    fun read(): List<LegacyRecord> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val json = array.optJSONObject(index) ?: return@mapNotNull null
                LegacyRecord(
                    name = json.optString("name").ifBlank { "Untitled" },
                    notes = json.optString("notes"),
                    status = Status.fromName(json.optString("status")),
                    resolution = Resolution.fromName(json.optString("resolution"))
                )
            }
        }.getOrDefault(emptyList())
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private companion object {
        const val KEY = "entries"
    }
}
