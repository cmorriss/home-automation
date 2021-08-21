@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.LoginFailed
import io.morrissey.iot.server.LoginPath
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.User
import io.morrissey.iot.server.model.Users
import io.morrissey.iot.server.security.HomeSiteSession
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.java.KoinJavaComponent.getKoin

class LoginRoute(serverConfig: HomeServerConfig) {
    private val route: Route = getKoin().get(AuthenticatedRoute)

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

                val json = HttpClient().get<String>("https://www.googleapis.com/userinfo/v2/me") {
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
