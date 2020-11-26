package io.morrissey.iot.server

import dev.misfitlabs.kotlinguice4.KotlinModule
import io.ktor.client.HttpClient
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.persistence.ApplicationDatabase
import io.morrissey.iot.server.persistence.TestDb

class TestModule(private val client: HttpClient, private val synchronizer: AutomationSynchronizer) : KotlinModule() {
    override fun configure() {
        bind<HttpClient>().toInstance(client)
        bind<AutomationSynchronizer>().toInstance(synchronizer)
        bind<ApplicationDatabase>().to<TestDb>().asEagerSingleton()
    }
}
