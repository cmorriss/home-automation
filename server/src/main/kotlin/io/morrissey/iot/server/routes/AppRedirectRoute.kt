@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.*
import org.koin.java.KoinJavaComponent.getKoin

class AppRedirectRoute {
    private val route: Route = getKoin().get(AuthorizedRoute)

    init { //        with(route) {
        //            get("/") {
        //                call.respondRedirect("/app")
        //            }
        //        }
    }
}
