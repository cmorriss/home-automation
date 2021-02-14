package io.morrissey.iot.server.persistence

import io.mockk.mockk
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.layout.HomeIotLayoutImpl
import io.morrissey.iot.server.layout.calcEndCron
import io.morrissey.iot.server.model.AutomationGroup
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
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
        TestDb().initialize()
        val controller: Controller = mockk(relaxed = true)
        val synchronizer: AutomationSynchronizer = mockk(relaxed = true)
        val layout = HomeIotLayoutImpl(controller, synchronizer)
        layout.populate()
        layout.populate()
        transaction { val automationGroups = AutomationGroup.find { Op.TRUE }
            print(automationGroups.count())
        }
    }
}
