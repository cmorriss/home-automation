package io.morrissey.iot.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.inject.Guice
import com.google.inject.util.Modules
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.model.AutomationDto
import io.morrissey.iot.server.modules.CallModule
import io.morrissey.iot.server.modules.MainModule
import org.junit.jupiter.api.Test
import org.slf4j.event.Level
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.iotdataplane.model.GetThingShadowRequest
import software.amazon.awssdk.services.iotdataplane.model.GetThingShadowResponse
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    fun <R> runTest(test: TestApplicationEngine.() -> R): R {
        val mockScheduler = mockk<AutomationSynchronizer>(relaxed = true)

        val client = HttpClient(MockEngine {
            respondOk()
        }) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }

        val serverConfig: HomeServerConfig = mockk(relaxed = true)
        every { serverConfig.logLevel } returns Level.DEBUG
        every { serverConfig.sessionSignSecret } returns "12345"
        every { serverConfig.clientId } returns "test"
        every { serverConfig.clientSecret } returns "test"
        every { serverConfig.authenticate } returns false

        val testModule = TestModule(client, mockScheduler)

        val thingShadowPayload = Controller.ControlThingPayload(Controller.ControlThingState())
        val jsonThingShadow = Gson().toJson(thingShadowPayload)
        val getShadowResponse = mockk<GetThingShadowResponse>()
        every { getShadowResponse.payload() }.returns(SdkBytes.fromString(jsonThingShadow, Charsets.UTF_8))
        every { testModule.mockIotDataPlane.getThingShadow(any<Consumer<GetThingShadowRequest.Builder>>()) }.returns(getShadowResponse)

        return withTestApplication({
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
                            }, test)
    }

    @Test
    fun testRoot() {
        runTest {
            handleRequest(HttpMethod.Get, "/api/iot/controls").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                println("response was ${response.content}")
            }
        }
    }

    @Test
    fun testUpdateAutomation() {
        runTest {
            handleRequest(HttpMethod.Get, "/api/iot/automations").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val automationDtos: List<AutomationDto> = jacksonObjectMapper().readValue(response.content!!)
                assertTrue { automationDtos.isNotEmpty() }
            }
        }
    }
}
