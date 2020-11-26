@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.security

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.locations.href
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.LoginPath
import javax.inject.Inject

class AuthorizingRouter @Inject constructor(route: Route, config: HomeServerConfig) {
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