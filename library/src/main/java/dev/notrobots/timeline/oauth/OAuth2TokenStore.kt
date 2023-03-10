package dev.notrobots.timeline.oauth

import android.content.Context

class OAuth2TokenStore(context: Context) : SharedPreferencesTokenStore<OAuth2Token>(context) {
    override fun serializeToken(token: OAuth2Token): String {
        return OAuth2TokenJsonAdapter.toJsonString(token)
    }

    override fun deserializeToken(token: String): OAuth2Token {
        return OAuth2TokenJsonAdapter.fromJson(token)
    }
}