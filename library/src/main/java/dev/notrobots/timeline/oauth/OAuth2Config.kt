package dev.notrobots.timeline.oauth

data class OAuth2Config(
    val consumerKey: String,
    val consumerSecret: String,
    val scope: String,
    val callbackUrl: String,
    val userAgent: String
)