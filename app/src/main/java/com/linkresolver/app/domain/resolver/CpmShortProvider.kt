package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.domain.model.ResolveResult

class CpmShortProvider : ResolverProvider {
    override val displayName: String = "CPMShort"

    override fun supports(uri: Uri): Boolean = uri.matchesHost("cpmshort.com")

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult {
        return context.genericResolver.resolve(uri.toString(), displayName, context.onProgress)
    }
}
