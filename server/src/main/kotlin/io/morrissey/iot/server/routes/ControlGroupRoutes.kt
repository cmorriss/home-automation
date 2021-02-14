@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.morrissey.iot.server.ControlGroupPath
import io.morrissey.iot.server.ControlGroupsPath
import io.morrissey.iot.server.model.ControlGroup
import io.morrissey.iot.server.model.ControlGroupDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class ControlGroupRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<ControlGroupDto, ControlGroup>(
    route, ControlGroupPath::class, ControlGroupsPath::class, ControlGroup, ControlGroupDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ControlGroupDto) {
        call.respond(entityDto)
    }
}
