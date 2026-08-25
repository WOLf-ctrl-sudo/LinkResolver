package com.linkresolver.app.data.network

import com.linkresolver.app.domain.model.RedirectStep
import com.linkresolver.app.domain.model.ResolveResult
import com.linkresolver.app.domain.model.ResolverStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Locale

/**
 * Manually walks an HTTP(S) redirect chain, one hop at a time, so every
 * intermediate URL can be captured and shown to the user.
 *
 * This resolver only follows redirect mechanisms that a normal browser
 * would follow automatically and that the destination server exposes
 * publicly: HTTP 3xx Location headers, and HTML meta-refresh tags found
 * in an otherwise-normal 200 response. It never executes page
 * JavaScript, solves CAPTCHAs, or attempts to defeat bot-protection
 * challenges. When it detects that kind of page, it reports
 * [ResolverStatus.PROTECTED_REDIRECT] instead of guessing a result.
 */
class RedirectResolver(
    private val maxRedirects: Int = DEFAULT_MAX_REDIRECTS,
    private val client: okhttp3.OkHttpClient = HttpClient.instance
) {

    companion object {
        const val DEFAULT_MAX_REDIRECTS = 20

        private val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)

        // HTML meta-refresh, e.g. <meta http-equiv="refresh" content="0;url=https://...">
        // Updated to be more permissive with quotes and the optional 'url=' prefix.
        private val META_REFRESH_REGEX = Regex(
            """<meta[^>]+http-equiv=["']?refresh["']?[^>]+content=["']?\s*\d+\s*;\s*(?:url=)?([^"'>\s]+)["']?""",
            RegexOption.IGNORE_CASE
        )

        // JavaScript redirects like window.location.replace("...") or location.href = "..."
        private val JS_REDIRECT_REGEX = Regex(
            """(?:window\.)?location(?:\.replace|\.href)?\s*[=(]\s*["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        )

        // Common parameter names used by shorteners to hide the target URL
        private val BYPASS_PARAMS = setOf("url", "link", "target", "goto", "destination", "u")

        // Heuristics for pages that require real browser / human interaction
        // (JS challenge pages, CAPTCHA gates, "click to continue" ad-wait
        // pages). We only ever *detect* these to report an honest status —
        // never attempt to solve or bypass them.
        private val PROTECTED_MARKERS = listOf(
            "cf-browser-verification",
            "checking your browser",
            "g-recaptcha",
            "hcaptcha",
            "cf-chl",
            "please enable javascript",
            "verifying you are human"
        )
    }

    suspend fun resolve(
        startUrl: String,
        providerName: String,
        onProgress: (String) -> Unit = {}
    ): ResolveResult = withContext(Dispatchers.IO) {
        val startedAt = System.currentTimeMillis()
        val steps = mutableListOf<RedirectStep>()

        onProgress("Validating URL...")
        if (!UrlValidator.isSyntacticallyValid(startUrl)) {
            return@withContext ResolveResult(
                originalUrl = startUrl,
                finalUrl = null,
                provider = providerName,
                status = ResolverStatus.INVALID_URL,
                redirects = emptyList(),
                elapsedMs = 0,
                errorMessage = "Please enter a valid HTTP or HTTPS URL."
            )
        }

        onProgress("Detecting provider...")
        steps.add(RedirectStep(url = startUrl, statusCode = null, statusText = null, type = "ORIGIN"))

        val visited = mutableSetOf(UrlValidator.normalize(startUrl))
        var currentUrl = startUrl

        repeat(maxRedirects + 1) { hop ->
            if (hop > 0) {
                onProgress("Following redirect $hop...")
            } else {
                onProgress("Checking initial URL...")
            }
            val host = try {
                java.net.URI(currentUrl).host
            } catch (e: Exception) {
                null
            }

            if (host != null && UrlValidator.isBlockedHost(host)) {
                return@withContext failure(
                    startUrl, providerName, steps, startedAt,
                    ResolverStatus.NETWORK_ERROR,
                    "This link points to a local or private network address and was blocked."
                )
            }

            val response: Response = try {
                val request = Request.Builder()
                    .url(currentUrl)
                    .header("User-Agent", HttpClient.USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
                    .build()
                client.newCall(request).execute()
            } catch (e: SocketTimeoutException) {
                return@withContext failure(
                    startUrl, providerName, steps, startedAt,
                    ResolverStatus.TIMEOUT,
                    "The website took too long to respond. Try again or open it in your browser."
                )
            } catch (e: IOException) {
                return@withContext failure(
                    startUrl, providerName, steps, startedAt,
                    ResolverStatus.NETWORK_ERROR,
                    "Check your internet connection and try again."
                )
            }

            response.use { resp ->
                val code = resp.code

                if (code in REDIRECT_CODES) {
                    val location = resp.header("Location")
                    if (location.isNullOrBlank()) {
                        return@withContext failure(
                            startUrl, providerName, steps, startedAt,
                            ResolverStatus.NO_DESTINATION,
                            "The server returned a redirect with no destination."
                        )
                    }
                    val nextUrl = resolveRelative(currentUrl, location)
                    if (!UrlValidator.isSyntacticallyValid(nextUrl)) {
                        return@withContext failure(
                            startUrl, providerName, steps, startedAt,
                            ResolverStatus.NO_DESTINATION,
                            "The redirect target was not a valid HTTP/HTTPS URL."
                        )
                    }

                    val normalized = UrlValidator.normalize(nextUrl)
                    if (!visited.add(normalized)) {
                        return@withContext failure(
                            startUrl, providerName, steps, startedAt,
                            ResolverStatus.REDIRECT_LOOP,
                            "The website created a redirect loop."
                        )
                    }

                    steps.add(
                        RedirectStep(
                            url = nextUrl,
                            statusCode = code,
                            statusText = resp.message.ifBlank { httpStatusText(code) },
                            type = "HTTP_HEADER"
                        )
                    )
                    currentUrl = nextUrl
                    return@use
                }

                if (code in 200..299) {
                    val contentType = resp.header("Content-Type").orEmpty()
                    val bodySnippet = if (contentType.contains("text/html", ignoreCase = true)) {
                        // Peek at a larger snippet (128KB) to find bypass links/buttons
                        resp.body?.source()?.let { source ->
                            source.request(128L * 1024)
                            source.buffer.clone().readString(Charsets.UTF_8)
                        }.orEmpty()
                    } else {
                        ""
                    }

                    if (isProtectedPage(bodySnippet)) {
                        return@withContext failure(
                            startUrl, providerName, steps, startedAt,
                            ResolverStatus.PROTECTED_REDIRECT,
                            "This website requires browser verification or interaction. " +
                                "The app cannot resolve it automatically."
                        )
                    }

                    val metaMatch = META_REFRESH_REGEX.find(bodySnippet)
                    if (metaMatch != null) {
                        val target = metaMatch.groupValues[1].trim().trim('"', '\'')
                        val nextUrl = resolveRelative(currentUrl, target)
                        if (UrlValidator.isSyntacticallyValid(nextUrl)) {
                            val normalized = UrlValidator.normalize(nextUrl)
                            if (!visited.add(normalized)) {
                                return@withContext failure(
                                    startUrl, providerName, steps, startedAt,
                                    ResolverStatus.REDIRECT_LOOP,
                                    "The website created a redirect loop."
                                )
                            }
                            steps.add(
                                RedirectStep(
                                    url = nextUrl,
                                    statusCode = code,
                                    statusText = "Meta refresh",
                                    type = "META_REFRESH"
                                )
                            )
                            currentUrl = nextUrl
                            return@use
                        }
                    }

                    // If no meta-refresh, try JavaScript or bypass link extraction
                    val bypassUrl = tryExtractBypassUrl(bodySnippet, currentUrl)
                    if (bypassUrl != null) {
                        val normalized = UrlValidator.normalize(bypassUrl.first)
                        if (!visited.add(normalized)) {
                            return@withContext failure(
                                    startUrl, providerName, steps, startedAt,
                                    ResolverStatus.REDIRECT_LOOP,
                                    "The website created a redirect loop."
                            )
                        }
                        steps.add(
                            RedirectStep(
                                url = bypassUrl.first,
                                statusCode = code,
                                statusText = bypassUrl.second,
                                type = if (bypassUrl.second.contains("JS", ignoreCase = true)) "JS_REDIRECT" else "BYPASS"
                            )
                        )
                        currentUrl = bypassUrl.first
                        return@use
                    }

                    // No further redirect signal: this is the final destination.
                    return@withContext ResolveResult(
                        originalUrl = startUrl,
                        finalUrl = currentUrl,
                        provider = providerName,
                        status = ResolverStatus.SUCCESS,
                        redirects = steps,
                        elapsedMs = System.currentTimeMillis() - startedAt
                    )
                }

                // Any other status (4xx/5xx) is a hard failure for this chain.
                return@withContext failure(
                    startUrl, providerName, steps, startedAt,
                    ResolverStatus.HTTP_ERROR,
                    "The server responded with HTTP $code."
                )
            }
        }

        return@withContext failure(
            startUrl, providerName, steps, startedAt,
            ResolverStatus.REDIRECT_LIMIT,
            "Too many redirects. The destination could not be determined safely."
        )
    }

    private fun failure(
        original: String,
        provider: String,
        steps: List<RedirectStep>,
        startedAt: Long,
        status: ResolverStatus,
        message: String
    ) = ResolveResult(
        originalUrl = original,
        finalUrl = null,
        provider = provider,
        status = status,
        redirects = steps,
        elapsedMs = System.currentTimeMillis() - startedAt,
        errorMessage = message
    )

    private fun isProtectedPage(html: String): Boolean {
        if (html.isBlank()) return false
        val lower = html.lowercase(Locale.US)
        return PROTECTED_MARKERS.any { marker -> lower.contains(marker) }
    }

    /**
     * Heuristically searches an HTML body for the likely "next step" link
     * when a standard HTTP redirect isn't provided.
     */
    private fun tryExtractBypassUrl(html: String, baseUrl: String): Pair<String, String>? {
        if (html.isBlank()) return null

        // 1. Check for JavaScript redirects
        val jsMatch = JS_REDIRECT_REGEX.find(html)
        if (jsMatch != null) {
            val target = jsMatch.groupValues[1].trim()
            val nextUrl = resolveRelative(baseUrl, target)
            if (UrlValidator.isSyntacticallyValid(nextUrl)) {
                return nextUrl to "JS Redirect"
            }
        }

        // 2. Check for URLs in common query parameters of the current page itself
        // (sometimes the final destination is right there in the URL)
        for (param in BYPASS_PARAMS) {
            val value = UrlValidator.getQueryParameter(baseUrl, param)
            if (!value.isNullOrBlank()) {
                // Try direct or Base64 decoded
                val candidate = if (UrlValidator.isSyntacticallyValid(value)) {
                    value
                } else {
                    UrlValidator.tryDecodeBase64Url(value)
                }
                if (candidate != null && candidate != baseUrl) {
                    return candidate to "Extracted from param: $param"
                }
            }
        }

        // 3. Look for "Continue" or "Get Link" buttons in the body
        // We look for <a> tags containing certain text or matching param patterns
        val anchorRegex = Regex("""<a[^>]+href=["']([^"']+)["'][^>]*>(.*?)</a>""", RegexOption.IGNORE_CASE)
        val matches = anchorRegex.findAll(html)
        
        for (match in matches) {
            val href = match.groupValues[1].trim()
            val text = match.groupValues[2].lowercase(Locale.US)
            
            // Heuristic: If link text contains "continue", "get link", or "skip"
            // AND it's a valid absolute URL (or relative to base)
            if (text.contains("continue") || text.contains("get link") || text.contains("skip ad")) {
                val nextUrl = resolveRelative(baseUrl, href)
                if (UrlValidator.isSyntacticallyValid(nextUrl) && nextUrl != baseUrl) {
                    return nextUrl to "Bypass Link ($text)"
                }
            }
            
            // Heuristic: Look for links with destination params in their own href
            for (param in BYPASS_PARAMS) {
                if (href.contains("$param=", ignoreCase = true)) {
                    val nextUrl = resolveRelative(baseUrl, href)
                    if (UrlValidator.isSyntacticallyValid(nextUrl) && nextUrl != baseUrl) {
                        return nextUrl to "Bypass Link (param match)"
                    }
                }
            }
        }

        return null
    }

    private fun resolveRelative(base: String, target: String): String {
        return try {
            java.net.URI(base).resolve(target.trim()).toString()
        } catch (e: Exception) {
            target.trim()
        }
    }

    private fun httpStatusText(code: Int): String = when (code) {
        301 -> "Moved Permanently"
        302 -> "Found"
        303 -> "See Other"
        307 -> "Temporary Redirect"
        308 -> "Permanent Redirect"
        else -> "Redirect"
    }
}
