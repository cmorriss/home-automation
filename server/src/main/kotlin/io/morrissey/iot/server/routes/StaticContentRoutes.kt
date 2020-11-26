package io.morrissey.iot.server.routes

import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.routing.Route
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.log
import io.morrissey.iot.server.modules.AuthorizedRoute
import java.io.File
import javax.inject.Inject

class StaticContentRoutes @Inject constructor(
    @AuthorizedRoute route: Route, serverConfig: HomeServerConfig
) {
    init {
        with(route) {
            static("/") {
                val staticContentDir = serverConfig.appContentDir
                log.info("Setting /app static content route to directory $staticContentDir")
                staticRootFolder = File(staticContentDir)
                default("index.html")
                files(".")
                files("assets")
                files("svg")
                files("assets/icon")
            }
        }
    }
}
