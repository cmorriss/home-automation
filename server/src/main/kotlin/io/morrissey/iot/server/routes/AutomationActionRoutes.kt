@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.AutomationActionPath
import io.morrissey.iot.server.AutomationActionsPath
import io.morrissey.iot.server.model.AutomationAction
import io.morrissey.iot.server.model.AutomationActionDto

class AutomationActionRoutes : EntityRoutes<AutomationActionDto, AutomationAction>(
    AuthorizedRoute,
    AutomationActionPath::class,
    AutomationActionsPath::class,
    AutomationAction,
    AutomationActionDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: AutomationActionDto) {
        call.respond(entityDto)
    }
}
