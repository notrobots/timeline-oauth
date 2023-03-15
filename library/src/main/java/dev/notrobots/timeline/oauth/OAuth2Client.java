package dev.notrobots.timeline.oauth;

import android.net.Uri;

import androidx.annotation.WorkerThread;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;

public abstract class OAuth2Client {
    final private OAuth20Service authService;
    final private OAuth2TokenStore tokenStore;    //TODO: This should use a generic TokenStore
    final private String clientId;
    private OAuth2Token lastToken;

    protected OAuth2Client(OAuth20Service authService, OAuth2TokenStore tokenStore, String clientId) {
        if (tokenStore == null) {
            throw new RuntimeException("TokenStore cannot be null");
        }

        if (clientId == null || clientId.isEmpty()) {
            throw new RuntimeException("ClientId cannot be null or empty");
        }

        this.authService = authService;
        this.tokenStore = tokenStore;
        this.clientId = clientId;
        this.lastToken = tokenStore.fetch(clientId);
    }

    public boolean isFinalRequestUrl(String url) {
        return url.startsWith(getAuthService().getCallback());
    }

    public String getChallengeCode(String url, String state) {
        Uri httpUrl = Uri.parse(url);

        if (httpUrl == null) {
            throw new IllegalArgumentException("Url malformed: $url");
        }

        String error = httpUrl.getQueryParameter("error");

        if (error != null) {
            throw new OAuthException("Service responded with error: $error");
        }

        String stateParam = httpUrl.getQueryParameter("state");
        String code = httpUrl.getQueryParameter("code");

        if (stateParam == null) {
            throw new IllegalArgumentException("Final redirect URL did not contain the 'state' query parameter");
        }

        if (!stateParam.equals(state)) {
            throw new IllegalStateException("State did not match");
        }

        if (code == null) {
            throw new IllegalArgumentException("Final redirect URL did not contain the 'code' query parameter");
        }

        return code;
    }

    public void onUserChallenge(String url, String state) {
        String code = getChallengeCode(url, state);

        try {
            OAuth2Token token = new OAuth2Token(getAuthService().getAccessToken(code));

            storeNewAccessToken(token);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OAuth20Service getAuthService() {
        return authService;
    }

    public OAuth2TokenStore getTokenStore() {
        return tokenStore;
    }

    public String getClientId() {
        return clientId;
    }

    public OAuth2Token getLastToken() {
        return lastToken;
    }

    private void storeNewAccessToken(OAuth2Token token) {
        tokenStore.store(clientId, token);
        lastToken = token;
    }

    /**
     * Checks if the last access token is expired and refreshes it
     */
    @WorkerThread
    public void refreshAccessTokenIfNeeded() {
        if (lastToken.isExpired()) {
            try {
                refreshAccessToken();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @WorkerThread
    public void refreshAccessToken() {
        if (lastToken == null) {
            lastToken = tokenStore.fetch(clientId);
        }

        if (lastToken == null) {
            throw new RuntimeException("Client with id " + clientId + " is not authorized");
        }

        String refreshToken = lastToken.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RuntimeException("Refresh token cannot be null or empty");
        }

        try {
            OAuth2Token newToken = new OAuth2Token(authService.refreshAccessToken(lastToken.getRefreshToken()));
            storeNewAccessToken(newToken);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void revokeAccessToken() {
        if (authService.getApi().getRevokeTokenEndpoint() != null) {
            try {
                getAuthService().revokeToken(getLastToken().getAccessToken());
            } catch (Exception e) {
                throw new RuntimeException("Cannot revoke token", e);
            }

            getTokenStore().delete(getClientId());
        }
    }

    /**
     * Executes the given request and signs it with the most recent oauth2 access token.
     *
     * If the current token is expired it will be refreshed.
     *
     * @param request The request to send
     * @return Request response
     */
    @WorkerThread
    protected Response sendRequest(OAuthRequest request) {
        try {
            refreshAccessTokenIfNeeded();
            sign(request);
            return getAuthService().execute(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Signs the given request with the most recent access token
     * @param request Request to sign
     */
    protected void sign(OAuthRequest request) {
        if (lastToken == null) {
            throw new RuntimeException("Cannot sign request. Token is null");
        }

        request.addHeader("Authorization", "Bearer " + lastToken.getAccessToken());
    }
}
