package com.linkresolver.app.domain.resolver

import android.net.Uri

/**
 * Central registry of all known providers. To add support for a new
 * shortener domain, implement [ResolverProvider] and add an instance
 * here — no other file needs to change (UI, history, settings, and
 * navigation are all provider-agnostic).
 */
object ProviderRegistry {

    private val genericFallback = GenericResolverProvider()

    private val providers: List<ResolverProvider> = listOf(
        IndianShortnerProvider(),
        VipShortProvider(),
        LinkVipShortProvider(),
        AroLinksProvider(),
        VpLinkProvider(),
        TpiProvider(),
        CpmShortProvider(),
        NowShortProvider(),
        AlphaLinksProvider()
        // genericFallback is intentionally last / not listed here — see findProvider()
    )

    fun findProvider(uri: Uri): ResolverProvider {
        return providers.firstOrNull { it.supports(uri) } ?: genericFallback
    }

    /** Names shown on the Settings/About "supported providers" list. */
    fun supportedProviderNames(): List<String> =
        listOf(genericFallback.displayName) + providers.map { it.displayName }
}
