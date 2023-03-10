package dev.notrobots.timeline.oauth

import com.github.scribejava.core.model.OAuth2AccessToken
import org.json.JSONObject

object OAuth2TokenJsonAdapter {
    fun toJson(token: OAuth2Token): JSONObject {
        return JSONObject().apply {
            put("access_token", token.accessToken)
            put("token_type", token.tokenType)
            put("expires_in", token.expiresIn.toString())
            put("refresh_token", token.refreshToken)
            put("scope", token.scope)
        }
    }

    fun toJsonString(token: OAuth2Token): String {
        return toJson(token).toString(0)
    }

    fun fromJson(token: String): OAuth2Token {
        val json = JSONObject(token)

        return OAuth2Token(
            json.optString("access_token"),
            json.optString("token_type"),
            json.optString("expires_in", "0").toInt(),
            json.optString("refresh_token"),
            json.optString("scope")
        )
    }
}