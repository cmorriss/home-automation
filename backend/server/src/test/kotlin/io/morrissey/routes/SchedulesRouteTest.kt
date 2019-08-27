package io.morrissey.routes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.InternalAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.morrissey.HomeServerConfig
import io.morrissey.injectedModule
import io.morrissey.model.IotLocation
import io.morrissey.model.Switch
import io.morrissey.model.Schedule
import io.morrissey.model.SwitchType
import io.morrissey.persistence.TestDb
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.event.Level
import java.time.DayOfWeek.TUESDAY
import kotlin.test.assertEquals

class SchedulesRouteTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun dbSetup() {
            TestDb.setup()
        }

        @AfterAll
        @JvmStatic
        fun dbTeardown() {
            TestDb.teardown()
        }
    }

    @UseExperimental(InternalAPI::class)
    @Test
    fun testSchedules() {
        val db = TestDb()

        val schedule = Schedule(
            startTime = "22:30",
            duration = 20
        )

        val valve = Switch(
            name = "TestValve",
            type = SwitchType.IRRIGATION_VALVE,
            location = IotLocation.Frontyard,
            locationId = 1,
            schedule = schedule
        )
        val valveId = db.createSwitch(valve)
        val storedValve = db.switch(valveId) ?: throw Exception("Couldn't find valve")
        val updatedSchedule = storedValve.schedule.copy(daysOn = setOf(TUESDAY))

        mockkStatic("io.morrissey.routes.SwitchesKt")
        every { updateSwitch(any(), any()) } returns PhysicalSwitchResult.SuccessResult(listOf())

        val httpClient = HttpClient(MockEngine {
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

        withTestApplication({
            injectedModule(db, httpClient, serverConfig)
        }) {
            handleRequest(HttpMethod.Post, "/api/iot/schedules/${updatedSchedule.id}") {
                setBody(jacksonObjectMapper().writeValueAsString(updatedSchedule))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
        }.apply {
            assertEquals(setOf(TUESDAY), db.switch(updatedSchedule.id)!!.schedule.daysOn)
        }
    }
}