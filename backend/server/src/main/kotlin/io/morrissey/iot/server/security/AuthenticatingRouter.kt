package io.morrissey.iot.server.security

import io.ktor.auth.authenticate
import io.ktor.routing.Route
import javax.inject.Inject

class AuthenticatingRouter @Inject constructor(private val route: Route) {
    val authenticatingRoute: Route
        get() {
            var _authenticatingRoute: Route? = null
            route.authenticate("google-oauth") {
                _authenticatingRoute = this
            }
            return _authenticatingRoute!!
        }

}
