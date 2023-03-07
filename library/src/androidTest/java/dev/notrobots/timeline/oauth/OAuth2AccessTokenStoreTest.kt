package dev.notrobots.timeline.oauth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.scribejava.core.model.OAuth2AccessToken
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OAuth2AccessTokenStoreTest {
    @Test
    fun Store_And_Fetch() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val tokenStore = OAuth2AccessTokenStore(appContext)
        val token = OAuth2AccessToken(
            "@",
            "bearer",
            250,
            "@",
            "read",
            ""
        )
        val id = "test_token"

        tokenStore.store(id, token)

        val fetchedToken = tokenStore.fetch(id)

        assert(fetchedToken == token)
        Assertions.assertEquals("dev.notrobots.timeline.oauth.test", appContext.packageName)
    }
}