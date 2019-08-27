package io.morrissey.routes

import io.ktor.application.call
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.morrissey.SwitchPath
import io.morrissey.Switches
import io.morrissey.model.SwitchDto
import io.morrissey.model.PhysicalSwitch
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


val log = LoggerFactory.getLogger("main")

val failSafeTasks = mutableMapOf<Int, ShutOffFailSafe>()
val timer = Timer(true)

fun Route.switches(knownSwitches: MutableMap<Int, PhysicalSwitch>) {
    get<Switches> {
        call.respond(knownSwitches.values.map { SwitchDto(it.id, it.on) })
    }

    post<SwitchPath> { requestedSwitch ->
        log.debug("Received requested switch id: $requestedSwitch")
        val switch = call.receive<SwitchDto>()
        log.debug("Received update for switch = $switch")
        val physicalSwitch = knownSwitches[switch.id]
        if (physicalSwitch == null) {
            log.error("Unable to find switch with local id: ${switch.id}")
        } else {
            if (switch.on) {
                physicalSwitch.pin?.low()
                log.debug("Adding failsafe timer for switch $requestedSwitch.")
                val failSafe = ShutOffFailSafe(physicalSwitch)
                failSafeTasks[physicalSwitch.id] = failSafe
                timer.schedule(failSafe, Instant.now().plus(1L, ChronoUnit.HOURS).toEpochMilli())
            } else {
                physicalSwitch.pin?.high()
                failSafeTasks[physicalSwitch.id]?.let { failSafe ->
                    log.debug("Removing failsafe timer since switch $requestedSwitch is now shut off.")
                    failSafe.cancel()
                    failSafeTasks.remove(physicalSwitch.id)
                }
            }
            knownSwitches[physicalSwitch.id] = physicalSwitch.copy(on = switch.on)
        }
        call.respond(SwitchDto(switch.id, switch.on))
    }
}

class ShutOffFailSafe(private val physicalSwitch: PhysicalSwitch) : TimerTask() {
    override fun run() {
        log.warn("Executing failsafe shutoff for switch ${physicalSwitch.id}!")
        physicalSwitch.pin?.high()
        failSafeTasks.remove(physicalSwitch.id)
    }

}