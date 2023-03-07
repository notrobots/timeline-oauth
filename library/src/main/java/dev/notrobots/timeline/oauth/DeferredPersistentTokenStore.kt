package dev.notrobots.timeline.oauth

import dev.notrobots.androidstuff.util.requireNotEmpty

/**
 * This specific TokenStore abstraction is a way of dealing with the assumption that all `store*` and `fetch*`
 * operations happen in insignificant time.
 *
 * All data is saved in memory until manually told to persist to a data source. This data source may be a file,
 * database, or a cloud storage provider. If the time it takes to persist the data is insignificant, you can enable
 * [autoPersist].
 *
 * When first created, it might be necessary to load the persisted data into memory using [load]. Otherwise the
 * TokenStore could be missing out on some data.
 */
abstract class DeferredPersistentTokenStore<T> @JvmOverloads constructor(
    initialData: Map<String, T> = mapOf()
) : TokenStore<T> {
    private var memoryData: MutableMap<String, T> = initialData.toMutableMap()
    private var lastPersistedData: Map<String, T>? = null

    /**
     * If true, [persist] will automatically be called after the in-memory data is mutated.
     */
    var autoPersist: Boolean = false

    /**
     * Fetches any data stored in memory about the given username. Useful for debugging.
     */
    fun inspect(id: String): T? = memoryData[id]

    /**
     * A list of all usernames that have either a refresh token or some OAuthData associated with it.
     */
    val ids: List<String>
        get() = memoryData.keys.toList()

    /**
     * Returns true if the in-memory copy of the data differs from the last persisted data
     */
    fun hasUnsaved() = lastPersistedData != memoryData

    /**
     * Persists the in-memory data to somewhere more permanent. Assume this is a blocking operation. Returns this
     * instance for chaining.
     */
    fun persist() {
        doPersist(memoryData)
        this.lastPersistedData = HashMap(memoryData)
    }

    /**
     * Loads the data from its persistent source. Overwrites any existing data in memory. Assume this is a blocking
     * operation. Returns this instance for chaining.
     */
    fun load() {
        this.memoryData = doLoad().toMutableMap()
    }

    /**
     * Clears all data currently being held in memory. Does not affect data already persisted. To clear both in-memory
     * data and store data, perform a [clear] follow by a [persist].
     */
    fun clear() {
        this.memoryData.clear()
    }

    /**
     * Returns the amount of entries currently being stored in memory.
     */
    fun size() = this.memoryData.size

    /**
     * Does the actual work for persisting data. The given data may contain null values depending on if the user asked
     * to keep insignificant values.
     */
    protected abstract fun doPersist(data: Map<String, T>)

    /**
     * Does the actual loading of the persisted data. Any insignificant entries returned will automatically be filtered
     * out, so unless manually doing this here saves a significant amount of time, you should just load all stored data.
     */
    protected abstract fun doLoad(): Map<String, T>

    /**
     * Returns a copy of the data currently in memory.
     */
    fun data(): Map<String, T> = HashMap(this.memoryData)

    final override fun store(id: String, data: T) {
        requireNotEmpty(id) {
            "Refusing to store data for unknown username"
        }

        this.memoryData[id] = data

        if (autoPersist && hasUnsaved()) {
            persist()
        }
    }

//    final override fun storeRefreshToken(id: String, token: String) {
//        requireNotEmpty(id) {
//            "Refusing to store data for unknown username"
//        }
//
//        this.memoryData[id]?.refreshToken = token
//
//        if (autoPersist && this.hasUnsaved()) {
//            persist()
//        }
//    }

    final override fun fetch(id: String): T? {
        return memoryData[id]
    }

//    final override fun fetchRefreshToken(id: String): String? {
//        return memoryData[id]?.refreshToken
//    }

    override fun delete(id: String) {
        if (id !in memoryData) {
            return
        }

        memoryData.remove(id)

        if (autoPersist && this.hasUnsaved()) {
            persist()
        }
    }

//    override fun deleteRefreshToken(id: String) {
//        if (id !in memoryData) {
//            return
//        }
//
//        memoryData.remove(id)
//
//        if (autoPersist && this.hasUnsaved())
//            persist()
//    }
}