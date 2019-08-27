@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.routes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.morrissey.SwitchPath
import io.morrissey.Switches
import io.morrissey.model.IotLocation
import io.morrissey.model.Switch
import io.morrissey.model.LocationStatus
import io.morrissey.model.PhysicalSwitch
import io.morrissey.persistence.HomeDao
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

val log: Logger = LoggerFactory.getLogger("main")

fun Route.switches(client: HttpClient, db: HomeDao) {
    get<Switches> {
        refreshSwitchState(client, db)
        call.respond(db.switches())
    }

    post<SwitchPath> {
        val aSwitch = call.receive<Switch>()
        log.info("Received update for switch = $aSwitch")
        try {
            updateSwitch(client, aSwitch)
            db.updateSwitch(aSwitch)
            call.respond(aSwitch)
        } catch (e: Exception) {
            log.error("An error occurred while updating the switch status.")
            refreshSwitchState(client, db)
            call.respond(db.switch(aSwitch.id)!!)
        }
    }

    db.switches().forEach {
        try {
            updateSwitch(client, it)
        } catch (e: Exception) {
            log.error("An error occurred when updating switch ${it.name}", e)
        }
    }
}

fun refreshSwitchState(client: HttpClient, db: HomeDao) {
    log.debug("refreshing switch state...")
    val switches = db.switches()

    val frontyardSwitches = getPhysicalSwitches(client, IotLocation.Frontyard)
    log.debug("found frontyard switches: $frontyardSwitches")
    val backyardSwitches = getPhysicalSwitches(client, IotLocation.Backyard)
    log.debug("found backyard switches: $backyardSwitches")
    updateLocalSwitches(IotLocation.Frontyard, switches, frontyardSwitches, db)
    updateLocalSwitches(IotLocation.Backyard, switches, backyardSwitches, db)
}

fun updateLocalSwitches(
    loc: IotLocation,
    storedSwitches: List<Switch>,
    physicalSwitchResults: PhysicalSwitchResult,
    db: HomeDao
) {
    when (physicalSwitchResults) {
        is PhysicalSwitchResult.SuccessResult -> physicalSwitchResults.physicalSwitches.forEach { physicalSwitch ->
            storedSwitches.filter { it.location == loc }.firstOrNull { it.locationId == physicalSwitch.id }?.let { storedSwitch ->
                if (physicalSwitch.on != storedSwitch.on) {
                    log.info("Updating switch ${storedSwitch.name} to new on value of ${physicalSwitch.on}")
                }
                db.updateSwitch(
                    storedSwitch.copy(
                        on = physicalSwitch.on,
                        locationStatus = LocationStatus.OK,
                        locationStatusMessage = ""
                    )
                )
            } ?: log.error("Could not locate switch in $loc with local id ${physicalSwitch.id}")
        }
        is PhysicalSwitchResult.FailureResult -> {
            storedSwitches.filter { it.location == physicalSwitchResults.location }
                .forEach { storedSwitch ->
                    log.warn("Setting switch ${storedSwitch.name} location status to ${physicalSwitchResults.locationStatus}.")
                    db.updateSwitch(
                        storedSwitch.copy(
                            locationStatus = physicalSwitchResults.locationStatus,
                            locationStatusMessage = physicalSwitchResults.statusMessage
                        )
                    )
                }
        }
    }
}

fun getPhysicalSwitches(client: HttpClient, loc: IotLocation): PhysicalSwitchResult {
    return try {
        runBlocking {
            PhysicalSwitchResult.SuccessResult(client.get("${loc.apiUrl}/iot/switches"))
        }
    } catch (e: Exception) {
        log.error("Exception while loading physical switches from ${loc.name}", e)
        return PhysicalSwitchResult.FailureResult(loc, e)
    }
}

fun updateSwitch(client: HttpClient, switch: Switch): PhysicalSwitchResult {
    return try {
        val stringResult = runBlocking {
            client.post<String> {
                url("${switch.location.apiUrl}/iot/switches/${switch.locationId}")
                contentType(ContentType.Application.Json)
                body = PhysicalSwitch(switch.locationId, switch.on)
            }
        }
        log.debug("received string result: $stringResult")
        PhysicalSwitchResult.SuccessResult(
            listOf(
                jacksonObjectMapper().readValue(
                    stringResult,
                    PhysicalSwitch::class.java
                )
            )
        )
    } catch (e: Exception) {
        log.error("Exception while loading physical switches from ${switch.location.name}", e)
        return PhysicalSwitchResult.FailureResult(switch.location, e)
    }
}

sealed class PhysicalSwitchResult {
    data class SuccessResult(val physicalSwitches: List<PhysicalSwitch>) : PhysicalSwitchResult()
    data class FailureResult(val location: IotLocation, val e: Exception) :
        PhysicalSwitchResult() {
        val locationStatus: LocationStatus
            get() = when (e) {
                is IOException -> LocationStatus.DISCONNECTED
                else -> LocationStatus.ERROR
            }
        val statusMessage: String
            get() = e.message ?: e.javaClass.name
    }
}