package com.avirajsharma.fundexplorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchlistFolderEntity::class,
        WatchlistFundEntity::class,
        ExploreCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FundDatabase : RoomDatabase() {
    abstract fun watchlistDao(): WatchlistDao
}
