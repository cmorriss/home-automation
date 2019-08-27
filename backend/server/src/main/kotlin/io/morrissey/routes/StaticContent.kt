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
        staticRootFolder = File("${serverConfig.appContentDir}/www")
        default("index.html")
        files(".")
        files("assets")
        files("svg")
        files("assets/icon")
    }
}