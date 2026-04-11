package com.avirajsharma.fundexplorer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist_folders")
    fun getAllFolders(): Flow<List<WatchlistFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: WatchlistFolderEntity)

    @Delete
    suspend fun deleteFolder(folder: WatchlistFolderEntity)

    @Query("SELECT * FROM watchlist_funds WHERE folderId = :folderId")
    fun getFundsInFolder(folderId: String): Flow<List<WatchlistFundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFund(fund: WatchlistFundEntity)

    @Query("DELETE FROM watchlist_funds WHERE folderId = :folderId AND schemeCode = :schemeCode")
    suspend fun deleteFund(folderId: String, schemeCode: Int)

    @Query("SELECT * FROM explore_cache WHERE category = :category")
    suspend fun getExploreCache(category: String): ExploreCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExploreCache(cache: ExploreCacheEntity)
}
