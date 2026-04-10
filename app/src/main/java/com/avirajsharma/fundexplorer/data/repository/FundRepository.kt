package com.avirajsharma.fundexplorer.data.repository

import com.avirajsharma.fundexplorer.data.api.MFApi
import com.avirajsharma.fundexplorer.data.model.FundDetailResponse
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FundRepository(private val api: MFApi) {

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

    suspend fun getFundsByCategory(category: String): Result<List<FundSearchResult>> {
        val query = when (category.lowercase()) {
            "index" -> "index"
            "bluechip" -> "bluechip"
            "tax" -> "tax"
            else -> category
        }
        return searchFunds(query)
    }
}
