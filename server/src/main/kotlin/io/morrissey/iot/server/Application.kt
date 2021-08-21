@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.request.*
import io.morrissey.iot.server.modules.awsModule
import io.morrissey.iot.server.modules.dbModule
import io.morrissey.iot.server.modules.mainModule
import kotlinx.serialization.json.Json
import org.koin.core.logger.Level
import org.koin.core.logger.PrintLogger
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.getKoin
import org.koin.logger.SLF4JLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val log: Logger = LoggerFactory.getLogger("main")

const val loginPath = "/login"
const val loginFailedPath = "/login-failed"
const val apiPath = "/api"
const val usersPath = "$apiPath/users"
const val iotPath = "$apiPath/iot"
const val controlsPath = "$iotPath/controls"
const val controlPath = "$controlsPath/{id}"
const val controlGroupsPath = "$iotPath/control-groups"
const val controlGroupPath = "$controlGroupsPath/{id}"
const val controlActionsPath = "$iotPath/control-actions"
const val controlActionPath = "$controlActionsPath/{id}"
const val automationActionsPath = "$iotPath/automation-actions"
const val automationActionPath = "$automationActionsPath/{id}"
const val thresholdsPath = "$iotPath/thresholds"
const val thresholdPath = "$thresholdsPath/{id}"
const val automationsPath = "$iotPath/automations"
const val automationPath = "$automationsPath/{id}"
const val automationGroupsPath = "$iotPath/automation-groups"
const val automationGroupPath = "$automationGroupsPath/{id}"
const val automationGroupActionsPath = "$iotPath/automation-group-actions"
const val automationGroupActionPath = "$automationGroupActionsPath/{id}"
const val metricsPath = "$iotPath/metrics"
const val metricPath = "$metricsPath/{id}"
const val metricDataPath = "$metricsPath/{id}/data"
const val eventsPath = "$iotPath/events"
const val eventPath = "$eventsPath/{id}"
const val actionsPath = "$iotPath/actions"
const val actionPath = "$actionsPath/{id}"


interface IdPath {
    val id: Int
}

@Location(controlsPath)
class ControlsPath

@Location(controlPath)
data class ControlPath(override val id: Int) : IdPath

@Location(controlGroupsPath)
class ControlGroupsPath

@Location(controlGroupPath)
data class ControlGroupPath(override val id: Int) : IdPath

@Location(controlActionsPath)
class ControlActionsPath

@Location(controlActionPath)
data class ControlActionPath(override val id: Int) : IdPath

@Location(automationActionsPath)
class AutomationActionsPath

@Location(automationActionPath)
data class AutomationActionPath(override val id: Int) : IdPath

@Location(automationsPath)
class AutomationsPath

@Location(automationPath)
class AutomationPath(override val id: Int) : IdPath

@Location(automationGroupsPath)
class AutomationGroupsPath

@Location(automationGroupPath)
class AutomationGroupPath(override val id: Int) : IdPath

@Location(automationGroupActionsPath)
class AutomationGroupActionsPath

@Location(automationGroupActionPath)
class AutomationGroupActionPath(override val id: Int) : IdPath

@Location(thresholdsPath)
class ThresholdsPath

@Location(thresholdPath)
class ThresholdPath(override val id: Int) : IdPath

@Location(metricsPath)
class MetricsPath

@Location(metricPath)
data class MetricPath(override val id: Int) : IdPath

@Location(metricDataPath)
class MetricDataPath(override val id: Int, val endTime: String = "latest", val duration: String = "THREE_HOURS") :
    IdPath

@Location(loginPath)
class LoginPath

@Location(loginFailedPath)
data class LoginFailed(val reason: String)

@Location(eventsPath)
class EventsPath

@Location(eventPath)
class EventPath(override val id: Int) : IdPath

@Location(actionsPath)
class ActionsPath

@Location(actionPath)
class ActionPath(override val id: Int) : IdPath

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
    coerceInputValues = true
}

fun Application.module(overrideModules: List<Module> = emptyList(), serverConfigOverride: HomeServerConfig? = null) {
    val homeServerConfig = serverConfigOverride ?: HomeServerConfig(environment.config)

    install(Koin) {
        printLogger(Level.DEBUG)
        val moduleList = listOf(mainModule(this@module, homeServerConfig), awsModule(homeServerConfig), dbModule())
        modules(
            moduleList.plus(overrideModules)
        )
    }

    getKoin().get<HomeServerApplicationStartup>()
}

fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val headerPort = request.header("Port")?.toInt() ?: request.port()
    val hostPort = request.host() + headerPort.let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    return "$protocol://$hostPort${application.locations.href(t)}"
}

