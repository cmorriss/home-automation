package io.morrissey.iot.server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.model.AutomationDto
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.ktor.ext.modules
import org.slf4j.event.Level
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.iotdataplane.model.GetThingShadowRequest
import software.amazon.awssdk.services.iotdataplane.model.GetThingShadowResponse
import java.util.function.Consumer
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    private val mockScheduler = mockk<AutomationSynchronizer>(relaxed = true)

    private val client = HttpClient(MockEngine {
        respondOk()
    }) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    private val testModule = TestModule(client, mockScheduler)

    private fun <R> runTest(test: TestApplicationEngine.() -> R): R {

        val serverConfig: HomeServerConfig = mockk(relaxed = true)
        every { serverConfig.logLevel } returns Level.DEBUG
        every { serverConfig.sessionSignSecret } returns "12345"
        every { serverConfig.clientId } returns "test"
        every { serverConfig.clientSecret } returns "test"
        every { serverConfig.authenticate } returns false

        val thingShadowPayload = Controller.ControlThingPayload(Controller.ControlThingState())
        val jsonThingShadow = json.encodeToString(Controller.ControlThingPayload.serializer(), thingShadowPayload)
        val getShadowResponse = mockk<GetThingShadowResponse>()
        every { getShadowResponse.payload() }.returns(SdkBytes.fromString(jsonThingShadow, Charsets.UTF_8))
        every { testModule.mockIotDataPlane.getThingShadow(any<Consumer<GetThingShadowRequest.Builder>>()) }.returns(
            getShadowResponse
        )

        return withTestApplication({
            module(listOf(testModule.toModule()))
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
        stopKoin()
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
        stopKoin()
    }
}
