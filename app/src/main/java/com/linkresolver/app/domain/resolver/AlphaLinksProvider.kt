package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.domain.model.ResolveResult

class AlphaLinksProvider : ResolverProvider {
    override val displayName: String = "AlphaLinks"

    override fun supports(uri: Uri): Boolean = uri.matchesHost("alpha-links.in")

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult {
        return context.genericResolver.resolve(uri.toString(), displayName, context.onProgress)
    }
}
