package io.morrissey.iot.server

import com.google.inject.Guice
import com.google.inject.util.Modules
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.mockk
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.modules.CallModule
import io.morrissey.iot.server.modules.MainModule
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    @Disabled
    fun testRoot() {
        val mockScheduler = mockk<AutomationSynchronizer>()

        val client = HttpClient(MockEngine {
            respondOk()
        }) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }

        val serverConfig = HomeServerConfig(
            overrides = mapOf(
                "authenticate" to "false", "sessionSignSecret" to "000987654321"
            )
        )

        val testModule = TestModule(client, mockScheduler)

        withTestApplication({
                                val injector = Guice.createInjector(
                                    Modules.override(
                                        MainModule(
                                            this, serverConfig
                                        )
                                    ).with(testModule)
                                )
                                // Intercept application call and put child injector into attributes
                                intercept(ApplicationCallPipeline.Features) {
                                    call.attributes.put(
                                        InjectorKey, injector.createChildInjector(
                                            CallModule(
                                                call
                                            )
                                        )
                                    )
                                }
                            }) {
            handleRequest(HttpMethod.Get, "/api/iot/controls").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                println("response was ${response.content}")
            }
        }
    }
}
