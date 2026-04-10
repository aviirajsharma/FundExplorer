package com.avirajsharma.fundexplorer.data.model

import com.google.gson.annotations.SerializedName

data class FundDetailResponse(
    @SerializedName("meta") val meta: Meta,
    @SerializedName("data") val navHistory: List<NavEntry>,
    @SerializedName("status") val status: String
)

data class Meta(
    @SerializedName("fund_house") val fundHouse: String,
    @SerializedName("scheme_type") val schemeType: String,
    @SerializedName("scheme_category") val schemeCategory: String,
    @SerializedName("scheme_code") val schemeCode: Int,
    @SerializedName("scheme_name") val schemeName: String
)

data class NavEntry(
    @SerializedName("date") val date: String,
    @SerializedName("nav") val nav: String
)
