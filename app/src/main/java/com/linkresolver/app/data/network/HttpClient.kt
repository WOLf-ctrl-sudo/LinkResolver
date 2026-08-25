package com.linkresolver.app.data.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Shared OkHttp client.
 *
 * IMPORTANT: followRedirects/followSslRedirects are disabled on purpose.
 * RedirectResolver walks the chain manually, one hop at a time, so every
 * intermediate URL can be captured and shown to the user. Certificate
 * validation is always left at OkHttp's default (system trust store) —
 * this app never installs a permissive TrustManager or HostnameVerifier.
 */
object HttpClient {

    const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/126.0.0.0 Mobile Safari/537.36"

    val instance: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(true)
            .build()
    }
}
