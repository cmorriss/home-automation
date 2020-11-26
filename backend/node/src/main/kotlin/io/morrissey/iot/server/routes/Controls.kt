@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.morrissey.ControlPath
import io.morrissey.Controls
import io.morrissey.iot.server.model.ControlDto
import io.morrissey.iot.server.model.ControlState
import io.morrissey.iot.server.model.PhysicalControl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Timer
import java.util.TimerTask
import kotlin.collections.set

val log: Logger = LoggerFactory.getLogger("main")

val failSafeTasks = mutableMapOf<String, ShutOffFailSafe>()
val timer = Timer(true)

fun Route.controls(knownControls: MutableMap<String, PhysicalControl>) {
    get<Controls> {
        call.respond(knownControls.values.map {
            ControlDto(
                it.id, it.state
            )
        })
    }

    get<ControlPath> { requestedControl ->
        log.debug("Received get for control ${requestedControl.id}")
        val physicalControl = knownControls[requestedControl.id]
        if (physicalControl == null) {
            log.error("Unable to find control with local id: ${requestedControl.id}")
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(
                ControlDto(
                    physicalControl.id, physicalControl.state
                )
            )
        }
    }

    post<ControlPath> { requestedControl ->
        log.debug("Received requested control given id: ${requestedControl.id}")
        val control = call.receive<ControlDto>()
        log.debug("Received update for control = $control")
        val physicalControl = knownControls[requestedControl.id]
        if (physicalControl == null) {
            log.error("Unable to find control with local id: ${requestedControl.id}")
            call.respond(HttpStatusCode.NotFound)
        } else {
            when (control.state) {
                ControlState.ON -> {
                    physicalControl.gpioPin?.low()
                    log.debug("Adding failsafe timer for control ${requestedControl.id}.")
                    val failSafe = ShutOffFailSafe(physicalControl)
                    failSafeTasks[physicalControl.id] = failSafe
                    timer.schedule(failSafe, Instant.now().plus(1L, ChronoUnit.HOURS).toEpochMilli())
                }
                ControlState.OFF -> {
                    physicalControl.gpioPin?.high()
                    failSafeTasks[physicalControl.id]?.let { failSafe ->
                        log.debug("Removing failsafe timer since control $requestedControl is now shut off.")
                        failSafe.cancel()
                        failSafeTasks.remove(physicalControl.id)
                    }
                }
            }

            knownControls[physicalControl.id] = physicalControl.copy(state = control.state)
            call.respond(control)
        }
    }
}

class ShutOffFailSafe(private val physicalControl: PhysicalControl) : TimerTask() {
    override fun run() {
        log.warn("Executing failsafe shutoff for control ${physicalControl.id}!")
        physicalControl.gpioPin?.high()
        failSafeTasks.remove(physicalControl.id)
    }
}
