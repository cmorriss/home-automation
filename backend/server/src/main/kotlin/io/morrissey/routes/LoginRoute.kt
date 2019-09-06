package io.morrissey.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.locations
import io.ktor.request.path
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.morrissey.HomeServerConfig
import io.morrissey.Login
import io.morrissey.LoginFailed
import io.morrissey.model.*
import io.morrissey.persistence.HomeDao
import io.morrissey.security.HomeSiteSession
import io.ktor.client.request.get as clientGet

@KtorExperimentalLocationsAPI
fun Route.login(db: HomeDao, serverConfig: HomeServerConfig) {
    get<Login> {
        log.debug("Handling login request...")
        if (!serverConfig.authenticate) {
            log.warn("Authentication disabled for this request.")
            call.sessions.set(HomeSiteSession(serverConfig.unauthenticatedPrincipal))
            return@get
        }

        val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
            ?: error("No principal")

        val json = HttpClient().clientGet<String>("https://www.googleapis.com/userinfo/v2/me") {
            header("Authorization", "Bearer ${principal.accessToken}")
        }

        val data = ObjectMapper().readValue<Map<String, Any?>>(json)
        val id = data["id"] as String?
        log.debug("Received data for principal: $data")

        if (id != null) {
            val verifiedEmail = (data["verified_email"] as? Boolean) == true
            val email = data["email"] as? String ?: ""
            if (!verifiedEmail || db.userByEmail(email) == null) {
                if (db.userByOauthId(id) == null) {
                    val firstName = data["given_name"] as? String ?: ""
                    val lastName = data["family_name"] as? String ?: ""
                    val picUrl = data["picture"] as? String ?: ""
                    if (verifiedEmail && emailIsAllowed(email, serverConfig)) {
                        db.createUser(UserEntity().apply {
                            oauthId = id
                            this.email = email
                            this.firstName = firstName
                            this.lastName = lastName
                            this.picUrl = picUrl
                        })
                    } else {
                        val reason = if (!verifiedEmail) {
                            "The email address on the Google account has not been verified."
                        } else {
                            "The email address is not in the allowed list."
                        }
                        call.respondRedirect(locations.href(LoginFailed(reason)))
                    }
                }
            }

            call.sessions.set(HomeSiteSession(id))
        }
        val redirectPath = when (call.request.path()) {
            "/login" -> "/"
            else -> call.request.path()
        }
        log.debug("Setting redirect for authentication to $redirectPath")
        call.respondRedirect(redirectPath)
    }
}

private fun emailIsAllowed(email: String, serverConfig: HomeServerConfig): Boolean {
    return serverConfig.authorizedEmails.split(",").any { it.equals(email, ignoreCase = true) }
}
