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
import io.morrissey.model.Schedule
import io.morrissey.model.ScheduleStatus
import io.morrissey.persistence.HomeDao

@KtorExperimentalLocationsAPI
fun Route.schedules(db: HomeDao) {
    get<Schedules> {
        call.respond(db.schedules())
    }

    post<SchedulePath> { requestedSchedule ->
        log.debug("Received requested schedule id: $requestedSchedule")
        val schedule = call.receive<Schedule>()
        log.info("Received update for schedule = $schedule")
        db.updateSchedule(schedule)
        call.respond(schedule)
    }

    get<io.morrissey.ScheduleStatus> {
        call.respond(db.scheduleStatus())
    }

    post<io.morrissey.ScheduleStatus> {
        val status = call.receive<ScheduleStatus>()
        log.info("Received update for ScheduleStatus: $status")
        db.updateScheduleStatus(status)
        call.respond(status)
    }
}
