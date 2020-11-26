package io.morrissey.iot.server.aws

import io.morrissey.iot.server.model.MetricData
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest
import software.amazon.awssdk.services.cloudwatch.model.Metric
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery
import software.amazon.awssdk.services.cloudwatch.model.MetricStat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class MetricDataRetriever @Inject constructor(private val cloudWatchClient: CloudWatchClient) {
    fun retrieve(metricId: Int, endTime: String, duration: MetricRetrievalDuration): MetricData {
        return transaction {
            val metric = io.morrissey.iot.server.model.Metric[metricId]
            val zonedEndTime = if (endTime == "latest") {
                ZonedDateTime.now(GMT_TIME_ZONE)
            } else {
                LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE).atZone(GMT_TIME_ZONE)
            }
            val cleanEndTime = zonedEndTime.minus((zonedEndTime.minute % 5).toLong(), ChronoUnit.MINUTES).withSecond(0)
            val startTime = cleanEndTime.minusSeconds(duration.seconds)
            val retrievedData = cloudWatchClient.getMetricData(
                GetMetricDataRequest.builder()
                    .startTime(startTime.toInstant())
                    .endTime(cleanEndTime.toInstant())
                    .metricDataQueries(
                        MetricDataQuery.builder()
                            .id("id1")
                            .metricStat(
                                MetricStat.builder()
                                    .metric(
                                        Metric.builder()
                                            .namespace(metric.externalNamespace)
                                            .metricName(metric.externalName)
                                            .build()
                                    )
                                    .period(metric.period)
                                    .stat(metric.statistic.cwName)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            val values = retrievedData.metricDataResults().map { result ->
                result.values()
            }.single()

            MetricData(
                metric = metric.toDto(),
                values = values,
                startTime = startTime.withZoneSameInstant(LOCAL_TIME_ZONE).format(DateTimeFormatter.ISO_DATE),
                endTime = cleanEndTime.withZoneSameInstant(LOCAL_TIME_ZONE).format(DateTimeFormatter.ISO_DATE)
            )
        }
    }
}

private const val MINUTE = 60L
private const val HOUR = 60L * MINUTE
private const val DAY = 24L * HOUR
private const val WEEK = 7L * DAY

enum class MetricRetrievalDuration(val seconds: Long) {
    ONE_HOUR(HOUR),
    THREE_HOURS(3L * HOUR),
    TWELVE_HOURS(12L * HOUR),
    ONE_DAY(DAY),
    TWO_DAYS(2L * DAY),
    ONE_WEEK(WEEK),
    TWO_WEEKS(2L * WEEK)
}
