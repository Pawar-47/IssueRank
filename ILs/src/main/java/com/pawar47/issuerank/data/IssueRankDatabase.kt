package com.pawar47.issuerank.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Version 1. Schema changes follow one rule: bump the version and add a [androidx.room.migration.Migration]
 * to [IssueRankDatabase.get]. Destructive fallback is never enabled — a user's records are
 * never thrown away to make a build pass.
 */
@Database(entities = [EntryEntity::class], version = 1, exportSchema = false)
abstract class IssueRankDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao

    companion object {
        private const val NAME = "ils.db"

        @Volatile
        private var instance: IssueRankDatabase? = null

        fun get(context: Context): IssueRankDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IssueRankDatabase::class.java,
                    NAME
                )
                    .build()
                    .also { instance = it }
            }
    }
}
