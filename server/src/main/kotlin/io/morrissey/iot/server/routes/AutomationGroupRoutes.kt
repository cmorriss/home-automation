@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.AutomationGroupPath
import io.morrissey.iot.server.AutomationGroupsPath
import io.morrissey.iot.server.model.AutomationGroup
import io.morrissey.iot.server.model.AutomationGroupDto

class AutomationGroupRoutes : EntityRoutes<AutomationGroupDto, AutomationGroup>(
    AuthorizedRoute, AutomationGroupPath::class, AutomationGroupsPath::class, AutomationGroup, AutomationGroupDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: AutomationGroupDto) {
        call.respond(entityDto)
    }
}
