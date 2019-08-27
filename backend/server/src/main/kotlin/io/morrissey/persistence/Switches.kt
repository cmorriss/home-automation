package io.morrissey.persistence

import org.jetbrains.exposed.dao.IntIdTable

object Switches : IntIdTable() {
    val name = varchar("name", 256)
    val type = varchar("type", 64)
    val location = varchar("location", 64)
    val locationId = integer("location_id")
    val locationStatus = varchar("locationStatus", 20)
    val locationStatusMessage = varchar("locationStatusMessage", 256)
    val on = bool("on")
    val lastUpdate = long("last_update")
    val schedule = reference("schedule", Schedules)
}