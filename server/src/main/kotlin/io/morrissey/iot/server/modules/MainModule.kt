package io.morrissey.iot.server.modules

import com.google.inject.BindingAnnotation
import com.google.inject.Provides
import com.google.inject.Singleton
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.ktor.application.Application
import io.ktor.routing.Route
import io.ktor.routing.routing
import io.morrissey.iot.server.HomeServerApplicationStartup
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.layout.HomeIotLayout
import io.morrissey.iot.server.layout.HomeIotLayoutImpl
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.persistence.ApplicationDatabaseProd
import io.morrissey.iot.server.routes.*
import io.morrissey.iot.server.security.AuthenticatingRouter
import io.morrissey.iot.server.security.AuthorizingRouter
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region.US_WEST_2
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import java.net.URI
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


class MainModule(
    private val application: Application, private val homeServerConfig: HomeServerConfig
) : KotlinModule() {

    override fun configure() {
        bind<Application>().toInstance(application)
        bind<HomeServerConfig>().toInstance(homeServerConfig)

        bind<ApplicationDatabase>().to<ApplicationDatabaseProd>().asEagerSingleton()
        bind<HomeIotLayout>().to<HomeIotLayoutImpl>()
        bind<HomeServerApplicationStartup>().asEagerSingleton()
//        bind<AppRedirectRoute>().asEagerSingleton()
        bind<ControlGroupRoutes>().asEagerSingleton()
        bind<ControlsRoutes>().asEagerSingleton()
        bind<LoginRoute>().asEagerSingleton()
        bind<StaticContentRoutes>().asEagerSingleton()
        bind<AutomationGroupRoutes>().asEagerSingleton()
        bind<AutomationRoutes>().asEagerSingleton()
        bind<ControlActionRoutes>().asEagerSingleton()
        bind<MetricRoutes>().asEagerSingleton()
        bind<MetricDataRoute>().asEagerSingleton()
        bind<AutomationGroupActionRoutes>().asEagerSingleton()
        bind<AutomationActionRoutes>().asEagerSingleton()

        install(AwsModule())

    }

    @Provides
    @AuthorizedRoute
    @Singleton
    fun authorizingRoute(): Route {
        return application.routing {
            AuthorizingRouter(this, homeServerConfig).authorizingRoute
        }
    }

    @Provides
    @AuthenticatedRoute
    @Singleton
    fun authenticatingRoute(): Route {
        return application.routing {
            AuthenticatingRouter(this).authenticatingRoute
        }
    }

    @Provides
    @Singleton
    fun iotDataPlaneClient(credentialsProvider: AwsCredentialsProvider): IotDataPlaneClient {
        return IotDataPlaneClient.builder().region(US_WEST_2).credentialsProvider(credentialsProvider).endpointOverride(
            URI.create("https://${homeServerConfig.awsIotEndpoint}:443")
        ).build()
    }
}

@BindingAnnotation
@Target(CLASS, VALUE_PARAMETER, FUNCTION)
@Retention(RUNTIME)
annotation class AuthorizedRoute

@BindingAnnotation
@Target(CLASS, VALUE_PARAMETER, FUNCTION)
@Retention(RUNTIME)
annotation class AuthenticatedRoute
