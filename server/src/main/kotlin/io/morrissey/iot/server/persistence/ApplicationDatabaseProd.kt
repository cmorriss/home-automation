package io.morrissey.iot.server.persistence

import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

class ApplicationDatabaseProd @Inject constructor(private val homeServerConfig: HomeServerConfig) : ApplicationDatabase {
    override fun initialize() {
        Database.connect(homeServerConfig.dbUrl, driver = "org.h2.Driver")
        createProdDbTables()
    }
}

fun createProdDbTables() {
    transaction {
        log.debug("Creating the db tables...")
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(
            Automations,
            AutomationGroups,
            AutomationGroupActions,
            Controls,
            ControlActions,
            ControlGroups,
            AutomationActions,
            Metrics,
            Users
        )
    }
}
