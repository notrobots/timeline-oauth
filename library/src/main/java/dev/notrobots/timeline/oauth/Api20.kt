package dev.notrobots.timeline.oauth

import com.github.scribejava.core.builder.api.DefaultApi20

abstract class Api20(
    val clientId: String,
    val clientSecret: String?,
    val userAgent: String,
    val state: String,
    val scope: String,
    val callback: String?
) : DefaultApi20()