@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.AutomationPath
import io.morrissey.iot.server.AutomationsPath
import io.morrissey.iot.server.model.Automation
import io.morrissey.iot.server.model.AutomationDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class AutomationRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<AutomationDto, Automation>(
    route, AutomationPath::class, AutomationsPath::class, Automation, AutomationDto::class
)
