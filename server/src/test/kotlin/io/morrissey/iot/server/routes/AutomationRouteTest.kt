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
import io.morrissey.iot.server.model.ActionType
import io.morrissey.iot.server.model.Automation
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.model.EventType
import io.morrissey.iot.server.module
import io.morrissey.iot.server.modules.mainModule
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.event.Level
import kotlin.test.assertEquals

class AutomationRouteTest {
    @Test
    fun testAutomations() {

//        var automation: Automation? = null
//
//        val serverConfig: HomeServerConfig = mockk(relaxed = true)
//        every { serverConfig.logLevel } returns Level.DEBUG
//        every { serverConfig.sessionSignSecret } returns "12345"
//        every { serverConfig.clientId } returns "test"
//        every { serverConfig.clientSecret } returns "test"
//        every { serverConfig.authenticate } returns false
//
//        withTestApplication({
//                module(serverConfigOverride = serverConfig)
//                            }) {
//            automation = transaction {
//                Automation.new {
//                    eventId = -1
//                    eventType = EventType.SCHEDULE
//                    cron = "30 22 ? * 2 *"
//                    actionId = 1
//                    actionType = ActionType.CONTROL
//                    associatedAutomationId = -1
//                    status = AutomationStatusEnum.ACTIVE
//                    resumeDate = ""
//                }
//            }
//
//            handleRequest(HttpMethod.Post, "/api/iot/automations/${transaction { automation!!.id }}") {
//                setBody(jacksonObjectMapper().writeValueAsString(transaction { automation!!.toDto() }))
//                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
//            }
//        }.apply {
//            assertEquals("30 22 ? * 2 *", transaction { Automation[automation!!.id].cron })
//        }
    }
}
