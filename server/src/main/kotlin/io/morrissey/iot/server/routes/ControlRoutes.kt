@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.routing.Route
import io.morrissey.iot.server.ControlPath
import io.morrissey.iot.server.ControlsPath
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.model.Control
import io.morrissey.iot.server.model.ControlDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

class ControlsRoutes @Inject constructor(
    @AuthorizedRoute route: Route, private val controller: Controller
) : EntityRoutes<ControlDto, Control>(
    route, ControlPath::class, ControlsPath::class, Control, ControlDto::class
) {
    override fun put(entityDto: ControlDto): ControlDto {
        val updatedDto = super.put(entityDto)
        controller.notifyUpdated(updatedDto.id)
        return updatedDto
    }

    override fun post(entityDto: ControlDto): ControlDto {
        val createdDto = super.post(entityDto)
        controller.notifyUpdated(createdDto.id)
        return createdDto
    }
}
