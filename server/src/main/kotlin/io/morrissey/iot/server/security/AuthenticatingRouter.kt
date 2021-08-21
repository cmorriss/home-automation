package io.morrissey.iot.server.security

import io.ktor.auth.*
import io.ktor.routing.*

class AuthenticatingRouter(private val route: Route) {
    val authenticatingRoute: Route
        get() {
            var _authenticatingRoute: Route? = null
            route.authenticate("google-oauth") {
                _authenticatingRoute = this
            }
            return _authenticatingRoute!!
        }
}
