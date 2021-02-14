package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.morrissey.iot.server.ThresholdPath
import io.morrissey.iot.server.ThresholdsPath
import io.morrissey.iot.server.model.Threshold
import io.morrissey.iot.server.model.ThresholdDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class ThresholdRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<ThresholdDto, Threshold>(
    route, ThresholdPath::class, ThresholdsPath::class, Threshold, ThresholdDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ThresholdDto) {
        call.respond(entityDto)
    }
}
