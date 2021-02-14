@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.morrissey.iot.server.AutomationPath
import io.morrissey.iot.server.AutomationsPath
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.model.Automation
import io.morrissey.iot.server.model.AutomationDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class AutomationRoutes @Inject constructor(
    @AuthorizedRoute route: Route,
    private val synchronizer: AutomationSynchronizer
) : EntityRoutes<AutomationDto, Automation>(
    route, AutomationPath::class, AutomationsPath::class, Automation, AutomationDto::class
) {
    override fun put(entityDto: AutomationDto): AutomationDto {
        val updatedDto = super.put(entityDto)
        synchronizer.synchronize(updatedDto.id)
        return updatedDto
    }

    override fun post(entityDto: AutomationDto): AutomationDto {
        val createdDto = super.post(entityDto)
        synchronizer.synchronize(createdDto.id)
        return createdDto
    }

    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: AutomationDto) {
        call.respond(entityDto)
    }
}
