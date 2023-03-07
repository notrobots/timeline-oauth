package dev.notrobots.timeline.oauth

import androidx.annotation.WorkerThread
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.builder.ServiceBuilderOAuth20
import com.github.scribejava.core.exceptions.OAuthException
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.AccessTokenRequestParams
import com.github.scribejava.core.oauth.AuthorizationUrlBuilder
import dev.notrobots.androidstuff.util.Logger
import dev.notrobots.timeline.oauth.Api20
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

abstract class OAuthHelper(
    var tokenId: String?,
    val tokenStore: TokenStore<OAuth2AccessToken>,
    val api: Api20
) {
    protected val logger = Logger(this)

    val authService = ServiceBuilder(api.clientId)
        .apiSecret(api.clientSecret)
        .callback(api.callback)
        .userAgent(api.userAgent)
        .apply(::onCreateService)
        .build(api)
    val authorizationUrl = authService.createAuthorizationUrlBuilder()
        .scope(api.scope)
        .state(api.state)
        .apply(::onCreateAuthorizationUrl)
        .build()

    protected open fun onCreateService(serviceBuilder: ServiceBuilderOAuth20) {}

    protected open fun onCreateAuthorizationUrl(authorizationUrlBuilder: AuthorizationUrlBuilder) {}

    protected open fun onCreateAccessTokenRequestParams(params: AccessTokenRequestParams) {}

    fun isFinalRequestUrl(url: String): Boolean {
        return url.startsWith(authService.callback)
    }

    fun getCode(url: String): String {
        val httpUrl = url.toHttpUrlOrNull()

        requireNotNull(httpUrl) {
            "Url malformed: $url"
        }

        val error = httpUrl.queryParameter("error")

        if (error != null) {
            throw OAuthException("Service responded with error: $error")
        }

        val state = httpUrl.queryParameter("state")
        val code = httpUrl.queryParameter("code")

        if (state == null) {
            throw IllegalArgumentException("Final redirect URL did not contain the 'state' query parameter")
        }

        if (state != api.state) {
            throw IllegalStateException("State did not match")
        }

        if (code == null) {
            throw IllegalArgumentException("Final redirect URL did not contain the 'code' query parameter")
        }

        return code
    }

    @WorkerThread
    fun requestAccessToken(code: String): OAuth2AccessToken? {
        val tokenRequestParams = AccessTokenRequestParams(code)

        onCreateAccessTokenRequestParams(tokenRequestParams)

        return authService.getAccessToken(tokenRequestParams)
    }

    @WorkerThread
    fun refreshAccessToken(refreshToken: String): OAuth2AccessToken? {
        return authService.refreshAccessToken(refreshToken)
    }

    @WorkerThread
    fun revokeAccessToken(accessToken: String) {
        authService.revokeToken(accessToken)
    }
}