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
import io.morrissey.iot.server.model.PhysicalControl
import io.morrissey.iot.server.model.PhysicalSensor
import io.morrissey.iot.server.routes.controls
import io.morrissey.iot.server.routes.log
import io.morrissey.iot.server.routes.sensors
import org.slf4j.event.Level
import java.io.File
import java.io.FileReader
import java.util.Properties

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

const val apiPath = "/api"
const val iotPath = "$apiPath/iot"
const val controlsPath = "$iotPath/controls"
const val controlPath = "$controlsPath/{id}"
const val sensorsPath = "$iotPath/sensors"
const val sensorPath = "$sensorsPath/{id}"
const val displaysPath = "$iotPath/displays"
const val displayPath = "$displaysPath/{id}"

@Location(controlsPath)
class Controls

@Location(controlPath)
data class ControlPath(val id: String)

@Location(sensorsPath)
class Sensors

@Location(sensorPath)
data class SensorPath(val id: String)

@Location(displaysPath)
class Displays

@Location(displayPath)
data class DisplayPath(val id: String)

const val etcDir = "/etc/home-automation-node"
val controlProperties = Properties().apply { load(FileReader("$etcDir/controls.properties")) }

val testing = File("$etcDir/testing").exists()

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(Locations) {}

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
        controls(knownControls)
        sensors(knownSensors)
    }
}

val gpio = if (!testing) {
    GpioFactory.getInstance()
} else {
    null
}

val knownControls = loadControls()
val knownSensors = loadSensors()

private fun loadControls(): MutableMap<String, PhysicalControl> {
    log.debug("Loading controls and setting GPIO pins...")
    return controlProperties.getProperty("controlPins").split(",").map { pinId ->
        val id = controlProperties.getProperty("$pinId.id")
        val gpioPin = RaspiPin.getPinByName("GPIO $pinId")
        log.debug("gpioPin for $pinId found as $gpioPin")
        val pinState = if (controlProperties.getProperty("$pinId.state") == "LOW") PinState.LOW else PinState.HIGH
        val pin = gpio?.provisionDigitalOutputPin(gpioPin, "Control $pinId", pinState)
        log.debug("pin $pinId created with name = ${pin?.name}, ")
        pin?.setShutdownOptions(true, pinState)

        PhysicalControl(id, pinId.toInt(), pin)
    }.associateBy { it.id }.toMutableMap()
}

private fun loadSensors(): MutableMap<String, PhysicalSensor> {
    log.debug("Loading sensors...")
    return controlProperties.getProperty("sensorPins").split(",").map { pinId ->
        val id = controlProperties.getProperty("$pinId.id")
        val hasValue = controlProperties.getProperty("$pinId.hasValue") == "true"
        val gpioPin = RaspiPin.getPinByName("GPIO $pinId")
        val pin = gpio?.provisionDigitalInputPin(gpioPin, "Sensor $pinId")
        PhysicalSensor(id, pinId.toInt(), pin, hasValue)
    }.associateBy { it.id }.toMutableMap()
}
