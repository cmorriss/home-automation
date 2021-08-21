package io.morrissey.iot.server.aws

import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.model.Metric
import io.morrissey.iot.server.model.MetricDimension
import io.morrissey.iot.server.model.MetricDto
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import java.io.FileInputStream
import java.util.*
import kotlin.test.assertTrue

class MetricDataTest {

    @Test
    fun testGetMetricData() {
        val secureProperties =
            Properties().apply { load(FileInputStream("/etc/home-automation-server/secure.properties")) }
        val credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                secureProperties.getProperty("awsAccessKey"), secureProperties.getProperty("awsSecretKey")
            )
        )
        val cwClient = CloudWatchClient.builder()

            .region(Region.US_WEST_2)
            .credentialsProvider(credentialsProvider)
            .build()

        val retriever = MetricDataRetriever(cwClient)
        val metric = mockk<Metric>()
        every { metric.externalNamespace }.returns("Home")
        every { metric.externalName }.returns("Master_Bath_Humidity")
        every { metric.dimensions }.returns(listOf())
        every { metric.statistic }.returns(Metric.Statistic.AVG)
        every { metric.period }.returns(300)
        every { metric.toDto() }.returns(MetricDto(1, "", "", "", 1, Metric.Statistic.AVG, listOf()))
        val data = retriever.retrieve(metric, "latest", MetricRetrievalDuration.THREE_HOURS)
        println("found data ${data.values}")
        println("found timestamps ${data.timestamps}")
        assertTrue { data.values.isNotEmpty() }
    }
}
