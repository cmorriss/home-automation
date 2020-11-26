@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server

import com.google.inject.Guice
import com.google.inject.Injector
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.locations
import io.ktor.request.header
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.util.AttributeKey
import io.morrissey.iot.server.modules.CallModule
import io.morrissey.iot.server.modules.MainModule
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
const val schedulesPath = "$iotPath/schedules"
const val schedulePath = "$schedulesPath/{id}"
const val scheduleActionsPath = "$iotPath/schedule-actions"
const val scheduleActionPath = "$scheduleActionsPath/{id}"
const val sensorThresholdsPath = "$iotPath/thresholds"
const val sensorThresholdPath = "$sensorThresholdsPath/{id}"
const val automationsPath = "$iotPath/automations"
const val automationPath = "$automationsPath/{id}"
const val automationGroupsPath = "$iotPath/automation-groups"
const val automationGroupPath = "$automationGroupsPath/{id}"
const val automationGroupActionsPath = "$iotPath/automation-group-actions"
const val automationGroupActionPath = "$automationGroupActionsPath/{id}"
const val metricsPath = "$iotPath/metrics"
const val metricPath = "$metricsPath/{id}"
const val metricDataPath = "$metricsPath/{id}/data"

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

@Location(schedulesPath)
class SchedulesPath

@Location(schedulePath)
data class SchedulePath(override val id: Int) : IdPath

@Location(scheduleActionsPath)
class ScheduleActionsPath

@Location(scheduleActionPath)
data class ScheduleActionPath(override val id: Int) : IdPath

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

@Location(metricsPath)
class MetricsPath

@Location(metricPath)
data class MetricPath(override val id: Int) : IdPath

@Location(metricDataPath)
class MetricDataPath(override val id: Int, val endTime: String = "latest", val duration: String = "THREE_HOURS") : IdPath

@Location(loginPath)
class LoginPath

@Location(loginFailedPath)
data class LoginFailed(val reason: String)


// attribute key for storing injector in a call
val InjectorKey = AttributeKey<Injector>("injector")

// accessor for injector from a call
val ApplicationCall.injector: Injector get() = attributes[InjectorKey]

fun Application.module() {
    val homeServerConfig = HomeServerConfig(environment.config)
    // Create main injector
    val injector = Guice.createInjector(
        MainModule(
            this, homeServerConfig
        )
    )

    // Intercept application call and put child injector into attributes
    intercept(ApplicationCallPipeline.Features) {
        call.attributes.put(
            InjectorKey, injector.createChildInjector(
                CallModule(
                    call
                )
            )
        )
    }
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

