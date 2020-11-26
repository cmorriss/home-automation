@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.locations.get
import io.ktor.locations.locations
import io.ktor.request.path
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.LoginFailed
import io.morrissey.iot.server.LoginPath
import io.morrissey.iot.server.log
import io.morrissey.iot.server.modules.AuthenticatedRoute
import io.morrissey.iot.server.model.User
import io.morrissey.iot.server.model.Users
import io.morrissey.iot.server.security.HomeSiteSession
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject
import io.ktor.client.request.get as clientGet

class LoginRoute @Inject constructor(
    @AuthenticatedRoute route: Route, serverConfig: HomeServerConfig
) {
    init {
        with(route) {
            get<LoginPath> {
                log.debug("Handling login request...")
                if (!serverConfig.authenticate) {
                    log.warn("Authentication disabled for this request.")
                    call.sessions.set(HomeSiteSession(serverConfig.unauthenticatedPrincipal))
                    return@get
                }

                val principal =
                        call.authentication.principal<OAuthAccessTokenResponse.OAuth2>() ?: error("No principal")

                val json = HttpClient().clientGet<String>("https://www.googleapis.com/userinfo/v2/me") {
                    header("Authorization", "Bearer ${principal.accessToken}")
                }

                val data = ObjectMapper().readValue<Map<String, Any?>>(json)
                val id = data["id"] as String?
                log.debug("Received data for principal: $data")

                if (id != null) {
                    val verifiedEmail = (data["verified_email"] as? Boolean) == true
                    val email = data["email"] as? String ?: ""
                    if (!verifiedEmail || transaction { User.find { Users.email eq email }.empty() }) {
                        if (transaction { User.find { Users.oauthId eq id }.empty() }) {
                            val firstName = data["given_name"] as? String ?: ""
                            val lastName = data["family_name"] as? String ?: ""
                            val picUrl = data["picture"] as? String ?: ""
                            if (verifiedEmail && emailIsAllowed(email, serverConfig)) {
                                transaction {
                                    User.new {
                                        oauthId = id
                                        this.email = email
                                        this.firstName = firstName
                                        this.lastName = lastName
                                        this.picUrl = picUrl
                                    }
                                }
                            } else {
                                val reason = if (!verifiedEmail) {
                                    "The email address on the Google account has not been verified."
                                } else {
                                    "The email address is not in the allowed list."
                                }
                                call.respondRedirect(
                                    locations.href(
                                        LoginFailed(
                                            reason
                                        )
                                    )
                                )
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
    }

    private fun emailIsAllowed(email: String, serverConfig: HomeServerConfig): Boolean {
        return serverConfig.authorizedEmails.split(",").any { it.equals(email, ignoreCase = true) }
    }
}
