@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.model.Schedule
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.event.Level
import java.time.DayOfWeek.TUESDAY
import kotlin.test.assertEquals

class SchedulesRouteTest {
    @Test
    @Disabled
    fun testSchedules() {

        val schedule = transaction {
            Schedule.new {
                cron = "30 22 ? * 2 *"
            }
        }

        val serverConfig: HomeServerConfig = mockk(relaxed = true)
        every { serverConfig.logLevel } returns Level.DEBUG
        every { serverConfig.sessionSignSecret } returns "12345"
        every { serverConfig.clientId } returns "test"
        every { serverConfig.clientSecret } returns "test"
        every { serverConfig.authenticate } returns false

        withTestApplication({
                                //injectedModule(httpClient, serverConfig)
                            }) {
            handleRequest(HttpMethod.Post, "/api/iot/schedules/${transaction { schedule.id }}") {
                setBody(jacksonObjectMapper().writeValueAsString(transaction { schedule.toDto() }))
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
        }.apply {
            assertEquals("30 22 ? * 2 *", transaction { Schedule[schedule.id].cron })
        }
    }
}