package io.morrissey.iot.server.modules

import io.ktor.application.*
import io.ktor.routing.*
import io.morrissey.iot.server.HomeServerApplicationStartup
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.layout.HomeIotLayout
import io.morrissey.iot.server.layout.HomeIotLayoutImpl
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.persistence.ApplicationDatabaseProd
import io.morrissey.iot.server.routes.AuthenticatedRoute
import io.morrissey.iot.server.routes.AuthorizedRoute
import io.morrissey.iot.server.security.AuthenticatingRouter
import io.morrissey.iot.server.security.AuthorizingRouter
import org.koin.core.module.Module
import org.koin.dsl.module

fun mainModule(
    application: Application, homeServerConfig: HomeServerConfig
): Module {
    return module {
        plus(awsModule(homeServerConfig))
        single { application }
        single { homeServerConfig }
        single<ApplicationDatabase> { ApplicationDatabaseProd(homeServerConfig) }
        single<HomeIotLayout> { HomeIotLayoutImpl(get(), get()) }

        factory<Route>(AuthorizedRoute) {
            application.routing {
                AuthorizingRouter(this, homeServerConfig).authorizingRoute
            }
        }
        factory<Route>(AuthenticatedRoute) {
            application.routing {
                AuthenticatingRouter(this).authenticatingRoute
            }
        }

        single { HomeServerApplicationStartup(application, homeServerConfig, get(), get(), get(), get(), get()) }

    }
}
