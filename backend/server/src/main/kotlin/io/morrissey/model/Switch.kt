package io.morrissey.model

import io.morrissey.model.LocationStatus.UNKNOWN
import org.joda.time.DateTime

data class Switch(
    val id: Int = NO_ID,
    val name: String,
    val type: SwitchType,
    val location: IotLocation,
    val locationId: Int,
    val locationStatus: LocationStatus = UNKNOWN,
    val locationStatusMessage: String = "",
    val on: Boolean = false,
    val lastUpdate: Long = DateTime.now().millis,
    val schedule: Schedule = Schedule()
)

enum class SwitchType(val text: String) { IRRIGATION_VALVE("Irrigation Valve"), LIGHT_SWITCH("Light Switch") }

enum class IotLocation(val apiUrl: String) {
    Backyard("http://backyard:8040/api"), Frontyard("http://frontyard:8040/api")
}

enum class LocationStatus {
    OK, ERROR, DISCONNECTED, UNKNOWN
}