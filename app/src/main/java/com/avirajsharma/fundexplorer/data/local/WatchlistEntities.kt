package com.avirajsharma.fundexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist_folders")
data class WatchlistFolderEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "watchlist_funds", primaryKeys = ["folderId", "schemeCode"])
data class WatchlistFundEntity(
    val folderId: String,
    val schemeCode: Int,
    val schemeName: String
)

@Entity(tableName = "explore_cache")
data class ExploreCacheEntity(
    @PrimaryKey val category: String,
    val fundsJson: String, // Simplified for caching
    val lastUpdated: Long = System.currentTimeMillis()
)
