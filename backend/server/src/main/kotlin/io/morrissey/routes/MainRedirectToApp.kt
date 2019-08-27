package io.morrissey.routes

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get

@KtorExperimentalLocationsAPI
fun Route.mainRedirectToApp() {
    get("/") {
        call.respondRedirect("/app")
    }
}
