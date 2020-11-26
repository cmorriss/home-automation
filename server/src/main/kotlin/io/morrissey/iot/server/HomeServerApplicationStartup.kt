@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.oauth
import io.ktor.http.ContentType
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.cookie
import io.ktor.util.date.GMTDate
import io.ktor.util.hex
import io.morrissey.iot.server.layout.HomeIotLayout
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.security.AuthenticationException
import io.morrissey.iot.server.security.AuthorizationException
import io.morrissey.iot.server.security.HomeSiteSession
import javax.inject.Inject

class HomeServerApplicationStartup @Inject constructor(
    application: Application,
    homeServerConfig: HomeServerConfig,
    applicationDatabase: ApplicationDatabase,
    homeIotLayout: HomeIotLayout
) {
    init {
        log.info("@@@@@@@@@@@@@ Starting up application... @@@@@@@@@@@@@@@@@@@@@@@@")

        log.info("@@@@@@@@@@@@@ Configuring application... @@@@@@@@@@@@@@@@@@@@@@@@")
        configure(application, homeServerConfig)

        log.info("@@@@@@@@@@@@@ Initializing Database Tables... @@@@@@@@@@@@@@@@@@@")
        applicationDatabase.initialize()

        log.info("@@@@@@@@@@@@@ Populating IoT Definitions and State... @@@@@@@@@@@")
        homeIotLayout.populate()
    }

    private fun configure(application: Application, homeServerConfig: HomeServerConfig) {
        with(application) {
            install(io.ktor.locations.Locations)
            install(io.ktor.sessions.Sessions) {
                cookie<HomeSiteSession>("HOME_SITE_SESSION", io.ktor.sessions.SessionStorageMemory()) {
                    val secretSignKey = hex(homeServerConfig.sessionSignSecret)
                    transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
                }
            }
            install(io.ktor.features.CallLogging)
            install(io.ktor.features.CachingHeaders) {
                options { outgoingContent ->
                    when (outgoingContent.contentType?.withoutParameters()) {
                        ContentType.Text.CSS -> io.ktor.http.content.CachingOptions(
                            io.ktor.http.CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60), expires = null as? GMTDate?
                        )
                        else -> null
                    }
                }
            }
            install(io.ktor.features.DefaultHeaders) {
                header("X-Engine", "Ktor") // will send this header with each response
                header("Access-Control-Allow-Origin", "*")
                header("Access-Control-Allow-Methods", "*")
                header("Access-Control-Allow-Headers", "*")
            }
            install(io.ktor.auth.Authentication) {
                oauth("google-oauth") {
                    client = io.ktor.client.HttpClient()
                    providerLookup = {
                        io.morrissey.iot.server.security.googleOauthProvider(
                            homeServerConfig
                        )
                    }
                    urlProvider = { redirectUrl(io.morrissey.iot.server.LoginPath()) }
                }
            }

            install(io.ktor.features.ContentNegotiation) {
                jackson {
                    enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
                }
            }
            install(io.ktor.features.StatusPages) {
                exception<AuthenticationException> {
                    call.respond(io.ktor.http.HttpStatusCode.Unauthorized)
                }
                exception<AuthorizationException> {
                    call.respond(io.ktor.http.HttpStatusCode.Forbidden)
                }
            }
            install(io.ktor.metrics.micrometer.MicrometerMetrics) {
                registry = io.morrissey.iot.server.visibility.influxMeterRegistry()
                meterBinders = kotlin.collections.listOf(
                    io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics(),
                    io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics(),
                    io.micrometer.core.instrument.binder.jvm.JvmGcMetrics(),
                    io.micrometer.core.instrument.binder.system.ProcessorMetrics(),
                    io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics(),
                    io.micrometer.core.instrument.binder.system.FileDescriptorMetrics()
                )
            }
        }
    }
}
