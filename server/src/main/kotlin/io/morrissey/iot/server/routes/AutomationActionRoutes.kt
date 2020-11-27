@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.AutomationActionPath
import io.morrissey.iot.server.AutomationActionsPath
import io.morrissey.iot.server.model.AutomationAction
import io.morrissey.iot.server.model.AutomationActionDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class AutomationActionRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<AutomationActionDto, AutomationAction>(
    route, AutomationActionPath::class, AutomationActionsPath::class, AutomationAction, AutomationActionDto::class
)
