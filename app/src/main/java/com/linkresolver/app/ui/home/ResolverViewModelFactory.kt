package com.linkresolver.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.linkresolver.app.data.repository.ResolverRepository

class ResolverViewModelFactory(
    private val repository: ResolverRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResolverViewModel::class.java)) {
            return ResolverViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
