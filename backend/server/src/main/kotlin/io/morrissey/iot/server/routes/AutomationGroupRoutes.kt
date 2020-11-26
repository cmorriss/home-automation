@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.AutomationGroupPath
import io.morrissey.iot.server.AutomationGroupsPath
import io.morrissey.iot.server.model.AutomationGroup
import io.morrissey.iot.server.model.AutomationGroupDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class AutomationGroupRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<AutomationGroupDto, AutomationGroup>(
    route, AutomationGroupPath::class, AutomationGroupsPath::class, AutomationGroup, AutomationGroupDto::class
)
