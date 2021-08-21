@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.AutomationGroupActionPath
import io.morrissey.iot.server.AutomationGroupActionsPath
import io.morrissey.iot.server.model.AutomationGroupAction
import io.morrissey.iot.server.model.AutomationGroupActionDto

class AutomationGroupActionRoutes : EntityRoutes<AutomationGroupActionDto, AutomationGroupAction>(
    AuthorizedRoute,
    AutomationGroupActionPath::class,
    AutomationGroupActionsPath::class,
    AutomationGroupAction,
    AutomationGroupActionDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: AutomationGroupActionDto) {
        call.respond(entityDto)
    }
}
