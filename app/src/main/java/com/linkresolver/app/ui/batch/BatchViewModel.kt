package com.linkresolver.app.ui.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.linkresolver.app.data.repository.ResolverRepository
import com.linkresolver.app.domain.model.ResolveResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

sealed interface BatchUiState {
    data object Idle : BatchUiState
    data class Running(val total: Int, val completed: Int) : BatchUiState
    data class Done(val results: List<ResolveResult>) : BatchUiState
}

class BatchViewModel(
    private val repository: ResolverRepository
) : ViewModel() {

    companion object {
        const val MAX_CONCURRENT = 3
    }

    private val _state = MutableStateFlow<BatchUiState>(BatchUiState.Idle)
    val state: StateFlow<BatchUiState> = _state.asStateFlow()

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    fun onInputChanged(value: String) {
        _input.value = value
    }

    fun resolveAll() {
        val urls = _input.value
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        if (urls.isEmpty()) return

        viewModelScope.launch {
            _state.value = BatchUiState.Running(total = urls.size, completed = 0)

            val semaphore = Semaphore(MAX_CONCURRENT)
            var completed = 0
            val results = mutableListOf<ResolveResult>()

            val jobs = urls.map { url ->
                async {
                    semaphore.withPermit {
                        val result = repository.resolve(url)
                        completed++
                        _state.value = BatchUiState.Running(total = urls.size, completed = completed)
                        result
                    }
                }
            }
            jobs.forEach { results.add(it.await()) }

            _state.value = BatchUiState.Done(results)
        }
    }

    fun reset() {
        _state.value = BatchUiState.Idle
    }
}

class BatchViewModelFactory(
    private val repository: ResolverRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BatchViewModel::class.java)) {
            return BatchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
