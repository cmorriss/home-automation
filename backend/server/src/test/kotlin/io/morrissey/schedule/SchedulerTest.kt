package io.morrissey.schedule

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.util.InternalAPI
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.morrissey.model.*
import io.morrissey.persistence.TestDb
import io.morrissey.routes.refreshSwitchState
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.DayOfWeek.TUESDAY
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@InternalAPI
class SchedulerTest {
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

    private lateinit var db: TestDb
    private lateinit var storedSchedule: Schedule
    private lateinit var storedSwitch: Switch
    private lateinit var httpClient: HttpClient
    private lateinit var scheduler: Scheduler
    private lateinit var mockHttpEngine: MockEngine
    private val startHour = 22
    private val startMinute = 30
    private val duration = 20
    private val startInstant = LocalDateTime.of(2019, 5, 28, startHour, startMinute).toInstant(defaultOffset)
    private val stopInstant = startInstant.plus(duration.toLong(), ChronoUnit.MINUTES)

    @BeforeEach
    fun setup() {

        db = TestDb()

        val schedule = Schedule(
            daysOn = setOf(TUESDAY),
            startTime = "$startHour:$startMinute",
            duration = duration
        )

        val valve = Switch(
            name = "TestValve",
            type = SwitchType.IRRIGATION_VALVE,
            location = IotLocation.Backyard,
            locationId = 1,
            schedule = schedule
        )
        val valveId = db.createSwitch(valve)
        storedSwitch = db.switch(valveId)!!
        storedSchedule = storedSwitch.schedule

        mockkStatic("io.morrissey.routes.SwitchesKt")
        every { refreshSwitchState(any(), any()) } just Runs
    }

    @Test
    fun timerOnTest() {
        setupMockEngineResponse(true)
        scheduler = Scheduler(
            db,
            Timer(),
            httpClient,
            Clock.fixed(startInstant, ZoneId.systemDefault())
        )

        Thread.sleep(1000)

        val body = mockHttpEngine.requestHistory.last().body as TextContent
        val realValve = jacksonObjectMapper().readValue<PhysicalSwitch>(body.text)
        assertEquals(PhysicalSwitch(storedSwitch.locationId, true), realValve)
        val valveState = db.switch(storedSwitch.id)
        assertNotNull(valveState)
        assertTrue(valveState.on)
    }

    @Test
    fun timerOffTest() {
        setupMockEngineResponse(false)
        db.updateSwitch(storedSwitch.copy(on = true))
        scheduler = Scheduler(
            db,
            Timer(),
            httpClient,
            Clock.fixed(stopInstant, ZoneId.systemDefault())
        )

        Thread.sleep(1000)

        val body = mockHttpEngine.requestHistory.last().body as TextContent
        val realValve = jacksonObjectMapper().readValue<PhysicalSwitch>(body.text)
        assertEquals(PhysicalSwitch(storedSwitch.locationId, false), realValve)
        val switchState = db.switch(storedSwitch.id)
        assertNotNull(switchState)
        assertFalse(switchState.on)
    }

    private fun setupMockEngineResponse(on: Boolean) {
        mockHttpEngine = MockEngine {
            respond(
                content = jacksonObjectMapper().writeValueAsString(PhysicalSwitch(1, on)),
                status = HttpStatusCode.OK,
                headers = headersOf("ContentType", ContentType.Application.Json.contentType)
            )
        }

        httpClient = HttpClient(mockHttpEngine) {
            install(JsonFeature) {
                serializer = GsonSerializer()
            }
        }
    }
}

val defaultOffset = OffsetDateTime.now().offset