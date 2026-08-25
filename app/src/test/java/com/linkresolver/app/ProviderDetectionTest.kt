package com.linkresolver.app

import android.net.Uri
import com.linkresolver.app.domain.resolver.AlphaLinksProvider
import com.linkresolver.app.domain.resolver.AroLinksProvider
import com.linkresolver.app.domain.resolver.CpmShortProvider
import com.linkresolver.app.domain.resolver.IndianShortnerProvider
import com.linkresolver.app.domain.resolver.LinkVipShortProvider
import com.linkresolver.app.domain.resolver.NowShortProvider
import com.linkresolver.app.domain.resolver.ProviderRegistry
import com.linkresolver.app.domain.resolver.TpiProvider
import com.linkresolver.app.domain.resolver.VipShortProvider
import com.linkresolver.app.domain.resolver.VpLinkProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProviderDetectionTest {

    @Test
    fun `each of the nine initial domains is detected by its dedicated provider`() {
        assertTrue(IndianShortnerProvider().supports(Uri.parse("https://indianshortner.in/A0epa")))
        assertTrue(VipShortProvider().supports(Uri.parse("https://vipshort.in/Cinema")))
        assertTrue(LinkVipShortProvider().supports(Uri.parse("https://link.vipshort.in/LivXow")))
        assertTrue(AroLinksProvider().supports(Uri.parse("https://arolinks.com/tA2Lv")))
        assertTrue(VpLinkProvider().supports(Uri.parse("https://vplink.in/CyVWUuOx")))
        assertTrue(TpiProvider().supports(Uri.parse("https://tpi.li/1mlFdwbf0l6m")))
        assertTrue(CpmShortProvider().supports(Uri.parse("https://cpmshort.com/wYoe")))
        assertTrue(NowShortProvider().supports(Uri.parse("https://nowshort.com/iSeS")))
        assertTrue(AlphaLinksProvider().supports(Uri.parse("https://alpha-links.in/o91u")))
    }

    @Test
    fun `vipshort and its link subdomain are treated as distinct providers`() {
        val vipShort = VipShortProvider()
        val linkVipShort = LinkVipShortProvider()

        assertTrue(vipShort.supports(Uri.parse("https://vipshort.in/x")))
        assertFalse(vipShort.supports(Uri.parse("https://link.vipshort.in/x")))

        assertTrue(linkVipShort.supports(Uri.parse("https://link.vipshort.in/x")))
        assertFalse(linkVipShort.supports(Uri.parse("https://vipshort.in/x")))
    }

    @Test
    fun `lookalike domains are not matched by substring`() {
        // A malicious host containing the provider name as a substring
        // must NOT be treated as that provider.
        assertFalse(VipShortProvider().supports(Uri.parse("https://vipshort.in.evil.com/x")))
        assertFalse(AroLinksProvider().supports(Uri.parse("https://notarolinks.com/x")))
    }

    @Test
    fun `unknown domain falls back to the generic resolver`() {
        val provider = ProviderRegistry.findProvider(Uri.parse("https://some-unregistered-shortener.example/abc"))
        assertEquals("Generic", provider.displayName)
    }
}
