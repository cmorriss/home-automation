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
        assertEquals("01:30", calcEndCron("01:00", 30))
        assertEquals("00:25", calcEndCron("23:25", 60))
        assertEquals("01:05", calcEndCron("23:25", 100))
    }

    @Test
    fun testPopulateDbTables() {
        TestDb()
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
