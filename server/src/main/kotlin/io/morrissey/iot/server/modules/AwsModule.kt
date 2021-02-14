package io.morrissey.iot.server.modules

import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.assistedinject.FactoryModuleBuilder
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.aws.AwsAutomationStatusHandler
import io.morrissey.iot.server.aws.AwsResumeDateHandler
import io.morrissey.iot.server.model.Automation
import io.morrissey.iot.server.model.AutomationGroup
import io.morrissey.iot.server.services.AutomationStatusHandler
import io.morrissey.iot.server.services.AutomationStatusHandlerFactory
import io.morrissey.iot.server.services.ResumeDateHandler
import io.morrissey.iot.server.services.ResumeDateHandlerFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.crt.io.ClientBootstrap
import software.amazon.awssdk.crt.io.EventLoopGroup
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import java.net.URI
import java.util.UUID
import software.amazon.awssdk.crt.io.HostResolver




class AwsModule : KotlinModule() {
    override fun configure() {
        install(
            FactoryModuleBuilder()
                .implement(ResumeDateHandler::class.java, AwsResumeDateHandler::class.java)
                .build(ResumeDateHandlerFactory::class.java)
        )
        install(
            FactoryModuleBuilder()
                .implement(AutomationStatusHandler::class.java, AwsAutomationStatusHandler::class.java)
                .build(AutomationStatusHandlerFactory::class.java)
        )

        // Static injection??? Code smell!! You bet, but I blame exposed as they're calling constructors directly in
        // static code without a proper path to allow injection other than giving the static class an instance of the
        // injector so it can override and perform the injection properly. It is what it is.
        requestStaticInjection<AutomationGroup>()
        requestStaticInjection<Automation>()
    }

    @Provides
    @Singleton
    fun awsIotMqttClient(homeServerConfig: HomeServerConfig): MqttClientConnection {
        val eventLoopGroup = EventLoopGroup(1)
        val resolver = HostResolver(eventLoopGroup)
        val clientBootstrap = ClientBootstrap(eventLoopGroup, resolver)

        return AwsIotMqttConnectionBuilder.newMtlsBuilder(
            homeServerConfig.awsIotPublicCert,
            homeServerConfig.awsIotPrivateKey
        ).withCertificateAuthority(homeServerConfig.awsCaRoot)
            .withEndpoint(homeServerConfig.awsIotEndpoint)
            .withClientId("HomeIoTServer-${UUID.randomUUID()}")
            .withBootstrap(clientBootstrap)
            .build()
    }

    @Provides
    @Singleton
    fun awsCredentials(homeServerConfig: HomeServerConfig): AwsCredentialsProvider = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(
            homeServerConfig.awsAccessKey, homeServerConfig.awsSecretKey
        )
    )

    @Provides
    @Singleton
    fun ebEventsClient(
        homeServerConfig: HomeServerConfig,
        credentialsProvider: AwsCredentialsProvider
    ): EventBridgeClient {
        return EventBridgeClient.builder()
            .region(Region.US_WEST_2)
            .credentialsProvider(credentialsProvider)
            .endpointOverride(
                URI.create("https://${homeServerConfig.awsCloudWatchEventsEndpoint}:443")
            )
            .build()
    }

    @Provides
    @Singleton
    fun cwClient(
        credentialsProvider: AwsCredentialsProvider
    ): CloudWatchClient {
        return CloudWatchClient.builder()
            .region(Region.US_WEST_2)
            .credentialsProvider(credentialsProvider)
            .build()
    }
}
