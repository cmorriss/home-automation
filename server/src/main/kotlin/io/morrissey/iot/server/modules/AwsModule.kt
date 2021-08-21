package io.morrissey.iot.server.modules

import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.aws.MetricDataRetriever
import org.koin.core.module.Module
import org.koin.dsl.module
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.crt.io.ClientBootstrap
import software.amazon.awssdk.crt.io.EventLoopGroup
import software.amazon.awssdk.crt.io.HostResolver
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import java.net.URI
import java.util.*

fun awsModule(homeServerConfig: HomeServerConfig): Module {
    fun awsIotMqttClient(): MqttClientConnection {
        val eventLoopGroup = EventLoopGroup(1)
        val resolver = HostResolver(eventLoopGroup)
        val clientBootstrap = ClientBootstrap(eventLoopGroup, resolver)

        return AwsIotMqttConnectionBuilder.newMtlsBuilder(
            homeServerConfig.awsIotPublicCert, homeServerConfig.awsIotPrivateKey
        ).withCertificateAuthority(homeServerConfig.awsCaRoot).withEndpoint(homeServerConfig.awsIotEndpoint)
            .withClientId("HomeIoTServer-${UUID.randomUUID()}").withBootstrap(clientBootstrap)
            .withCleanSession(true).build()
    }

    fun awsCredentials(): AwsCredentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(
            homeServerConfig.awsAccessKey, homeServerConfig.awsSecretKey
        )
    )

    fun ebEventsClient(credentialsProvider: AwsCredentialsProvider): EventBridgeClient {
        return EventBridgeClient.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider)
            .endpointOverride(
                URI.create("https://${homeServerConfig.awsCloudWatchEventsEndpoint}:443")
            ).build()
    }

    fun cwClient(credentialsProvider: AwsCredentialsProvider): CloudWatchClient {
        return CloudWatchClient.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider).build()
    }

    fun iotDataPlaneClient(credentialsProvider: AwsCredentialsProvider): IotDataPlaneClient {
        return IotDataPlaneClient.builder().region(Region.US_WEST_2).credentialsProvider(credentialsProvider).endpointOverride(
            URI.create("https://${homeServerConfig.awsIotEndpoint}:443")
        ).build()
    }

    return module {
        single { awsIotMqttClient() }
        single { awsCredentials() }
        single { ebEventsClient(get()) }
        single { cwClient(get()) }
        single { iotDataPlaneClient(get()) }
        single { Controller(get(), get()) }
        single { AutomationSynchronizer(get(), homeServerConfig) }
        single { MetricDataRetriever(get()) }
    }
}
