package com.linkresolver.app.domain.resolver

import android.net.Uri
import com.linkresolver.app.domain.model.ResolveResult

/**
 * Fallback provider used for any domain that isn't specifically
 * registered, and reused internally by the named providers below.
 * Simply follows the standard HTTP redirect chain.
 */
class GenericResolverProvider : ResolverProvider {

    override val displayName: String = "Generic"

    override fun supports(uri: Uri): Boolean = true

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult {
        return context.genericResolver.resolve(uri.toString(), displayName, context.onProgress)
    }
}
