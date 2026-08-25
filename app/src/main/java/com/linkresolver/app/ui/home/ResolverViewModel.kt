package com.linkresolver.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linkresolver.app.data.repository.ResolverRepository
import com.linkresolver.app.domain.model.ResolverStatus
import com.linkresolver.app.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResolverViewModel(
    private val repository: ResolverRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ResolverUiState>(ResolverUiState.Idle)
    val state: StateFlow<ResolverUiState> = _state.asStateFlow()

    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    val history = repository.observeHistory()
    val settings = repository.settings

    fun onUrlChanged(newValue: String) {
        _urlInput.value = newValue
    }

    fun clearInput() {
        _urlInput.value = ""
    }

    fun setInputFromShare(sharedText: String) {
        _urlInput.value = extractFirstUrl(sharedText) ?: sharedText.trim()
    }

    fun resolve() {
        val url = _urlInput.value.trim()
        if (url.isEmpty()) return

        viewModelScope.launch {
            _state.value = ResolverUiState.Loading(ResolveProgressMessages.CHECKING_URL)

            val result = repository.resolve(url) { message ->
                _state.value = ResolverUiState.Loading(message)
            }

            AppLogger.resolutionEvent(
                provider = result.provider,
                status = result.status.name,
                redirectCount = (result.redirects.size - 1).coerceAtLeast(0),
                durationMs = result.elapsedMs
            )

            _state.value = if (result.status == ResolverStatus.SUCCESS) {
                ResolverUiState.Success(result)
            } else {
                ResolverUiState.Error(result)
            }
        }
    }

    fun resetToIdle() {
        _state.value = ResolverUiState.Idle
    }

    suspend fun deleteHistoryEntry(entry: com.linkresolver.app.data.local.ResolutionHistoryEntity) {
        repository.deleteHistoryItem(entry)
    }

    suspend fun clearHistory() {
        repository.clearHistory()
    }

    fun updateSettings(transform: (com.linkresolver.app.data.local.AppSettings) -> com.linkresolver.app.data.local.AppSettings) {
        repository.updateSettings(transform)
    }

    private fun extractFirstUrl(text: String): String? {
        val regex = Regex("""https?://[^\s]+""")
        return regex.find(text)?.value
    }
}
