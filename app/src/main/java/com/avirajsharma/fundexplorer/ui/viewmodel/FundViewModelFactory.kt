package com.avirajsharma.fundexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.avirajsharma.fundexplorer.data.repository.FundRepository

class FundViewModelFactory(private val repository: FundRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FundViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
