package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.domain.model.ResolveResult

class VipShortProvider : ResolverProvider {
    override val displayName: String = "VIPShort"

    // Deliberately does NOT match the "link." subdomain — that's a
    // distinct provider handled by LinkVipShortProvider.
    override fun supports(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        return host == "vipshort.in" || host == "www.vipshort.in"
    }

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult {
        return context.genericResolver.resolve(uri.toString(), displayName, context.onProgress)
    }
}
