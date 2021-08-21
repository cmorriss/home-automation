@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.ControlPath
import io.morrissey.iot.server.ControlsPath
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.model.Control
import io.morrissey.iot.server.model.ControlDto

class ControlsRoutes(private val controller: Controller) : EntityRoutes<ControlDto, Control>(
    AuthorizedRoute, ControlPath::class, ControlsPath::class, Control, ControlDto::class
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

    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ControlDto) {
        call.respond(entityDto)
    }
}
