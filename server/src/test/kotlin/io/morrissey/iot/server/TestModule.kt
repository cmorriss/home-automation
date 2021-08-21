package io.morrissey.iot.server

import io.ktor.client.*
import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.persistence.TestDb
import org.koin.core.module.Module
import org.koin.dsl.module
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import java.util.concurrent.CompletableFuture

@Suppress("MemberVisibilityCanBePrivate")
class TestModule(private val client: HttpClient, private val synchronizer: AutomationSynchronizer) {
    val mockMqttClientConnection: MqttClientConnection = mockk(relaxed = true)
    val mockEventBridgeClient: EventBridgeClient = mockk(relaxed = true)
    val mockCloudWatchClient: CloudWatchClient = mockk(relaxed = true)
    val mockIotDataPlane: IotDataPlaneClient = mockk(relaxed = true)

    fun toModule() : Module {
        every { mockMqttClientConnection.connect() }.returns(mockk<CompletableFuture<Boolean>>().apply {
            every { get() }.returns(true)
        })

        return module(override = true) {
            single { client }
            single { synchronizer }
            single { mockMqttClientConnection }
            single { mockEventBridgeClient }
            single { mockCloudWatchClient }
            single { mockIotDataPlane }
            single<ApplicationDatabase> { TestDb() }
        }
    }
}
