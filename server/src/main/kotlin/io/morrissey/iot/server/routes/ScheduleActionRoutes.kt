@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.ScheduleActionPath
import io.morrissey.iot.server.ScheduleActionsPath
import io.morrissey.iot.server.model.ScheduleAction
import io.morrissey.iot.server.model.ScheduleActionDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class ScheduleActionRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<ScheduleActionDto, ScheduleAction>(
    route, ScheduleActionPath::class, ScheduleActionsPath::class, ScheduleAction, ScheduleActionDto::class
)
