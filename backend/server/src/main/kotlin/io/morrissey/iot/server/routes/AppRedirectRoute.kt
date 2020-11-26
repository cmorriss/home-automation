@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.call
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class AppRedirectRoute @Inject constructor(@AuthorizedRoute route: Route) {
    init {
//        with(route) {
//            get("/") {
//                call.respondRedirect("/app")
//            }
//        }
    }
}
