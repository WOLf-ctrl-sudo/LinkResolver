package com.linkresolver.app.ui.home

import com.linkresolver.app.domain.model.ResolveResult

sealed interface ResolverUiState {

    data object Idle : ResolverUiState

    data class Loading(val message: String) : ResolverUiState

    data class Success(val result: ResolveResult) : ResolverUiState

    data class Error(val result: ResolveResult) : ResolverUiState
}

/** Progress messages shown while a resolve is in flight. */
object ResolveProgressMessages {
    const val CHECKING_URL = "Checking URL..."
    const val DETECTING_PROVIDER = "Detecting provider..."
    const val FOLLOWING_REDIRECTS = "Following redirects..."
    const val FINDING_DESTINATION = "Finding final destination..."
}
