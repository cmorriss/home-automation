package io.morrissey.iot.server.aws

import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.MetricData
import io.morrissey.iot.server.model.MetricDimension
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class MetricDataRetriever @Inject constructor(private val cloudWatchClient: CloudWatchClient) {
    companion object {
        const val DATE_TIME_FORMAT = "E h:mm a"
    }

    fun retrieve(
        metric: io.morrissey.iot.server.model.Metric,
        endTime: String,
        duration: MetricRetrievalDuration
    ): MetricData {
        log.info("Retrieving metric data for duration of $duration ending at $endTime.")
        log.info("Using metric: ${metric.externalName}, period ${metric.period}, statistic ${metric.statistic.cwName}, namespace ${metric.externalNamespace}, dimensions ${metric.dimensions}")
        val zonedEndTime = if (endTime == "latest") {
            ZonedDateTime.now(GMT_TIME_ZONE)
        } else {
            LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE).atZone(GMT_TIME_ZONE)
        }
        val cleanEndTime = zonedEndTime.minus((zonedEndTime.minute % 5).toLong(), ChronoUnit.MINUTES).withSecond(0)
        val startTime = cleanEndTime.minusSeconds(duration.seconds)
        log.info("Metric data start time is ${startTime.toInstant()}, end time is ${cleanEndTime.toInstant()}")
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
                                        .dimensions(metric.dimensions.toCloudWatchDimensions())
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
        val values = retrievedData.metricDataResults()[0].values().toMutableList()
        val timestamps = retrievedData.metricDataResults()[0].timestamps()
            .map { it.atZone(LOCAL_TIME_ZONE) }
            .map { it.format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)) }
            .toMutableList()

        // Need to reverse them as cloudwatch sends the data back backwards.
        values.reverse()
        timestamps.reverse()
        log.info("Retrieved values of $values")

        return MetricData(
            metric = metric.toDto(),
            values = values,
            timestamps = timestamps,
            startTime = startTime.withZoneSameInstant(LOCAL_TIME_ZONE).format(DateTimeFormatter.ISO_DATE),
            endTime = cleanEndTime.withZoneSameInstant(LOCAL_TIME_ZONE).format(DateTimeFormatter.ISO_DATE)
        )
    }

    private fun List<MetricDimension>.toCloudWatchDimensions(): List<Dimension> {
        return map {
            Dimension.builder().name(it.name).value(it.value).build()
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
