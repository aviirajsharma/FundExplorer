package com.avirajsharma.fundexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avirajsharma.fundexplorer.data.model.FundDetailResponse
import com.avirajsharma.fundexplorer.data.model.FundSearchResult
import com.avirajsharma.fundexplorer.data.repository.FundRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FundViewModel(private val repository: FundRepository) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<FundSearchResult>>(emptyList())
    val searchResults: StateFlow<List<FundSearchResult>> = _searchResults.asStateFlow()

    private val _categoryFunds = MutableStateFlow<Map<String, List<FundSearchResult>>>(emptyMap())
    val categoryFunds: StateFlow<Map<String, List<FundSearchResult>>> = _categoryFunds.asStateFlow()

    private val _fundDetails = MutableStateFlow<FundDetailResponse?>(null)
    val fundDetails: StateFlow<FundDetailResponse?> = _fundDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun searchFunds(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.searchFunds(query)
                .onSuccess {
                    _searchResults.value = it
                    _error.value = null
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _isLoading.value = false
        }
    }

    fun fetchExploreData() {
        val categories = listOf("Index", "Bluechip", "Tax")
        viewModelScope.launch {
            _isLoading.value = true
            val results = mutableMapOf<String, List<FundSearchResult>>()
            categories.forEach { category ->
                repository.getFundsByCategory(category)
                    .onSuccess {
                        results[category] = it.take(10) // Limit for explore screen
                    }
            }
            _categoryFunds.value = results
            _isLoading.value = false
        }
    }

    fun fetchFundDetails(schemeCode: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getFundDetails(schemeCode)
                .onSuccess {
                    _fundDetails.value = it
                    _error.value = null
                }
                .onFailure {
                    _error.value = it.message ?: "Unknown error"
                }
            _isLoading.value = false
        }
    }
}
