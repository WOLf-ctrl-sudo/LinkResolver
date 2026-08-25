package com.linkresolver.app.domain.model

data class ResolveResult(
    val originalUrl: String,
    val finalUrl: String?,
    val provider: String,
    val status: ResolverStatus,
    val redirects: List<RedirectStep>,
    val elapsedMs: Long,
    val errorMessage: String? = null
) {
    val isSuccess: Boolean get() = status == ResolverStatus.SUCCESS && finalUrl != null
}
