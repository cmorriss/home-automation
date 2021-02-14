@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.morrissey.iot.server.ControlActionPath
import io.morrissey.iot.server.ControlActionsPath
import io.morrissey.iot.server.model.ControlAction
import io.morrissey.iot.server.model.ControlActionDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class ControlActionRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<ControlActionDto, ControlAction>(
    route, ControlActionPath::class, ControlActionsPath::class, ControlAction, ControlActionDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ControlActionDto) {
        call.respond(entityDto)
    }
}
