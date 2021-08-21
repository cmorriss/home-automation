@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.security

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.LoginPath

class AuthorizingRouter(route: Route, config: HomeServerConfig) {
    val authorizingRoute: Route

    init {
        authorizingRoute = route.createChild(object : RouteSelector(1.0) {
            override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
                RouteSelectorEvaluation.Constant
        })

        authorizingRoute.intercept(ApplicationCallPipeline.Features) {
            val session = call.sessions.get<HomeSiteSession>()
            if (session == null) {
                if (config.authenticate) {
                    call.respondRedirect(href(LoginPath()))
                    return@intercept finish()
                } else {
                    call.sessions.set(HomeSiteSession(config.unauthenticatedPrincipal))
                }
            }
            proceed()
        }
    }
}
