package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.ThresholdPath
import io.morrissey.iot.server.ThresholdsPath
import io.morrissey.iot.server.model.Threshold
import io.morrissey.iot.server.model.ThresholdDto

class ThresholdRoutes : EntityRoutes<ThresholdDto, Threshold>(
    AuthorizedRoute, ThresholdPath::class, ThresholdsPath::class, Threshold, ThresholdDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: ThresholdDto) {
        call.respond(entityDto)
    }
}
