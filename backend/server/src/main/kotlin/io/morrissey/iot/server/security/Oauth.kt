package io.morrissey.iot.server.security

import io.ktor.auth.OAuthServerSettings
import io.ktor.http.HttpMethod
import io.morrissey.iot.server.HomeServerConfig

fun googleOauthProvider(serverConfig: HomeServerConfig): OAuthServerSettings {
    return OAuthServerSettings.OAuth2ServerSettings(
        name = "google",
        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
        accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",
        requestMethod = HttpMethod.Post,

        clientId = serverConfig.clientId,
        clientSecret = serverConfig.clientSecret,
        defaultScopes = listOf("profile", "email")
    )
}
