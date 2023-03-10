package dev.notrobots.timeline.oauth.apis;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class TumblrApi20 extends DefaultApi20 {
    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.tumblr.com/v2/oauth2/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://www.tumblr.com/oauth2/authorize";
    }

    private static class InstanceHolder {
        private static final TumblrApi20 INSTANCE = new TumblrApi20();
    }

    public static TumblrApi20 instance() {
        return InstanceHolder.INSTANCE;
    }
}
