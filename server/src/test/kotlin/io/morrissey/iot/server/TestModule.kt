package io.morrissey.iot.server

import dev.misfitlabs.kotlinguice4.KotlinModule
import io.ktor.client.HttpClient
import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.persistence.TestDb
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import java.util.concurrent.CompletableFuture

@Suppress("MemberVisibilityCanBePrivate")
class TestModule(private val client: HttpClient, private val synchronizer: AutomationSynchronizer) : KotlinModule() {
    val mockMqttClientConnection: MqttClientConnection = mockk(relaxed = true)
    val mockEventBridgeClient: EventBridgeClient = mockk(relaxed = true)
    val mockCloudWatchClient: CloudWatchClient = mockk(relaxed = true)
    val mockIotDataPlane: IotDataPlaneClient = mockk(relaxed = true)

    init {
        every { mockMqttClientConnection.connect() }.returns(
            mockk<CompletableFuture<Boolean>>().apply {
                every { get() }.returns(true)
            }
        )
    }

    override fun configure() {
        bind<HttpClient>().toInstance(client)
        bind<AutomationSynchronizer>().toInstance(synchronizer)
        bind<MqttClientConnection>().toInstance(mockMqttClientConnection)
        bind<EventBridgeClient>().toInstance(mockEventBridgeClient)
        bind<CloudWatchClient>().toInstance(mockCloudWatchClient)
        bind<IotDataPlaneClient>().toInstance(mockIotDataPlane)

        bind<ApplicationDatabase>().to<TestDb>().asEagerSingleton()
    }
}
