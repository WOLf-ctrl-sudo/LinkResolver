package com.linkresolver.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Mirrors the extraction logic used in ResolverViewModel.setInputFromShare
 * so it can be unit tested without an Android runtime.
 */
private fun extractFirstUrl(text: String): String? {
    val regex = Regex("""https?://[^\s]+""")
    return regex.find(text)?.value
}

class ShareIntentUrlExtractionTest {

    @Test
    fun `extracts a bare shared url`() {
        assertEquals(
            "https://vipshort.in/Cinema",
            extractFirstUrl("https://vipshort.in/Cinema")
        )
    }

    @Test
    fun `extracts a url embedded in surrounding text`() {
        assertEquals(
            "https://arolinks.com/tA2Lv",
            extractFirstUrl("Check this out: https://arolinks.com/tA2Lv it's great")
        )
    }

    @Test
    fun `returns null when no url is present`() {
        assertNull(extractFirstUrl("just some plain text with no link"))
    }
}
