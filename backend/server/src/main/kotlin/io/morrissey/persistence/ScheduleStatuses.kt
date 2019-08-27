package io.morrissey.persistence

import org.jetbrains.exposed.dao.IntIdTable

object ScheduleStatuses : IntIdTable() {
    val status = varchar("status", 256)
    val pausedUntilDate = varchar("paused-until-date", 32)
}