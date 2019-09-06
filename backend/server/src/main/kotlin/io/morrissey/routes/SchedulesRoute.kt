package io.morrissey.routes

import io.ktor.application.call
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.morrissey.SchedulePath
import io.morrissey.Schedules
import io.morrissey.model.*
import io.morrissey.persistence.HomeDao
import java.lang.IllegalArgumentException

@KtorExperimentalLocationsAPI
fun Route.schedules(db: HomeDao) {
    get<Schedules> {
        call.respond(db.schedules())
    }

    post<SchedulePath> { path ->
        log.debug("Received requested schedule id: ${path.id}")
        val schedule = call.receive<ScheduleDto>()
        log.info("Received update for schedule = $schedule")
        val storedSchedule = db.schedule(path.id) ?: throw IllegalArgumentException("The schedule for id ${path.id} could not be found.")
        storedSchedule.startTime = schedule.startTime
        storedSchedule.daysOn = schedule.daysOn
        storedSchedule.duration = schedule.duration
        db.updateSchedule(storedSchedule)
        call.respond(storedSchedule)
    }

    get<io.morrissey.ScheduleStatus> {
        call.respond(db.scheduleStatus())
    }

    post<io.morrissey.ScheduleStatus> {
        val status = call.receive<ScheduleStatusDto>()
        log.info("Received update for ScheduleStatus: $status")
        val storedStatus = db.scheduleStatus()
        storedStatus.pausedUntilDate = status.pausedUntilDate
        storedStatus.status = status.status
        db.updateScheduleStatus(storedStatus)
        call.respond(storedStatus)
    }
}
