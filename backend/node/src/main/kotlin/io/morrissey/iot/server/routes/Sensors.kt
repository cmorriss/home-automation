package io.morrissey.iot.server.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.morrissey.SensorPath
import io.morrissey.Sensors
import io.morrissey.iot.server.model.PhysicalSensor

fun Route.sensors(knownSensors: MutableMap<String, PhysicalSensor>) {
    get<Sensors> {
        call.respond(knownSensors.values.map { it.toDto() })
    }

    get<SensorPath> { requestedSensor ->
        log.debug("Received get for sensor ${requestedSensor.id}")
        val physicalSensor = knownSensors[requestedSensor.id]
        if (physicalSensor == null) {
            log.error("Unable to find sensor with local id: ${requestedSensor.id}")
            call.respond(HttpStatusCode.NotFound)
        } else {
            call.respond(physicalSensor.toDto())
        }
    }
}
