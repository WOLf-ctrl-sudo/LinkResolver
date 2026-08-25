package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.domain.model.ResolveResult

class LinkVipShortProvider : ResolverProvider {
    override val displayName: String = "Link.VIPShort"

    override fun supports(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        return host == "link.vipshort.in"
    }

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult {
        return context.genericResolver.resolve(uri.toString(), displayName, context.onProgress)
    }
}
