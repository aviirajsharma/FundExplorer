package com.avirajsharma.fundexplorer.data.repository

import com.avirajsharma.fundexplorer.data.api.MFApi
import com.avirajsharma.fundexplorer.data.local.*
import com.avirajsharma.fundexplorer.data.model.FundDetailResponse
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.data.model.WatchlistFolder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FundRepository @Inject constructor(
    private val api: MFApi,
    private val dao: WatchlistDao
) {
    private val gson = Gson()

    suspend fun searchFunds(query: String): Result<List<FundSearchResult>> = withContext(Dispatchers.IO) {
        runCatching {
            api.searchFunds(query)
        }
    }

    suspend fun getFundDetails(schemeCode: Int): Result<FundDetailResponse> = withContext(Dispatchers.IO) {
        runCatching {
            api.getFundDetails(schemeCode)
        }
    }

    suspend fun getFundsByCategory(category: String): Flow<Result<List<FundSearchResult>>> = flow {
        val cached = dao.getExploreCache(category)
        if (cached != null) {
            val type = object : TypeToken<List<FundSearchResult>>() {}.type
            val funds: List<FundSearchResult> = gson.fromJson(cached.fundsJson, type)
            emit(Result.success(funds))
        }

        val query = when (category.lowercase()) {
            "index" -> "index"
            "bluechip" -> "bluechip"
            "tax" -> "tax"
            else -> category
        }

        try {
            val apiResults = api.searchFunds(query)
            dao.insertExploreCache(
                ExploreCacheEntity(
                    category = category,
                    fundsJson = gson.toJson(apiResults.take(10))
                )
            )
            emit(Result.success(apiResults.take(10)))
        } catch (e: Exception) {
            if (cached == null) {
                emit(Result.failure(e))
            }
        }
    }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWatchlistFolders(): Flow<List<WatchlistFolder>> {
        return dao.getAllFolders().flatMapLatest { folderEntities ->
            if (folderEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    folderEntities.map { entity ->
                        dao.getFundsInFolder(entity.id).map { funds ->
                            WatchlistFolder(
                                id = entity.id,
                                name = entity.name,
                                funds = funds.map { FundSearchResult(it.schemeCode, it.schemeName) }
                            )
                        }
                    }
                ) { it.toList() }
            }
        }
    }

    suspend fun createFolder(name: String, id: String) = withContext(Dispatchers.IO) {
        dao.insertFolder(WatchlistFolderEntity(id, name))
    }

    suspend fun deleteFolder(id: String, name: String) = withContext(Dispatchers.IO) {
        dao.deleteFolder(WatchlistFolderEntity(id, name))
    }

    suspend fun addFundToFolder(folderId: String, fund: FundSearchResult) = withContext(Dispatchers.IO) {
        dao.insertFund(
            WatchlistFundEntity(
                folderId = folderId,
                schemeCode = fund.schemeCode,
                schemeName = fund.schemeName
            )
        )
    }

    suspend fun removeFundFromFolder(folderId: String, schemeCode: Int) = withContext(Dispatchers.IO) {
        dao.deleteFund(folderId, schemeCode)
    }
}
