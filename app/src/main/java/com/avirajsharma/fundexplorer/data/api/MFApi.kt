package com.avirajsharma.fundexplorer.data.api

import com.avirajsharma.fundexplorer.data.model.FundDetailResponse
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MFApi {
    @GET("mf/search")
    suspend fun searchFunds(@Query("q") query: String): List<FundSearchResult>

    @GET("mf/{schemeCode}")
    suspend fun getFundDetails(@Path("schemeCode") schemeCode: Int): FundDetailResponse

    companion object {
        const val BASE_URL = "https://api.mfapi.in/"
    }
}
