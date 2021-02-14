@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.morrissey.iot.server.MetricDataPath
import io.morrissey.iot.server.aws.MetricDataRetriever
import io.morrissey.iot.server.aws.MetricRetrievalDuration
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.Metric
import io.morrissey.iot.server.modules.AuthorizedRoute
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

class MetricDataRoute @Inject constructor(
    @AuthorizedRoute route: Route,
    metricDataRetriever: MetricDataRetriever
) {
    init {
        with(route) {
            get<MetricDataPath> { metricData ->
                val duration = MetricRetrievalDuration.valueOf(metricData.duration)
                val result = transaction {
                    val metric = Metric[metricData.id]
                    metricDataRetriever.retrieve(metric, metricData.endTime, duration)
                }
                log.info("Responding to metric data request with $result")
                call.respond(result)
            }
        }
    }
}
