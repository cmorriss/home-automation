@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.ControlActionPath
import io.morrissey.iot.server.ControlActionsPath
import io.morrissey.iot.server.model.ControlAction
import io.morrissey.iot.server.model.ControlActionDto

class ControlActionRoutes : EntityRoutes<ControlActionDto, ControlAction>(
    AuthorizedRoute, ControlActionPath::class, ControlActionsPath::class, ControlAction, ControlActionDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ControlActionDto) {
        call.respond(entityDto)
    }
}
