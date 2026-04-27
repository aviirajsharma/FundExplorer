package com.avirajsharma.fundexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avirajsharma.fundexplorer.data.model.FundDetailResponse
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.data.model.WatchlistFolder
import com.avirajsharma.fundexplorer.data.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface ExploreUiState {
    object Loading : ExploreUiState
    data class Success(val categoryFunds: Map<String, List<FundSearchResult>>) : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<FundSearchResult>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

sealed interface FundDetailUiState {
    object Loading : FundDetailUiState
    data class Success(val details: FundDetailResponse) : FundDetailUiState
    data class Error(val message: String) : FundDetailUiState
}

@HiltViewModel
class FundViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _exploreUiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val exploreUiState: StateFlow<ExploreUiState> = _exploreUiState.asStateFlow()

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _fundDetailUiState = MutableStateFlow<FundDetailUiState>(FundDetailUiState.Loading)
    val fundDetailUiState: StateFlow<FundDetailUiState> = _fundDetailUiState.asStateFlow()

    val watchlistFolders: StateFlow<List<WatchlistFolder>> = repository.getWatchlistFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchFunds(query: String) {
        if (query.isBlank()) {
            _searchUiState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            repository.searchFunds(query)
                .onSuccess {
                    _searchUiState.value = SearchUiState.Success(it)
                }
                .onFailure {
                    _searchUiState.value = SearchUiState.Error(it.message ?: "Search failed")
                }
        }
    }

    fun fetchExploreData() {
        val categories =
            listOf("Index Funds", "Bluechip Funds", "Tax Saver (ELSS)", "Large Cap Funds")
        viewModelScope.launch {
            _exploreUiState.value = ExploreUiState.Loading
            val categoryData = mutableMapOf<String, List<FundSearchResult>>()
            
            try {
                categories.forEach { category ->
                    val query = when (category) {
                        "Index Funds" -> "Index"
                        "Bluechip Funds" -> "Bluechip"
                        "Tax Saver (ELSS)" -> "Tax"
                        "Large Cap Funds" -> "Large Cap"
                        else -> category
                    }
                    repository.getFundsByCategory(query).collect { result ->
                        result.onSuccess { funds ->
                            categoryData[category] = funds
                            _exploreUiState.value = ExploreUiState.Success(categoryData.toMap())
                        }
                    }
                }

                repository.getFundsByCategory("Equity").collect { result ->
                    result.onSuccess { funds ->
                        categoryData["All Funds"] = funds
                        _exploreUiState.value = ExploreUiState.Success(categoryData.toMap())
                    }
                }
            } catch (e: Exception) {
                _exploreUiState.value = ExploreUiState.Error(e.message ?: "Failed to fetch explore data")
            }
        }
    }

    fun fetchFundDetails(schemeCode: Int) {
        viewModelScope.launch {
            _fundDetailUiState.value = FundDetailUiState.Loading
            repository.getFundDetails(schemeCode)
                .onSuccess {
                    _fundDetailUiState.value = FundDetailUiState.Success(it)
                }
                .onFailure {
                    _fundDetailUiState.value = FundDetailUiState.Error(it.message ?: "Failed to fetch details")
                }
        }
    }

    fun createWatchlistFolder(name: String) {
        viewModelScope.launch {
            repository.createFolder(name, UUID.randomUUID().toString())
        }
    }

    fun addFundToWatchlist(folderId: String, fund: FundSearchResult) {
        viewModelScope.launch {
            repository.addFundToFolder(folderId, fund)
        }
    }

    fun removeFundFromWatchlist(folderId: String, schemeCode: Int) {
        viewModelScope.launch {
            repository.removeFundFromFolder(folderId, schemeCode)
        }
    }

    fun isFundInWatchlist(schemeCode: Int): Flow<Boolean> {
        return watchlistFolders.map { folders ->
            folders.any { folder -> folder.funds.any { it.schemeCode == schemeCode } }
        }
    }
}
