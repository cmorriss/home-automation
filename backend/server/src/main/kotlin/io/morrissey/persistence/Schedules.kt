package io.morrissey.persistence

import org.jetbrains.exposed.dao.IntIdTable

object Schedules : IntIdTable() {
    val daysOn = varchar("days-on", 256)
    val startTime = varchar("start-time", 10)
    val duration = integer("duration")
}