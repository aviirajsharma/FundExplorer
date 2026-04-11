package com.avirajsharma.fundexplorer.data.model

data class WatchlistFolder(
    val id: String,
    val name: String,
    val funds: List<FundSearchResult> = emptyList()
)
