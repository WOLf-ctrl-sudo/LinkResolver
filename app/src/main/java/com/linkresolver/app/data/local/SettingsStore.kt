package com.linkresolver.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val autoCopyFinalUrl: Boolean = false,
    val autoOpenFinalUrl: Boolean = false,
    val saveHistory: Boolean = true,
    val maxRedirects: Int = 20,
    val timeoutSeconds: Int = 30,
    val browserFallbackEnabled: Boolean = true,
    val stripSensitiveParams: Boolean = true
)

/**
 * Lightweight settings persistence. A small SharedPreferences wrapper
 * is used instead of a full DataStore dependency to keep the sample
 * self-contained; it's easy to swap for DataStore later since all
 * reads/writes go through this single class.
 */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("link_resolver_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun load(): AppSettings = AppSettings(
        autoCopyFinalUrl = prefs.getBoolean(KEY_AUTO_COPY, false),
        autoOpenFinalUrl = prefs.getBoolean(KEY_AUTO_OPEN, false),
        saveHistory = prefs.getBoolean(KEY_SAVE_HISTORY, true),
        maxRedirects = prefs.getInt(KEY_MAX_REDIRECTS, 20),
        timeoutSeconds = prefs.getInt(KEY_TIMEOUT, 30),
        browserFallbackEnabled = prefs.getBoolean(KEY_BROWSER_FALLBACK, true),
        stripSensitiveParams = prefs.getBoolean(KEY_STRIP_SENSITIVE, true)
    )

    fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        prefs.edit()
            .putBoolean(KEY_AUTO_COPY, updated.autoCopyFinalUrl)
            .putBoolean(KEY_AUTO_OPEN, updated.autoOpenFinalUrl)
            .putBoolean(KEY_SAVE_HISTORY, updated.saveHistory)
            .putInt(KEY_MAX_REDIRECTS, updated.maxRedirects)
            .putInt(KEY_TIMEOUT, updated.timeoutSeconds)
            .putBoolean(KEY_BROWSER_FALLBACK, updated.browserFallbackEnabled)
            .putBoolean(KEY_STRIP_SENSITIVE, updated.stripSensitiveParams)
            .apply()
    }

    companion object {
        private const val KEY_AUTO_COPY = "auto_copy_final_url"
        private const val KEY_AUTO_OPEN = "auto_open_final_url"
        private const val KEY_SAVE_HISTORY = "save_history"
        private const val KEY_MAX_REDIRECTS = "max_redirects"
        private const val KEY_TIMEOUT = "timeout_seconds"
        private const val KEY_BROWSER_FALLBACK = "browser_fallback"
        private const val KEY_STRIP_SENSITIVE = "strip_sensitive_params"
    }
}
