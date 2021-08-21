package io.morrissey.iot.server.persistence

import io.mockk.every
import io.mockk.mockk
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.aws.AwsAutomationStatusHandler
import io.morrissey.iot.server.layout.HomeIotLayoutImpl
import io.morrissey.iot.server.layout.calcEndCron
import io.morrissey.iot.server.model.AutomationGroup
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.modules.dbModule
import io.morrissey.iot.server.services.AutomationStatusHandler
import io.morrissey.iot.server.services.ResumeDateHandler
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import kotlin.test.assertEquals

class TestAutomationDef {
    @Test
    fun testCalcEndTime() {
        assertEquals("30 1", calcEndCron("0 1", 30))
        assertEquals("25 0", calcEndCron("25 23", 60))
        assertEquals("5 1", calcEndCron("25 23", 100))
    }

    @Test
    fun testPopulateDbTables() {
        val controller: Controller = mockk(relaxed = true)
        val ebClient: EventBridgeClient = mockk(relaxed = true)
        val synchronizer: AutomationSynchronizer = mockk(relaxed = true)
        startKoin {
            modules(module {
                single { synchronizer }
                single { ebClient }
            }, dbModule())
        }
        TestDb().initialize()
        val layout = HomeIotLayoutImpl(controller, synchronizer)
        layout.populate()
        layout.populate()
        transaction { val automationGroups = AutomationGroup.find { Op.TRUE }
            print(automationGroups.count())
        }
        stopKoin()
    }
}
