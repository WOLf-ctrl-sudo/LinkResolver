package com.linkresolver.app.data.repository

import android.net.Uri
import com.linkresolver.app.data.local.AppSettings
import com.linkresolver.app.data.local.ResolutionHistoryDao
import com.linkresolver.app.data.local.ResolutionHistoryEntity
import com.linkresolver.app.data.local.SettingsStore
import com.linkresolver.app.data.network.RedirectResolver
import com.linkresolver.app.data.network.UrlValidator
import com.linkresolver.app.domain.model.ResolveResult
import com.linkresolver.app.domain.model.ResolverStatus
import com.linkresolver.app.domain.resolver.ProviderRegistry
import com.linkresolver.app.domain.resolver.ResolverContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ResolverRepository(
    private val historyDao: ResolutionHistoryDao,
    private val settingsStore: SettingsStore
) {

    val settings: Flow<AppSettings> get() = settingsStore.settings

    fun observeHistory(): Flow<List<ResolutionHistoryEntity>> = historyDao.observeAll()

    suspend fun resolve(rawUrl: String, onProgress: (String) -> Unit = {}): ResolveResult {
        val currentSettings = settingsStore.settings.first()

        val uri: Uri = UrlValidator.toUri(rawUrl)
            ?: return ResolveResult(
                originalUrl = rawUrl,
                finalUrl = null,
                provider = "Unknown",
                status = ResolverStatus.INVALID_URL,
                redirects = emptyList(),
                elapsedMs = 0,
                errorMessage = "Please enter a valid HTTP or HTTPS URL."
            )

        val provider = ProviderRegistry.findProvider(uri)
        val resolver = RedirectResolver(maxRedirects = currentSettings.maxRedirects)
        val context = ResolverContext(
            genericResolver = resolver,
            maxRedirects = currentSettings.maxRedirects,
            onProgress = onProgress
        )

        val result = provider.resolve(uri, context)

        if (currentSettings.saveHistory) {
            saveToHistory(result, currentSettings)
        }

        return result
    }

    private suspend fun saveToHistory(result: ResolveResult, settings: AppSettings) {
        val storedFinalUrl = result.finalUrl?.let { url ->
            if (settings.stripSensitiveParams) UrlValidator.stripSensitiveParams(url) else url
        }
        val storedOriginalUrl = if (settings.stripSensitiveParams) {
            UrlValidator.stripSensitiveParams(result.originalUrl)
        } else {
            result.originalUrl
        }

        historyDao.insert(
            ResolutionHistoryEntity(
                originalUrl = storedOriginalUrl,
                finalUrl = storedFinalUrl,
                provider = result.provider,
                status = result.status.name,
                redirectCount = (result.redirects.size - 1).coerceAtLeast(0),
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistoryItem(entity: ResolutionHistoryEntity) = historyDao.delete(entity)

    suspend fun deleteHistoryItemById(id: Long) = historyDao.deleteById(id)

    suspend fun clearHistory() = historyDao.clearAll()

    fun updateSettings(transform: (AppSettings) -> AppSettings) = settingsStore.update(transform)
}
