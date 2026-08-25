package com.linkresolver.app.util

import android.util.Log
import com.linkresolver.app.BuildConfig

/**
 * Central logging point. In release builds this never logs full URLs
 * (which may carry sensitive query parameters) — only high-level,
 * non-identifying resolution metadata.
 */
object AppLogger {
    private const val TAG = "LinkResolver"

    fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun resolutionEvent(provider: String, status: String, redirectCount: Int, durationMs: Long) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "provider=$provider status=$status redirects=$redirectCount durationMs=$durationMs")
        }
        // Release builds intentionally log nothing here to avoid
        // capturing URLs or other potentially sensitive data.
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}
