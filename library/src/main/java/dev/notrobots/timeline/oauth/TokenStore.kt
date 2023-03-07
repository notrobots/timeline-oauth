package dev.notrobots.timeline.oauth

/**
 * A TokenStore does exactly what you think it does: it stores tokens. More specifically, it stores refresh tokens and
 * OAuthData instances (which contains access tokens). A fully fleshed out TokenStore stores, fetches, and deletes
 * OAuthData instances and refresh tokens from an arbitrary data source. For an example, see [JsonFileTokenStore].
 *
 * In this class, "latest" ([fetch], [store], [delete] refers to the most recently acquired
 * [OAuthToken] instance. This OAuthData does not necessarily have to be unexpired.
 *
 * Code adapter from [mattbdean/JRAW](https://github.com/mattbdean/JRAW/blob/master/lib/src/main/kotlin/net/dean/jraw/oauth/TokenStore.kt)
 */
interface TokenStore<T> {
    /**
     * Stores the most recently acquired OAuthData instance. [OAuthToken.refreshToken] should be ignored. Consumers
     * will call [storeRefreshToken] directly when necessary.
     */
    fun store(id: String, token: T)

//    /**
//     * Stores a refresh token for a given user.
//     */
//    fun storeRefreshToken(id: String, token: String)

    /**
     * Attempts to fetch the most recently stored OAuthData instance, or null if there is none.
     */
    fun fetch(id: String): T?

//    /**
//     * Attempts to fetch the most recently stored refresh token, or null if there is none.
//     */
//    fun fetchRefreshToken(id: String): String?

    /**
     * Deletes the OAuthData tied to this user. Does nothing if there was no data to begin with.
     */
    fun delete(id: String)

//    /**
//     * Deletes the refresh token tied to this user. Does nothing if there was no refresh token to begin with.
//     */
//    fun deleteRefreshToken(id: String)
}