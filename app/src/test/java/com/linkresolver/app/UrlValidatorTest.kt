package com.linkresolver.app

import com.linkresolver.app.data.network.UrlValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlValidatorTest {

    @Test
    fun `valid http and https urls are accepted`() {
        assertTrue(UrlValidator.isSyntacticallyValid("https://example.com"))
        assertTrue(UrlValidator.isSyntacticallyValid("http://example.com"))
    }

    @Test
    fun `invalid or dangerous schemes are rejected`() {
        assertFalse(UrlValidator.isSyntacticallyValid("invalid"))
        assertFalse(UrlValidator.isSyntacticallyValid("javascript:alert()"))
        assertFalse(UrlValidator.isSyntacticallyValid("file:///test"))
        assertFalse(UrlValidator.isSyntacticallyValid("content://com.example/data"))
        assertFalse(UrlValidator.isSyntacticallyValid("intent://example"))
        assertFalse(UrlValidator.isSyntacticallyValid("data:text/html;base64,abc"))
    }

    @Test
    fun `blank input is rejected`() {
        assertFalse(UrlValidator.isSyntacticallyValid(""))
        assertFalse(UrlValidator.isSyntacticallyValid("   "))
    }

    @Test
    fun `normalization is case and trailing slash insensitive`() {
        val a = UrlValidator.normalize("HTTP://Example.COM")
        val b = UrlValidator.normalize("https://example.com/")
        val c = UrlValidator.normalize("https://example.com")
        assertEquals("http://example.com", a)
        assertEquals("https://example.com", b)
        assertEquals("https://example.com", c)
    }

    @Test
    fun `normalization strips default ports`() {
        assertEquals("http://example.com/path", UrlValidator.normalize("http://example.com:80/path"))
        assertEquals("https://example.com/path", UrlValidator.normalize("https://example.com:443/path"))
    }

    @Test
    fun `sensitive query params are stripped when requested`() {
        val sanitized = UrlValidator.stripSensitiveParams(
            "https://example.com/final?token=abc123&foo=bar"
        )
        assertTrue(sanitized.contains("foo=bar"))
        assertFalse(sanitized.contains("token"))
    }

    @Test
    fun `decodes base64-encoded urls correctly`() {
        val target = "https://example.com/target"
        // Base64 for "https://example.com/target" is "aHR0cHM6Ly9leGFtcGxlLmNvbS90YXJnZXQ="
        val encoded = "aHR0cHM6Ly9leGFtcGxlLmNvbS90YXJnZXQ="
        
        assertEquals(target, UrlValidator.tryDecodeBase64Url(encoded))
    }

    @Test
    fun `returns null for invalid base64 or non-url content`() {
        assertEquals(null, UrlValidator.tryDecodeBase64Url("not-base64-at-all!"))
        // Base64 for "just some text"
        assertEquals(null, UrlValidator.tryDecodeBase64Url("anVzdCBzb21lIHRleHQ="))
    }
}
