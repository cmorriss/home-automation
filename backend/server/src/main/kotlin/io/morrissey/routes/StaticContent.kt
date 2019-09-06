package io.morrissey.routes

import io.ktor.http.content.default
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.routing.Route
import io.morrissey.*
import java.io.File

fun Route.staticContent(serverConfig: HomeServerConfig) {
    static("/app") {
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