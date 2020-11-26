@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.AutomationGroupActionPath
import io.morrissey.iot.server.AutomationGroupActionsPath
import io.morrissey.iot.server.model.AutomationGroupAction
import io.morrissey.iot.server.model.AutomationGroupActionDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class AutomationGroupActionRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<AutomationGroupActionDto, AutomationGroupAction>(
    route, AutomationGroupActionPath::class, AutomationGroupActionsPath::class, AutomationGroupAction, AutomationGroupActionDto::class
)