package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.Route
import io.morrissey.iot.server.MetricPath
import io.morrissey.iot.server.MetricsPath
import io.morrissey.iot.server.model.Metric
import io.morrissey.iot.server.model.MetricDto
import io.morrissey.iot.server.modules.AuthorizedRoute
import javax.inject.Inject

class MetricRoutes @Inject constructor(
    @AuthorizedRoute route: Route
) : EntityRoutes<MetricDto, Metric>(
    route, MetricPath::class, MetricsPath::class, Metric, MetricDto::class
) {
    override suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: MetricDto) {
        call.respond(entityDto)
    }
}
