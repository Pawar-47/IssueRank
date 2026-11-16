package com.pawar47.issuerank

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.pawar47.issuerank.data.Entry
import com.pawar47.issuerank.data.EntryStore
import com.pawar47.issuerank.data.Resolution
import com.pawar47.issuerank.data.Status

class EntriesViewModel(app: Application) : AndroidViewModel(app) {
    private val store = EntryStore(app)

    /** Ordered by rank: index 0 is rank 1. */
    val entries = mutableStateListOf<Entry>().apply { addAll(store.load()) }

    private fun persist() = store.save(entries.toList())

    fun add(name: String, notes: String, status: Status, resolution: Resolution) {
        val id = (entries.maxOfOrNull { it.id } ?: 0L) + 1L
        entries.add(Entry(id, name, notes, status, resolution))
        persist()
    }

    fun update(entry: Entry) {
        val i = entries.indexOfFirst { it.id == entry.id }
        if (i >= 0) {
            entries[i] = entry
            persist()
        }
    }

    fun delete(id: Long) {
        val i = entries.indexOfFirst { it.id == id }
        if (i >= 0) {
            entries.removeAt(i)
            persist()
        }
    }

    /** Moves an entry up (delta -1) or down (delta +1) in the ranking. */
    fun move(id: Long, delta: Int) {
        val i = entries.indexOfFirst { it.id == id }
        val j = i + delta
        if (i < 0 || j < 0 || j >= entries.size) return
        val e = entries.removeAt(i)
        entries.add(j, e)
        persist()
    }

    fun setStatus(id: Long, status: Status) {
        val e = entries.firstOrNull { it.id == id } ?: return
        update(e.copy(status = status))
    }

    fun setResolution(id: Long, resolution: Resolution) {
        val e = entries.firstOrNull { it.id == id } ?: return
        update(e.copy(resolution = resolution))
    }
}
