package io.morrissey.security

import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.href
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.RouteSelector
import io.ktor.routing.RouteSelectorEvaluation
import io.ktor.routing.RoutingResolveContext
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import io.morrissey.HomeServerConfig
import io.morrissey.Login


data class HomeSiteSession(val id: String, val count: Int = 0)

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()

@KtorExperimentalLocationsAPI
fun Route.authorized(config: HomeServerConfig, callback: Route.() -> Unit): Route {
    val routeWithSessionIntercept = createChild(object : RouteSelector(1.0) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    })

    routeWithSessionIntercept.intercept(ApplicationCallPipeline.Features) {
        val session = call.sessions.get<HomeSiteSession>()
        if (session == null) {
            if (config.authenticate) {
                call.respondRedirect(href(Login()))
                return@intercept finish()
            } else {
                call.sessions.set(HomeSiteSession(config.unauthenticatedPrincipal))
            }
        }
        proceed()
    }

    callback(routeWithSessionIntercept)

    return routeWithSessionIntercept
}
