package io.morrissey.iot.server.aws

import com.google.inject.Guice
import io.ktor.util.InternalAPI
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.layout.calcEndCron
import io.morrissey.iot.server.model.ActionType.CONTROL
import io.morrissey.iot.server.model.Automation
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.model.Control
import io.morrissey.iot.server.model.ControlAction
import io.morrissey.iot.server.model.ControlState.OFF
import io.morrissey.iot.server.model.ControlState.ON
import io.morrissey.iot.server.model.ControlType.IRRIGATION_VALVE
import io.morrissey.iot.server.model.EventType.SCHEDULE
import io.morrissey.iot.server.model.Schedule
import io.morrissey.iot.server.modules.AwsModule
import io.morrissey.iot.server.persistence.TestDb
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesRequest
import software.amazon.awssdk.services.cloudwatchevents.model.ListRulesResponse
import software.amazon.awssdk.services.cloudwatchevents.model.ListTargetsByRuleRequest
import software.amazon.awssdk.services.cloudwatchevents.model.ListTargetsByRuleResponse
import software.amazon.awssdk.services.cloudwatchevents.model.PutRuleRequest
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsRequest
import software.amazon.awssdk.services.cloudwatchevents.model.Rule
import software.amazon.awssdk.services.cloudwatchevents.model.Target
import java.time.ZoneId
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.test.assertEquals

@InternalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutomationSynchronizerTest {
    companion object {
        private lateinit var storedControl: Control
        private lateinit var storedControlAction: ControlAction
        private lateinit var testSchedule: Schedule
        private const val startCron = "30 22 ? * TUE *"

        @JvmStatic
        fun calcEndCronTest(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("20 1 ? * SUN *", 30, "50 1 ? * SUN *"),
                Arguments.of("20 1 ? * SUN *", 40, "0 2 ? * SUN *"),
                Arguments.of("20 23 ? * SAT *", 40, "0 0 ? * SUN *"),
                Arguments.of("20 23 ? * TUE,SAT,SUN *", 110, "10 1 ? * WED,SUN,MON *")
            )
        }

        @JvmStatic
        fun convertCronTimeZone(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("30 23 ? * MON,WED,FRI *", LOCAL_TIME_ZONE, GMT_TIME_ZONE, "30 6 ? * TUE,THU,SAT *"),
                Arguments.of("30 6 ? * TUE,THU,SAT *", GMT_TIME_ZONE, LOCAL_TIME_ZONE, "30 23 ? * MON,WED,FRI *"),
                Arguments.of("30 6 ? * MON,WED,FRI *", LOCAL_TIME_ZONE, GMT_TIME_ZONE, "30 13 ? * MON,WED,FRI *"),
                Arguments.of("30 13 ? * MON,WED,FRI *", GMT_TIME_ZONE, LOCAL_TIME_ZONE, "30 6 ? * MON,WED,FRI *")
            )
        }

        @BeforeAll
        @JvmStatic
        fun setup() {
            TestDb().initialize()

            Guice.createInjector(AwsModule())

            testSchedule = transaction {
                Schedule.new {
                    cron = startCron
                }
            }

            storedControl = transaction {
                Control.new {
                    thingName = "1"
                    name = "TestValve"
                    state = OFF
                    lastUpdate = ""
                    type = IRRIGATION_VALVE
                }
            }

            storedControlAction = transaction {
                ControlAction.new {
                    control = storedControl
                    state = ON
                }
            }

            transaction {
                Automation.new {
                    actionId = storedControlAction.id.value
                    actionType = CONTROL
                    eventId = testSchedule.id.value
                    eventType = SCHEDULE
                    associatedAutomationId = -1
                    status = AutomationStatusEnum.ACTIVE
                    resumeDate = ""
                    name = "TestAutomation"
                }
            }
        }
    }

    @Test
    fun createSchedule() {
        val mockCWClient = mockk<CloudWatchEventsClient>(relaxed = true)
        val mockHomeServerConfig = mockk<HomeServerConfig>()
        val mockResponse = mockk<ListRulesResponse>()
        every { mockResponse.rules() }.returns(emptyList())
        every { mockCWClient.listRules(any<Consumer<ListRulesRequest.Builder>>()) }.returns(mockResponse)
        every { mockHomeServerConfig.awsIotTriggerLambdaArn }.returns("")

        val scheduler = AutomationSynchronizer(mockCWClient, mockHomeServerConfig)

        scheduler.synchronize(testSchedule.id.value)

        verify(exactly = 1) {
            mockCWClient.putRule(any<Consumer<PutRuleRequest.Builder>>())
        }
        verify(exactly = 1) {
            mockCWClient.putTargets(any<Consumer<PutTargetsRequest.Builder>>())
        }
    }

    @Test
    fun updateSchedule() {
        val mockCWClient = mockk<CloudWatchEventsClient>(relaxed = true)
        val mockHomeServerConfig = mockk<HomeServerConfig>()
        val mockResponse = mockk<ListRulesResponse>()
        val mockRule = mockk<Rule>(relaxed = true)
        val mockListTargetsResponse = mockk<ListTargetsByRuleResponse>()
        val mockTarget = mockk<Target>(relaxed = true)
        every { mockTarget.id() }.returns(storedControl.toTargetId())
        every { mockListTargetsResponse.targets() }.returns(listOf(mockTarget))
        every { mockRule.name() }.returns(testSchedule.toCloudWatchRuleName())
        every { mockCWClient.listTargetsByRule(any<Consumer<ListTargetsByRuleRequest.Builder>>()) }.returns(
            mockListTargetsResponse
        )
        every { mockCWClient.listRules(any<Consumer<ListRulesRequest.Builder>>()) }.returns(mockResponse)
        every { mockResponse.rules() }.returns(listOf(mockRule))
        every { mockHomeServerConfig.awsIotTriggerLambdaArn }.returns("")

        val scheduler = AutomationSynchronizer(mockCWClient, mockHomeServerConfig)

        scheduler.synchronize(testSchedule.id.value)

        verify {
            mockCWClient.listTargetsByRule(any<Consumer<ListTargetsByRuleRequest.Builder>>())
        }
        verify(exactly = 1) {
            mockCWClient.putRule(any<Consumer<PutRuleRequest.Builder>>())
        }
        verify(exactly = 1) {
            mockCWClient.putTargets(any<Consumer<PutTargetsRequest.Builder>>())
        }
    }

    @ParameterizedTest
    @MethodSource
    fun convertCronTimeZone(startCron: String, from: ZoneId, to: ZoneId, expectedCron: String) {
        val convertedCron = startCron.convertCron(from, to)
        assertEquals(expectedCron, convertedCron)
    }

    @ParameterizedTest
    @MethodSource
    fun calcEndCronTest(startCron: String, duration: Int, endCron: String) {
        assertEquals(endCron, calcEndCron(startCron, duration))
    }
}
