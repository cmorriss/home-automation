@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.ControlGroupPath
import io.morrissey.iot.server.ControlGroupsPath
import io.morrissey.iot.server.model.ControlGroup
import io.morrissey.iot.server.model.ControlGroupDto

class ControlGroupRoutes : EntityRoutes<ControlGroupDto, ControlGroup>(
    AuthorizedRoute, ControlGroupPath::class, ControlGroupsPath::class, ControlGroup, ControlGroupDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ControlGroupDto) {
        call.respond(entityDto)
    }
}
