package dev.notrobots.timeline.oauth;

import android.net.Uri;

import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.oauth.OAuth20Service;

public abstract class OAuth2Client {
    final private OAuth20Service authService;
    final private OAuth2TokenStore tokenStore;    //TODO: This should use a generic TokenStore
    final private String clientId;
    private OAuth2Token lastToken;

    protected OAuth2Client(OAuth20Service authService, OAuth2TokenStore tokenStore, String clientId) {
        this.authService = authService;
        this.tokenStore = tokenStore;
        this.clientId = clientId;
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

            tokenStore.store(clientId, token);
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
        if (tokenStore == null) {
            throw new RuntimeException("Token store was not provided");
        }

        if (lastToken == null) {
            lastToken = tokenStore.fetch(clientId);
        }

        if (lastToken == null) {
            throw new RuntimeException("Client with id " + clientId + " is not authorized");
        }

        if (lastToken.isExpired()) {
            try {
                lastToken = new OAuth2Token(getAuthService().refreshAccessToken(lastToken.getRefreshToken()));
                tokenStore.store(clientId, lastToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return lastToken;
    }
}
