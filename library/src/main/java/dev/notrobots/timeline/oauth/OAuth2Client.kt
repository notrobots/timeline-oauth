package dev.notrobots.timeline.oauth

import androidx.annotation.WorkerThread
import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.oauth.OAuth20Service

abstract class OAuth2Client(
    val authService: OAuth20Service,
    val tokenStore: OAuth2TokenStore,
    val authConfig: OAuth2Config,
    val clientId: String
) {
    var currentToken: OAuth2Token? = null
        private set

    private fun storeNewAccessToken(token: OAuth2Token) {
        tokenStore.store(clientId, token)
        currentToken = token
    }

    /**
     * Executes the given request and signs it with the most recent oauth2 access token.
     *
     * If the current token is expired it will be refreshed.
     *
     * @param request The request to send
     * @return Request response
     */
    @WorkerThread
    fun sendRequest(request: OAuthRequest): Response {
        return try {
            refreshAccessTokenIfNeeded()
            sign(request)
            authService.execute(request)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Signs the given request with the most recent access token
     * @param request Request to sign
     */
    protected fun sign(request: OAuthRequest) {
        if (currentToken == null) {
            throw RuntimeException("Cannot sign request. Token is null")
        }
        request.addHeader("Authorization", "Bearer " + currentToken!!.accessToken)
    }

    /**
     * Checks if the last access token is expired and refreshes it
     */
    @WorkerThread
    fun refreshAccessTokenIfNeeded() {
        if (currentToken!!.isExpired()) {
            try {
                refreshAccessToken()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }

    @WorkerThread
    fun refreshAccessToken() {
        if (currentToken == null) {
            currentToken = tokenStore.fetch(clientId)
        }

        if (currentToken == null) {
            throw RuntimeException("Client with id $clientId is not authorized")
        }

        val refreshToken = currentToken!!.refreshToken

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw RuntimeException("Refresh token cannot be null or empty")
        }

        try {
            val newToken = OAuth2Token(authService.refreshAccessToken(currentToken!!.refreshToken))
            storeNewAccessToken(newToken)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @WorkerThread
    fun logout() {
        revokeAccessToken()
        tokenStore.delete(clientId)
    }

    @WorkerThread
    fun revokeAccessToken() {
        if (authService.api.revokeTokenEndpoint != null) {
            try {
                authService.revokeToken(currentToken!!.accessToken)
            } catch (e: Exception) {
                throw RuntimeException("Cannot revoke token", e)
            }
        }
    }
}