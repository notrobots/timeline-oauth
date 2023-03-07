package dev.notrobots.timeline.oauth

import android.content.Context

/**
 * This TokenStore implementation persists OAuthData and refresh tokens to a private
 * SharedPreferences.
 *
 * SharedPreferences are persisted using `apply()` rather than `commit()`, so it might be a good
 * idea to enable [autoPersist].
 *
 * It should be noted that this is probably not the most efficient or secure means of storing this
 * kind of data. For apps that have several hundred users, it might be better to store this
 * information in a database.
 */
abstract class SharedPreferencesTokenStore<T>(context: Context) : DeferredPersistentTokenStore<T>() {
    val sharedPreferences = context.getSharedPreferences(
        "token_store",  //XXX: This name is too generic and might cause issues with other libraries
        Context.MODE_PRIVATE
    )

    protected abstract fun serializeToken(token: T): String

    protected abstract fun deserializeToken(token: String): T

    override fun doLoad(): Map<String, T> {
        return sharedPreferences
            .all
            // Only operate on key-value pairs whose value is a string (since we store all data
            // as strings)
            .filter { it.value is String }
            // Parse the JSON value to a PersistedAuthData
            .map { it.key to deserializeToken(it.value as String) }
            .toMap()
    }

    override fun doPersist(data: Map<String, T>) {
        val editor = sharedPreferences.edit().clear()

        for ((username, persistedData) in data) {
            editor.putString(username, serializeToken(persistedData))
        }

        editor.apply()
    }
}