package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.data.network.RedirectResolver
import com.linkresolver.app.domain.model.ResolveResult

/**
 * Shared dependencies handed to every provider so individual provider
 * classes stay thin and don't each construct their own HTTP stack.
 */
class ResolverContext(
    val genericResolver: RedirectResolver,
    val maxRedirects: Int,
    val onProgress: (String) -> Unit = {}
)

/**
 * A provider knows how to recognize URLs for a given short-link domain
 * and resolve them to a final destination.
 *
 * Most providers in this app are thin wrappers that simply hand the
 * URL to the generic HTTP-redirect engine, because that's genuinely
 * how these services work: a normal 301/302 chain. A provider should
 * only diverge from that when the destination requires something a
 * plain HTTP client can't do safely/legitimately (e.g. a client-side
 * JS timer or human verification step) — in which case it should
 * return a [com.linkresolver.app.domain.model.ResolverStatus.PROTECTED_REDIRECT]
 * result rather than fabricate a destination.
 */
interface ResolverProvider {
    /** Human-readable name shown in the UI, e.g. "VIPShort". */
    val displayName: String

    fun supports(uri: Uri): Boolean

    suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult
}

/**
 * Hostname matching helper. Never use substring matching (e.g.
 * `host.contains("vipshort")`) since that would also match unrelated
 * or malicious lookalike domains such as "vipshort.evil.com".
 */
fun Uri.matchesHost(vararg hosts: String): Boolean {
    val host = this.host?.lowercase() ?: return false
    return hosts.any { candidate ->
        host == candidate || host.endsWith(".$candidate")
    }
}
