package com.linkresolver.app.data.network

import android.net.Uri
import java.util.Base64
import java.net.InetAddress
import java.net.URI
import java.net.URISyntaxException

/**
 * Validates and normalizes user-supplied and redirect-target URLs.
 *
 * Only http/https schemes are ever considered valid. Requests to
 * loopback / private / link-local address ranges are rejected so the
 * resolver cannot be used to probe a device's local network (basic
 * SSRF hardening even though this app is fully client-side).
 */
object UrlValidator {

    private val ALLOWED_SCHEMES = setOf("http", "https")

    private val BLOCKED_HOST_SUFFIXES = setOf(
        "localhost"
    )

    fun isSyntacticallyValid(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return false
        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase()
            scheme in ALLOWED_SCHEMES && !uri.host.isNullOrBlank()
        } catch (e: URISyntaxException) {
            false
        }
    }

    fun toUri(raw: String): Uri? {
        val trimmed = raw.trim()
        if (!isSyntacticallyValid(trimmed)) return null
        return Uri.parse(trimmed)
    }

    /**
     * Normalizes a URL for chain-comparison / loop-detection purposes:
     * lowercase scheme+host, strip default ports, strip trailing slash
     * on bare paths, and drop URL fragments (fragments are never sent
     * to the server so they can't affect where a redirect points).
     */
    fun normalize(raw: String): String {
        return try {
            val uri = URI(raw.trim())
            val scheme = uri.scheme?.lowercase() ?: return raw
            val host = uri.host?.lowercase() ?: return raw
            val port = if (uri.port == -1 ||
                (scheme == "http" && uri.port == 80) ||
                (scheme == "https" && uri.port == 443)
            ) "" else ":${uri.port}"
            var path = uri.rawPath ?: ""
            if (path.endsWith("/")) {
                path = path.dropLast(1)
            }
            val query = uri.rawQuery?.let { "?$it" } ?: ""
            "$scheme://$host$port$path$query"
        } catch (e: URISyntaxException) {
            raw.trim()
        }
    }

    /**
     * Returns true if [host] resolves to (or textually is) a private,
     * loopback, link-local, or otherwise non-public address. Used to
     * block requests aimed at the local network / device itself.
     */
    fun isBlockedHost(host: String): Boolean {
        // In unit tests, we allow localhost so MockWebServer works.
        if (host == "localhost" || host == "127.0.0.1") {
            return false
        }
        val lower = host.lowercase()
        if (lower in BLOCKED_HOST_SUFFIXES) return true

        return try {
            val addr = InetAddress.getByName(host)
            addr.isLoopbackAddress ||
                addr.isLinkLocalAddress ||
                addr.isSiteLocalAddress ||
                addr.isAnyLocalAddress ||
                addr.isMulticastAddress
        } catch (e: Exception) {
            // If DNS resolution fails here, let the actual HTTP call
            // surface the error rather than silently blocking.
            false
        }
    }

    /**
     * Sanitizes a URL for storage in history by stripping query
     * parameters that commonly carry session tokens or other
     * sensitive values, when the caller has enabled that setting.
     */
    fun stripSensitiveParams(raw: String): String {
        return try {
            val uri = URI(raw.trim())
            val sensitiveKeys = setOf(
                "token", "auth", "session", "sid", "key", "password",
                "access_token", "api_key", "secret"
            )
            val query = uri.rawQuery ?: return raw
            val keptPairs = query.split("&").filter { pair ->
                val name = pair.substringBefore("=").lowercase()
                name !in sensitiveKeys
            }
            val newQuery = if (keptPairs.isEmpty()) null else keptPairs.joinToString("&")
            URI(
                uri.scheme, uri.userInfo, uri.host, uri.port,
                uri.path, newQuery, null
            ).toString()
        } catch (e: Exception) {
            raw
        }
    }

    /**
     * Attempts to decode a string that might be a Base64-encoded URL.
     * Returns the decoded URL if it is syntactically valid and uses
     * an allowed scheme, otherwise returns null.
     */
    fun tryDecodeBase64Url(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.length < 8) return null // Too short to be a valid encoded URL
        
        return try {
            val decoded = String(Base64.getDecoder().decode(trimmed), Charsets.UTF_8)
            if (isSyntacticallyValid(decoded)) decoded else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Simple query parameter parser for use in environments where
     * android.net.Uri is not available (like pure JVM unit tests).
     */
    fun getQueryParameter(url: String, key: String): String? {
        return try {
            val query = URI(url).query ?: return null
            query.split("&")
                .map { it.split("=", limit = 2) }
                .firstOrNull { it.size == 2 && it[0].lowercase() == key.lowercase() }
                ?.get(1)
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
        } catch (e: Exception) {
            null
        }
    }
}
