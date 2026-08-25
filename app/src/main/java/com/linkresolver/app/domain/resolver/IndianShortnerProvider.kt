package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.domain.model.ResolveResult

class IndianShortnerProvider : ResolverProvider {
    override val displayName: String = "IndianShortner"

    override fun supports(uri: Uri): Boolean = uri.matchesHost("indianshortner.in")

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult {
        // This provider resolves through normal HTTP redirects, so it
        // delegates directly to the generic engine.
        return context.genericResolver.resolve(uri.toString(), displayName, context.onProgress)
    }
}
