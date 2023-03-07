package dev.notrobots.timeline.oauth

import android.content.Context
import com.github.scribejava.core.model.OAuth2AccessToken

class OAuth2AccessTokenStore(context: Context) : SharedPreferencesTokenStore<OAuth2AccessToken>(context) {
    override fun serializeToken(token: OAuth2AccessToken): String {
        return OAuth2AccessTokenJsonAdapter.toJsonString(token)
    }

    override fun deserializeToken(token: String): OAuth2AccessToken {
        return OAuth2AccessTokenJsonAdapter.fromJson(token)
    }
}