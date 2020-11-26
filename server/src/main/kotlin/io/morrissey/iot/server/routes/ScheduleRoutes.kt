@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.SchedulePath
import io.morrissey.iot.server.SchedulesPath
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.model.Schedule
import io.morrissey.iot.server.model.ScheduleDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class ScheduleRoutes @Inject constructor(
    @AuthorizedRoute route: Route, private val synchronizer: AutomationSynchronizer
) : EntityRoutes<ScheduleDto, Schedule>(
    route, SchedulePath::class, SchedulesPath::class, Schedule, ScheduleDto::class
) {
    override fun put(entityDto: ScheduleDto): ScheduleDto {
        val updatedDto = super.put(entityDto)
        synchronizer.synchronize(updatedDto.id)
        return updatedDto
    }

    override fun post(entityDto: ScheduleDto): ScheduleDto {
        val createdDto = super.post(entityDto)
        synchronizer.synchronize(createdDto.id)
        return createdDto
    }
}
