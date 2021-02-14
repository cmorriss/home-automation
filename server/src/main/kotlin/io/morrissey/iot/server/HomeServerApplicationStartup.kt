package io.morrissey.iot.server

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.metrics.micrometer.*
import io.ktor.response.*
import io.ktor.sessions.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.morrissey.iot.server.layout.HomeIotLayout
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.security.AuthenticationException
import io.morrissey.iot.server.security.AuthorizationException
import io.morrissey.iot.server.security.HomeSiteSession
import io.morrissey.iot.server.visibility.influxMeterRegistry
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
            install(Locations)
            install(Sessions) {
                cookie<HomeSiteSession>("HOME_SITE_SESSION", SessionStorageMemory()) {
                    val secretSignKey = hex(homeServerConfig.sessionSignSecret)
                    transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
                }
            }
            install(CallLogging)
            install(CachingHeaders) {
                options { outgoingContent ->
                    when (outgoingContent.contentType?.withoutParameters()) {
                        ContentType.Text.CSS -> CachingOptions(
                            CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60), expires = null as? GMTDate?
                        )
                        else -> null
                    }
                }
            }
            install(DefaultHeaders) {
                header("X-Engine", "Ktor") // will send this header with each response
                header("Access-Control-Allow-Origin", "*")
                header("Access-Control-Allow-Methods", "*")
                header("Access-Control-Allow-Headers", "*")
            }
            install(Authentication) {
                oauth("google-oauth") {
                    client = io.ktor.client.HttpClient()
                    providerLookup = {
                        io.morrissey.iot.server.security.googleOauthProvider(
                            homeServerConfig
                        )
                    }
                    urlProvider = { redirectUrl(LoginPath()) }
                }
            }

            install(ContentNegotiation) {
                jackson {
                    enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
                }
            }
            install(StatusPages) {
                exception<AuthenticationException> {
                    call.respond(Unauthorized)
                }
                exception<AuthorizationException> {
                    call.respond(Forbidden)
                }
            }
            install(MicrometerMetrics) {
                registry = influxMeterRegistry()
                meterBinders = listOf(
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
