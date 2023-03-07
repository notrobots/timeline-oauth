package dev.notrobots.timeline.oauth

import com.github.scribejava.core.model.OAuth2AccessToken
import org.json.JSONObject
import org.junit.Test

internal class OAuth2AccessTokenJsonAdapterTest {
    private val token = OAuth2AccessToken(
        "@",
        "bearer",
        250,
        "@",
        "read",
        ""
    )
    private val tokenJson = """
        {
            "access_token": "@",
            "token_type": "bearer",
            "expires_in": "250",
            "refresh_token": "@",
            "scope": "read"
        }
    """

    @Test
    fun serializeToken() {
        assert(OAuth2AccessTokenJsonAdapter.toJsonString(token) == JSONObject(tokenJson).toString(0))
    }

    @Test
    fun deserializeToken() {
        assert(OAuth2AccessTokenJsonAdapter.fromJson(tokenJson) == token)
    }
}