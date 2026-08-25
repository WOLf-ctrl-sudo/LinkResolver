package com.linkresolver.app

import com.linkresolver.app.data.network.HttpClient
import com.linkresolver.app.data.network.RedirectResolver
import com.linkresolver.app.domain.model.ResolverStatus
import kotlinx.coroutines.test.runTest
import okhttp3.Dns
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetAddress

class RedirectResolverTest {

    private lateinit var server: MockWebServer
    private lateinit var resolver: RedirectResolver

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val testClient = HttpClient.instance.newBuilder()
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> =
                    listOf(InetAddress.getByName("127.0.0.1"))
            })
            .addInterceptor { chain ->
                val request = chain.request()
                if (request.url.host != "127.0.0.1" && request.url.host != "localhost") {
                    val newUrl = request.url.newBuilder()
                        .scheme("http")
                        .host("127.0.0.1")
                        .port(server.port)
                        .build()
                    chain.proceed(request.newBuilder().url(newUrl).build())
                } else {
                    chain.proceed(request)
                }
            }
            .build()

        resolver = RedirectResolver(maxRedirects = 20, client = testClient)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `follows a single 302 redirect to a final 200 page`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(302).setHeader("Location", server.url("/final").toString())
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html>done</html>"))

        val result = resolver.resolve(server.url("/start").toString(), "TestProvider")

        assertEquals(ResolverStatus.SUCCESS, result.status)
        assertTrue(result.finalUrl!!.endsWith("/final"))
        // ORIGIN + one redirect step
        assertEquals(2, result.redirects.size)
    }

    @Test
    fun `follows a chain of mixed redirect codes`() = runTest {
        server.enqueue(MockResponse().setResponseCode(301).setHeader("Location", server.url("/step2").toString()))
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", server.url("/step3").toString()))
        server.enqueue(MockResponse().setResponseCode(307).setHeader("Location", server.url("/final").toString()))
        server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

        val result = resolver.resolve(server.url("/start").toString(), "TestProvider")

        assertEquals(ResolverStatus.SUCCESS, result.status)
        assertTrue(result.finalUrl!!.endsWith("/final"))
        assertEquals(4, result.redirects.size) // origin + 3 hops
    }

    @Test
    fun `detects a redirect loop`() = runTest {
        val urlA = server.url("/a").toString()
        val urlB = server.url("/b").toString()

        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", urlB))
        server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", urlA))

        val result = resolver.resolve(urlA, "TestProvider")

        assertEquals(ResolverStatus.REDIRECT_LOOP, result.status)
    }

    @Test
    fun `stops after exceeding the max redirect limit`() = runTest {
        val smallResolver = RedirectResolver(maxRedirects = 3)
        repeat(6) { i ->
            server.enqueue(
                MockResponse().setResponseCode(302).setHeader("Location", server.url("/step$i").toString())
            )
        }

        val result = smallResolver.resolve(server.url("/start").toString(), "TestProvider")

        assertEquals(ResolverStatus.REDIRECT_LIMIT, result.status)
    }

    @Test
    fun `reports an http error for 4xx and 5xx responses`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = resolver.resolve(server.url("/missing").toString(), "TestProvider")

        assertEquals(ResolverStatus.HTTP_ERROR, result.status)
    }

    @Test
    fun `rejects an invalid url before making any request`() = runTest {
        val result = resolver.resolve("not-a-url", "TestProvider")
        assertEquals(ResolverStatus.INVALID_URL, result.status)
    }

    @Test
    fun `detects a protected verification page instead of faking success`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                "<html><body>Checking your browser before accessing the site. " +
                    "<div class=\"g-recaptcha\"></div></body></html>"
            )
        )

        val result = resolver.resolve(server.url("/gate").toString(), "TestProvider")

        assertEquals(ResolverStatus.PROTECTED_REDIRECT, result.status)
        assertEquals(null, result.finalUrl)
    }

    @Test
    fun `follows meta-refresh without url= prefix`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                "<html><meta http-equiv=\"refresh\" content=\"0; https://example.com/target\"></html>"
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

        val result = resolver.resolve(server.url("/meta").toString(), "TestProvider")

        assertEquals(ResolverStatus.SUCCESS, result.status)
        assertEquals("https://example.com/target", result.finalUrl)
    }

    @Test
    fun `follows javascript location replace redirect`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                "<script>window.location.replace('https://example.com/js-target');</script>"
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

        val result = resolver.resolve(server.url("/js").toString(), "TestProvider")

        println("Result status: ${result.status}")
        println("Error message: ${result.errorMessage}")

        assertEquals(ResolverStatus.SUCCESS, result.status)
        assertEquals("https://example.com/js-target", result.finalUrl)
        assertEquals("JS_REDIRECT", result.redirects.last().type)
    }

    @Test
    fun `extracts bypass url from query parameter`() = runTest {
        // Here the target is in the URL itself
        val target = "https://destination.com/final"
        val startUrl = server.url("/bypass?url=$target").toString()
        
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html>no redirect here</html>"))
        server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

        val result = resolver.resolve(startUrl, "TestProvider")

        assertEquals(ResolverStatus.SUCCESS, result.status)
        assertEquals(target, result.finalUrl)
        assertEquals("BYPASS", result.redirects.last().type)
    }

    @Test
    fun `extracts bypass link from html body text`() = runTest {
        val target = "https://example.com/final-destination"
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                "<html><body><a href=\"$target\">Click here to Continue</a></body></html>"
            )
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

        val result = resolver.resolve(server.url("/link").toString(), "TestProvider")

        assertEquals(ResolverStatus.SUCCESS, result.status)
        assertEquals(target, result.finalUrl)
        assertTrue(result.redirects.last().statusText!!.contains("Continue"))
    }

    @Test
    fun `returns NO_REDIRECT_FOUND when no redirects or bypasses are detected`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("<html>Just a plain page</html>"))

        val url = server.url("/plain").toString()
        val result = resolver.resolve(url, "TestProvider")

        assertEquals(ResolverStatus.NO_REDIRECT_FOUND, result.status)
        assertEquals(null, result.finalUrl)
        assertTrue(result.errorMessage!!.contains("No further redirect was found"))
    }

    @Test
    fun `extracts bypass link with new anchor text heuristics`() = runTest {
        val targets = listOf(
            "https://example.com/click" to "Click here",
            "https://example.com/download" to "Download",
            "https://example.com/proceed" to "Proceed",
            "https://example.com/goto" to "Go to link"
        )

        for ((target, text) in targets) {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    "<html><body><a href=\"$target\">$text</a></body></html>"
                )
            )
            server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

            val result = resolver.resolve(server.url("/link").toString(), "TestProvider")

            assertEquals(ResolverStatus.SUCCESS, result.status)
            assertEquals(target, result.finalUrl)
        }
    }

    @Test
    fun `extracts bypass url from new query parameters`() = runTest {
        val params = listOf("r", "redirect", "to", "out")
        val target = "https://destination.com/final"

        for (param in params) {
            val startUrl = server.url("/bypass?$param=$target").toString()
            server.enqueue(MockResponse().setResponseCode(200).setBody("<html>no redirect here</html>"))
            server.enqueue(MockResponse().setResponseCode(200).setBody("done"))

            val result = resolver.resolve(startUrl, "TestProvider")

            assertEquals(ResolverStatus.SUCCESS, result.status)
            assertEquals(target, result.finalUrl)
        }
    }

    @Test
    fun `detects protected page with new markers`() = runTest {
        val markers = listOf("countdown", "please wait", "unlocking in")

        for (marker in markers) {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    "<html><body>Wait for the $marker...</body></html>"
                )
            )

            val result = resolver.resolve(server.url("/gate").toString(), "TestProvider")

            assertEquals(ResolverStatus.PROTECTED_REDIRECT, result.status)
        }
    }
}
