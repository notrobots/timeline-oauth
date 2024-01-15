package dev.notrobots.timeline.oauth

import android.net.Uri
import android.util.Log
import androidx.annotation.WorkerThread
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.exceptions.OAuthException
import com.github.scribejava.core.oauth.OAuth20Service
import java.util.UUID

abstract class OAuth2Helper<T : Any>(
    private val authConfig: OAuth2Config,
    private val tokenStore: OAuth2TokenStore,
    api: DefaultApi20
) {
    val authService: OAuth20Service
    lateinit var client: T

    init {
        authService = ServiceBuilder(authConfig.consumerKey)
            .apiSecret(authConfig.consumerSecret)
            .userAgent(authConfig.userAgent)
            .callback(authConfig.callbackUrl)
            .defaultScope(authConfig.scope)
            .build(api)
    }

    fun getRandomUniqueID(): String {
        var id = UUID.randomUUID().toString()

        if (id in tokenStore.ids) {
            // The number of random version 4 UUIDs which need to be generated in
            // order to have a 50% probability of at least one collision is 2.71 quintillion.
            // Source: https://en.wikipedia.org/wiki/Universally_unique_identifier#Collisions.
            while (id in tokenStore.ids) {
                id = UUID.randomUUID().toString()
            }
        }

        return id
    }

    fun isFinalRequestUrl(url: String): Boolean {
        return url.startsWith(authService.callback)
    }

    abstract fun onCreateClient(token: OAuth2Token, tokenStore: OAuth2TokenStore, authConfig: OAuth2Config): T

    @WorkerThread
    fun onUserChallenge(url: String?, state: String): T {
        val httpUrl = Uri.parse(url) ?: throw IllegalArgumentException("Url malformed: \$url")
        val error = httpUrl.getQueryParameter("error")

        if (error != null) {
            throw OAuthException("Service responded with error: \$error")
        }

        val stateParam = httpUrl.getQueryParameter("state")
        val code = httpUrl.getQueryParameter("code")

        requireNotNull(stateParam) {
            "Final redirect URL did not contain the 'state' query parameter"
        }

        check(stateParam == state) {
            "State did not match"
        }

        requireNotNull(code) {
            "Final redirect URL did not contain the 'code' query parameter"
        }

        try {
            val token = OAuth2Token(authService.getAccessToken(code))

            client = onCreateClient(token, tokenStore, authConfig)

            return client
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun login(clientId: String): T {
        val current = tokenStore.fetch(clientId)

        if (current == null) {
            throw Exception("Client $clientId has no token stored")
        }

        client = onCreateClient(current, tokenStore, authConfig)

        return client
    }
}