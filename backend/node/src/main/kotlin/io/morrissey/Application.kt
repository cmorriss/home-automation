@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey

import com.fasterxml.jackson.databind.SerializationFeature
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.locations.Location
import io.ktor.locations.Locations
import io.ktor.request.path
import io.ktor.routing.routing
import io.morrissey.model.PhysicalSwitch
import io.morrissey.routes.switches
import io.morrissey.routes.log
import org.slf4j.event.Level
import java.io.File
import java.io.FileReader
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

const val apiPath = "/api"
const val iotPath = "$apiPath/iot"
const val switchesPath = "$iotPath/switches"
const val switchPath = "$switchesPath/{id}"

@Location(switchesPath)
class Switches

@Location(switchPath)
data class SwitchPath(val id: Int)

const val etcDir = "/etc/home-automation-node"
val switchProperties = Properties().apply { load(FileReader("$etcDir/switches.properties")) }

val testing = File("$etcDir/testing").exists()

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(Locations) {
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        switches(knownSwitches)
    }
}

val gpio = if (!testing) {
    GpioFactory.getInstance()
} else {
    null
}

val knownSwitches = loadSwitches()

private fun loadSwitches(): MutableMap<Int, PhysicalSwitch> {
    log.debug("Loading switches and setting GPIO pins...")
    return switchProperties.getProperty("switchIds").split(",").map {
        val gpioPin = RaspiPin.getPinByName("GPIO $it")
        log.debug("gpioPin for $it found as $gpioPin")
        val pin = gpio?.provisionDigitalOutputPin(gpioPin, "Switch $it", PinState.HIGH)
        log.debug("pin $it created with name = ${pin?.name}, ")
        pin?.setShutdownOptions(true, PinState.HIGH)

        PhysicalSwitch(it.toInt(), pin)
    }.associateBy { it.id }.toMutableMap()

}
