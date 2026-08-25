package com.linkresolver.app.domain.model

/**
 * A single hop in a redirect chain.
 *
 * [type] is one of "HTTP_HEADER" (Location header), "META_REFRESH"
 * (HTML meta-refresh tag), "JS_REDIRECT" (JavaScript location change),
 * "BYPASS" (extracted from page body), or "ORIGIN" (the very first URL
 * entered by the user).
 */
data class RedirectStep(
    val url: String,
    val statusCode: Int?,
    val statusText: String?,
    val type: String
)
