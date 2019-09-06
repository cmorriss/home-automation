@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.Logging
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.jackson.jackson
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.locations.locations
import io.ktor.metrics.micrometer.MicrometerMetrics
import io.ktor.request.header
import io.ktor.request.host
import io.ktor.request.path
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.util.date.GMTDate
import io.ktor.util.hex
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.morrissey.persistence.HomeDao
import io.morrissey.persistence.HomeH2DB
import io.morrissey.routes.*
import io.morrissey.schedule.Scheduler
import io.morrissey.security.*
import io.morrissey.visibility.EmailFactory
import io.morrissey.visibility.ErrorReporter
import io.morrissey.visibility.influxMeterRegistry
import java.time.Clock
import java.util.*


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

const val loginPath = "/login"
const val loginFailedPath = "/login-failed"
const val apiPath = "/api"
const val usersPath = "$apiPath/users"
const val iotPath = "$apiPath/iot"
const val switchesPath = "$iotPath/switches"
const val switchPath = "$switchesPath/{id}"
const val schedulesPath = "$iotPath/schedules"
const val schedulePath = "$schedulesPath/{id}"
const val scheduleStatusPath = "$iotPath/schedule-status"


@Location(switchesPath)
class Switches

@Location(switchPath)
data class SwitchPath(val id: Int)

@Location(schedulesPath)
class Schedules

@Location(schedulePath)
data class SchedulePath(val id: Int)

@Location(scheduleStatusPath)
class ScheduleStatus

@Location(usersPath)
class Users

@Location(loginPath)
class Login

@Location(loginFailedPath)
data class LoginFailed(val reason: String)

@Suppress("unused") // Referenced in application.conf
fun Application.injectedModule(db: HomeDao, httpClient: HttpClient, homeServerConfig: HomeServerConfig) {
    log.info("@@@@@@@@@@@@@@@@@@@@@ Starting up application @@@@@@@@@@@@@@@@@@@@@@")
    install(Locations)
    install(Sessions) {
        cookie<HomeSiteSession>("HOME_SITE_SESSION", SessionStorageMemory()) {
            val secretSignKey = hex(homeServerConfig.sessionSignSecret)
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }
    install(CallLogging) {
        level = homeServerConfig.logLevel
        filter { call -> call.request.path().startsWith("/") }
    }
    install(CachingHeaders) {
        options { outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(
                    CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60),
                    expires = null as? GMTDate?
                )
                else -> null
            }
        }
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    install(Authentication) {
        oauth("google-oauth") {
            client = HttpClient()
            providerLookup = { googleOauthProvider(homeServerConfig) }
            urlProvider = { redirectUrl(Login()) }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        anyHost()
    }
    install(StatusPages) {
        exception<AuthenticationException> {
            call.respond(HttpStatusCode.Unauthorized)
        }
        exception<AuthorizationException> {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
    install(MicrometerMetrics) {
        registry = influxMeterRegistry()
        meterBinders = listOf(
            ClassLoaderMetrics(),
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            FileDescriptorMetrics()
        )
    }

    routing {
        authenticate("google-oauth") {
            login(db, homeServerConfig)
        }

        authorized(homeServerConfig) {
            switches(httpClient, db)
            schedules(db)
            mainRedirectToApp()
            staticContent(homeServerConfig)
        }
    }
}

fun Application.module() {
    val homeServerConfig = HomeServerConfig(environment.config)
    val client = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = homeServerConfig.requestLogLevel
        }
    }
    val timer = Timer(true)
    val clock = Clock.systemDefaultZone()
    val db = HomeH2DB(homeServerConfig.dbUrl)
    Scheduler(db, timer, client, clock)
    ErrorReporter(Timer(), EmailFactory(), client, homeServerConfig)
    injectedModule(db, client, homeServerConfig)
}

private fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val headerPort = request.header("Port")?.toInt() ?: request.port()
    val hostPort = request.host() + headerPort.let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://$hostPort${application.locations.href(t)}"
}

