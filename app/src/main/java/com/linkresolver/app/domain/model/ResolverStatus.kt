package com.linkresolver.app.domain.model

enum class ResolverStatus {
    SUCCESS,
    INVALID_URL,
    NETWORK_ERROR,
    TIMEOUT,
    REDIRECT_LOOP,
    REDIRECT_LIMIT,
    UNSUPPORTED_PROVIDER,
    PROTECTED_REDIRECT,
    NO_DESTINATION,
    HTTP_ERROR
}
