package dev.notrobots.timeline.oauth

import okhttp3.internal.toLongOrDefault
import org.json.JSONObject
import java.util.*

object OAuth2TokenJsonAdapter {
    fun toJson(token: OAuth2Token): JSONObject {
        return JSONObject().apply {
            put("access_token", token.accessToken)
            put("refresh_token", token.refreshToken)
            put("token_type", token.tokenType)
            put("expires_in", token.expiresIn.toString())
            put("scope", token.scope)
            put("expiration_date", token.expirationDate.time)
        }
    }

    fun toJsonString(token: OAuth2Token): String {
        return toJson(token).toString(0)
    }

    fun fromJson(token: String): OAuth2Token {
        val json = JSONObject(token)

        return OAuth2Token(
            json.optString("access_token"),
            json.optString("refresh_token") ,
            json.optString("expires_in", "0").toInt(),
            json.optString("token_type"),
            json.optString("scope"),
            Date(json.optString("expiration_date", "0").toLongOrDefault(0))
        )
    }
}