package io.morrissey.iot.server.aws

import com.google.inject.Guice
import com.google.inject.util.Modules
import dev.misfitlabs.kotlinguice4.KotlinModule
import io.ktor.util.*
import io.mockk.clearMocks
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
import io.morrissey.iot.server.modules.AwsModule
import io.morrissey.iot.server.persistence.TestDb
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.eventbridge.model.ListRulesRequest
import software.amazon.awssdk.services.eventbridge.model.ListRulesResponse
import software.amazon.awssdk.services.eventbridge.model.ListTargetsByRuleRequest
import software.amazon.awssdk.services.eventbridge.model.ListTargetsByRuleResponse
import software.amazon.awssdk.services.eventbridge.model.PutRuleRequest
import software.amazon.awssdk.services.eventbridge.model.PutTargetsRequest
import software.amazon.awssdk.services.eventbridge.model.Rule
import software.amazon.awssdk.services.eventbridge.model.Target
import java.time.Instant
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
        private lateinit var testAutomation: Automation
        private const val startCron = "30 22 ? * TUE *"

        @Suppress("unused")
        @JvmStatic
        fun calcEndCronTest(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    CalcEndCronParam(
                        startCron = "20 1 ? * SUN *",
                        duration = 30,
                        endCron = "50 1 ? * SUN *"
                    )
                ),
                Arguments.of(
                    CalcEndCronParam(
                        startCron = "20 1 ? * SUN *",
                        duration = 40,
                        endCron = "0 2 ? * SUN *"
                    )
                ),
                Arguments.of(
                    CalcEndCronParam(
                        startCron = "20 23 ? * SAT *",
                        duration = 40,
                        endCron = "0 0 ? * SUN *"
                    )
                ),
                Arguments.of(
                    CalcEndCronParam(
                        startCron = "20 23 ? * TUE,SAT,SUN *",
                        duration = 110,
                        endCron = "10 1 ? * WED,SUN,MON *"
                    )
                )
            )
        }

        @Suppress("unused")
        @JvmStatic
        fun convertCronTimeZone(): Stream<Arguments> {
            // Assumes PST
            val localHour = if (LOCAL_TIME_ZONE.rules.isDaylightSavings(Instant.now())) 6 else 7
            return Stream.of(
                Arguments.of(
                    ConvertCronTimeZoneParam(
                        startCron = "30 23 ? * MON,WED,FRI *",
                        from = LOCAL_TIME_ZONE,
                        to = GMT_TIME_ZONE,
                        expectedCron = "30 $localHour ? * TUE,THU,SAT *"
                    )
                ),
                Arguments.of(
                    ConvertCronTimeZoneParam(
                        startCron = "30 $localHour ? * TUE,THU,SAT *",
                        from = GMT_TIME_ZONE,
                        to = LOCAL_TIME_ZONE,
                        expectedCron = "30 23 ? * MON,WED,FRI *"
                    )
                ),
                Arguments.of(
                    ConvertCronTimeZoneParam(
                        startCron = "30 $localHour ? * MON,WED,FRI *",
                        from = LOCAL_TIME_ZONE,
                        to = GMT_TIME_ZONE,
                        expectedCron = "30 15 ? * MON,WED,FRI *"
                    )
                ),
                Arguments.of(
                    ConvertCronTimeZoneParam(
                        startCron = "30 15 ? * MON,WED,FRI *",
                        from = GMT_TIME_ZONE,
                        to = LOCAL_TIME_ZONE,
                        expectedCron = "30 $localHour ? * MON,WED,FRI *"
                    )
                )
            )
        }

        val mockEbClient = mockk<EventBridgeClient>(relaxed = true)

        @BeforeAll
        @JvmStatic
        fun setup() {
            TestDb().initialize()

            Guice.createInjector(
                Modules.override(AwsModule()).with(object : KotlinModule() {
                    override fun configure() {
                        bind<EventBridgeClient>().toInstance(mockEbClient)
                    }
                })
            )

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

            testAutomation = transaction {
                Automation.new {
                    actionId = storedControlAction.id.value
                    actionType = CONTROL
                    eventId = -1
                    eventType = SCHEDULE
                    associatedAutomationId = -1
                    status = AutomationStatusEnum.ACTIVE
                    resumeDate = ""
                    name = "TestAutomation"
                    cron = startCron
                }
            }
        }
    }

    @Test
    fun createScheduledAutomation() {
        clearMocks(mockEbClient)
        val mockHomeServerConfig = mockk<HomeServerConfig>()
        val mockResponse = mockk<ListRulesResponse>()
        every { mockResponse.rules() }.returns(emptyList())
        every { mockEbClient.listRules(any<Consumer<ListRulesRequest.Builder>>()) }.returns(mockResponse)
        every { mockHomeServerConfig.awsIotTriggerLambdaArn }.returns("")

        val synchronizer = AutomationSynchronizer(mockEbClient, mockHomeServerConfig)

        synchronizer.synchronize(testAutomation.id.value)

        verify(exactly = 1) {
            mockEbClient.putRule(any<Consumer<PutRuleRequest.Builder>>())
        }
        verify(exactly = 1) {
            mockEbClient.putTargets(any<Consumer<PutTargetsRequest.Builder>>())
        }
    }

    @Test
    fun updateScheduledAutomation() {
        clearMocks(mockEbClient)
        val mockHomeServerConfig = mockk<HomeServerConfig>()
        val mockResponse = mockk<ListRulesResponse>()
        val mockRule = mockk<Rule>(relaxed = true)
        val mockListTargetsResponse = mockk<ListTargetsByRuleResponse>()
        val mockTarget = mockk<Target>(relaxed = true)
        every { mockTarget.id() }.returns(storedControl.toTargetId())
        every { mockListTargetsResponse.targets() }.returns(listOf(mockTarget))
        every { mockListTargetsResponse.hasTargets() }.returns(true)
        every { mockRule.name() }.returns(testAutomation.toEventBridgeRuleName())
        every { mockEbClient.listTargetsByRule(any<Consumer<ListTargetsByRuleRequest.Builder>>()) }.returns(
            mockListTargetsResponse
        )
        every { mockEbClient.listRules(any<Consumer<ListRulesRequest.Builder>>()) }.returns(mockResponse)
        every { mockResponse.rules() }.returns(listOf(mockRule))
        every { mockHomeServerConfig.awsIotTriggerLambdaArn }.returns("")

        val synchronizer = AutomationSynchronizer(mockEbClient, mockHomeServerConfig)

        synchronizer.synchronize(testAutomation.id.value)

        verify {
            mockEbClient.listRules(any<Consumer<ListRulesRequest.Builder>>())
        }
        verify(exactly = 1) {
            mockEbClient.putRule(any<Consumer<PutRuleRequest.Builder>>())
        }
        verify(exactly = 1) {
            mockEbClient.putTargets(any<Consumer<PutTargetsRequest.Builder>>())
        }
    }

    @ParameterizedTest
    @MethodSource
    fun convertCronTimeZone(params: ConvertCronTimeZoneParam) {
        val convertedCron = params.startCron.convertCron(params.from, params.to)
        assertEquals(params.expectedCron, convertedCron)
    }

    @ParameterizedTest
    @MethodSource
    fun calcEndCronTest(params: CalcEndCronParam) {
        assertEquals(params.endCron, calcEndCron(params.startCron, params.duration))
    }
}

data class ConvertCronTimeZoneParam(
    val startCron: String,
    val from: ZoneId,
    val to: ZoneId,
    val expectedCron: String
)

data class CalcEndCronParam(
    val startCron: String,
    val duration: Int,
    val endCron: String
)
