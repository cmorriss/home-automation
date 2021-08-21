package io.morrissey.iot.server.routes

import io.ktor.http.content.*
import io.ktor.routing.*
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.log
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File

class StaticContentRoutes(serverConfig: HomeServerConfig) {
    private val route: Route = getKoin().get(AuthorizedRoute)

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
