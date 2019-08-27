package io.morrissey

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import io.mockk.*
import io.morrissey.persistence.*
import io.morrissey.routes.*
import kotlin.test.*

class ApplicationTest {
    @KtorExperimentalAPI
    @Test
    fun testRoot() {
        val db = TestDb()

        mockkStatic("io.morrissey.routes.SwitchesKt")
        every { updateSwitch(any(), any()) } returns PhysicalSwitchResult.SuccessResult(listOf())
        every { getPhysicalSwitches(any(), any()) } returns PhysicalSwitchResult.SuccessResult(listOf())

        val client = HttpClient(MockEngine {
            respondOk()
        }) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }

        val serverConfig =
                HomeServerConfig(overrides = mapOf("authenticate" to "false", "sessionSignSecret" to "000987654321"))
        withTestApplication({ injectedModule(db, client, serverConfig) }) {
            handleRequest(HttpMethod.Get, "/api/iot/switches").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                println("response was ${response.content}")
            }
        }
    }
}
