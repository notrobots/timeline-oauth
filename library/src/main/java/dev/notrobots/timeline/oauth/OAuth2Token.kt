package dev.notrobots.timeline.oauth

import com.github.scribejava.core.model.OAuth2AccessToken
import java.util.*
import java.util.concurrent.TimeUnit

class OAuth2Token(
    accessToken: String,
    refreshToken: String,
    expiresIn: Int,
    tokenType: String,
    scope: String
) : OAuth2AccessToken(accessToken, refreshToken, expiresIn, tokenType, scope, "") {  //TODO: Add the actual raw response
    val expirationDate = Date(Date().time + TimeUnit.SECONDS.toMillis(expiresIn.toLong()))

    constructor(token: OAuth2AccessToken) : this(
        token.accessToken,
        token.refreshToken,
        token.expiresIn,
        token.tokenType,
        token.scope
    )

    fun isExpired(): Boolean {
        return expirationDate.before(Date())
    }
}