package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.morrissey.iot.server.MetricPath
import io.morrissey.iot.server.MetricsPath
import io.morrissey.iot.server.model.Metric
import io.morrissey.iot.server.model.MetricDto

class MetricRoutes : EntityRoutes<MetricDto, Metric>(
    AuthorizedRoute, MetricPath::class, MetricsPath::class, Metric, MetricDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: MetricDto) {
        call.respond(entityDto)
    }
}
