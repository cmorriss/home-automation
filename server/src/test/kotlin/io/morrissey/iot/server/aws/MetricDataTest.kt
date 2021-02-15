package io.morrissey.iot.server.aws

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.morrissey.iot.server.model.Metric
import io.morrissey.iot.server.model.MetricDimension
import io.morrissey.iot.server.model.MetricDto
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult
import java.io.FileInputStream
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetricDataTest {

    @Test
    fun testGetMetricData() {
        val mockCwClient = mockk<CloudWatchClient>()

        val timeStamp1 = Instant.now()
        val timeStamp2 = timeStamp1.minusMillis(1000)
        val metricData1 = 1.0
        val metricData2 = 2.0
        val metricDataResults = MetricDataResult.builder().values(metricData1, metricData2).timestamps(timeStamp1, timeStamp2).build()
        val getMetricDataResponse = GetMetricDataResponse.builder().metricDataResults(metricDataResults).build()

        every { mockCwClient.getMetricData(match<GetMetricDataRequest> { request ->
            val metricStat = request.metricDataQueries()[0].metricStat()
            val metric = metricStat.metric()
            val dimension = metric.dimensions()[0]
            metric.namespace() == "SENSORS" &&
                metric.metricName() == "MASTER_BATH_HUMIDITY" &&
                metricStat.period() == 300 &&
                metricStat.stat() == Metric.Statistic.AVG.cwName &&
                dimension.name() == "LOCATION" &&
                dimension.value() == "MASTER_BATH"
        }) }.returns(getMetricDataResponse)

        val retriever = MetricDataRetriever(mockCwClient)
        val metric = mockk<Metric>()
        every { metric.externalNamespace }.returns("SENSORS")
        every { metric.externalName }.returns("MASTER_BATH_HUMIDITY")
        every { metric.dimensions }.returns(listOf(MetricDimension("LOCATION", "MASTER_BATH")))
        every { metric.statistic }.returns(Metric.Statistic.AVG)
        every { metric.period }.returns(300)
        every { metric.toDto() }.returns(MetricDto(1, "", "", "", 1, Metric.Statistic.AVG, listOf()))
        val data = retriever.retrieve(metric, "latest", MetricRetrievalDuration.THREE_HOURS)
        assertTrue { data.values.isNotEmpty() }
        assertEquals(listOf(metricData2, metricData1), data.values)
        assertEquals(2, data.timestamps.size)
    }
}
