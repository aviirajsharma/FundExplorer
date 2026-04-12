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

@HiltViewModel
class FundViewModel @Inject constructor(
    private val repository: FundRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<FundSearchResult>>(emptyList())
    val searchResults: StateFlow<List<FundSearchResult>> = _searchResults.asStateFlow()

    private val _categoryFunds = MutableStateFlow<Map<String, List<FundSearchResult>>>(emptyMap())
    val categoryFunds: StateFlow<Map<String, List<FundSearchResult>>> = _categoryFunds.asStateFlow()

    private val _fundDetails = MutableStateFlow<FundDetailResponse?>(null)
    val fundDetails: StateFlow<FundDetailResponse?> = _fundDetails.asStateFlow()

    val watchlistFolders: StateFlow<List<WatchlistFolder>> = repository.getWatchlistFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun searchFunds(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.searchFunds(query)
                .onSuccess {
                    _searchResults.value = it
                }
                .onFailure {
                    _error.value = it.message ?: "Search failed"
                }
            _isLoading.value = false
        }
    }

    fun fetchExploreData() {
        val categories =
            listOf("Index Funds", "Bluechip Funds", "Tax Saver (ELSS)", "Large Cap Funds")
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
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
                        val current = _categoryFunds.value.toMutableMap()
                        current[category] = funds
                        _categoryFunds.value = current
                    }.onFailure {
                        _error.value = it.message ?: "Failed to fetch $category funds"
                    }
                }
            }

            repository.getFundsByCategory("Equity").collect { result ->
                result.onSuccess { funds ->
                    val current = _categoryFunds.value.toMutableMap()
                    current["All Funds"] = funds
                    _categoryFunds.value = current
                }
            }

            _isLoading.value = false
        }
    }

    fun fetchFundDetails(schemeCode: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getFundDetails(schemeCode)
                .onSuccess {
                    _fundDetails.value = it
                }
                .onFailure {
                    _error.value = it.message ?: "Failed to fetch details"
                }
            _isLoading.value = false
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
