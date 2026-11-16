package com.pawar47.issuerank.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class Status(val label: String) {
    Pending("Pending"),
    Completed("Completed")
}

enum class Resolution(val label: String) {
    Resolved("Resolved"),
    NotResolved("Not Resolved")
}

/**
 * One tracked item. The position of an entry in the master list is its rank
 * (index 0 = rank 1), so rank is always unique and contiguous.
 */
data class Entry(
    val id: Long,
    val name: String,
    val notes: String = "",
    val status: Status = Status.Pending,
    val resolution: Resolution = Resolution.NotResolved
)

/** Tiny persistence layer: the whole list as one JSON string in SharedPreferences. */
class EntryStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences("issuerank", Context.MODE_PRIVATE)

    fun load(): List<Entry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                Entry(
                    id = o.getLong("id"),
                    name = o.optString("name"),
                    notes = o.optString("notes"),
                    status = runCatching { Status.valueOf(o.optString("status")) }
                        .getOrDefault(Status.Pending),
                    resolution = runCatching { Resolution.valueOf(o.optString("resolution")) }
                        .getOrDefault(Resolution.NotResolved)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(entries: List<Entry>) {
        val array = JSONArray()
        entries.forEach { e ->
            val o = JSONObject()
            o.put("id", e.id)
            o.put("name", e.name)
            o.put("notes", e.notes)
            o.put("status", e.status.name)
            o.put("resolution", e.resolution.name)
            array.put(o)
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object {
        const val KEY = "entries"
    }
}
