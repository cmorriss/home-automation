package io.morrissey.persistence

import io.morrissey.model.*
import io.morrissey.model.IotLocation.*
import io.morrissey.model.SwitchType.*
import io.morrissey.routes.log
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeH2DB(private val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")) :
    HomeDao {

    constructor(dir: File) : this(
        Database.connect("jdbc:h2:file:${dir.canonicalFile.absolutePath}", driver = "org.h2.Driver")
    ) {
        // Create the used tables
        transaction(db) {
            log.info("db during create = $db")
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Switches, Schedules, ScheduleStatuses, Users)
            if (switches().isEmpty()) {
                createSwitches()
            }
        }
    }

    private fun createSwitches() {
        createSwitch(Switch(name = "La Isla Bonita", type = IRRIGATION_VALVE, location = Backyard, locationId = 2))
        createSwitch(Switch(name = "Irish Moss", type = IRRIGATION_VALVE, location = Backyard, locationId = 3))
        createSwitch(Switch(name = "Grass", type = IRRIGATION_VALVE, location = Backyard, locationId = 4))
        createSwitch(Switch(name = "Flood Light", type = LIGHT_SWITCH, location = Backyard, locationId = 5))
        createSwitch(Switch(name = "Orchard", type = IRRIGATION_VALVE, location = Backyard, locationId = 5))
        createSwitch(Switch(name = "Garden", type = IRRIGATION_VALVE, location = Frontyard, locationId = 2))
        createSwitch(Switch(name = "Bushes", type = IRRIGATION_VALVE, location = Frontyard, locationId = 3))
        createSwitch(Switch(name = "Ferns", type = IRRIGATION_VALVE, location = Frontyard, locationId = 4))
    }

    //////////////////
    // User
    /////////////////
    override fun createUser(user: User): Int = transaction(db) {
        Users.insertAndGetId {
            it[oauthId] = user.oauthId
            it[firstName] = user.firstName
            it[lastName] = user.lastName
            it[email] = user.email
            it[picUrl] = user.picUrl
            it[createDate] = DateTime.now()
        }.value
    }

    override fun updateUser(user: User) {
        transaction(db) {
            Users.update {
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[picUrl] = user.picUrl
                it[email] = user.email
            }
        }
    }

    override fun user(id: Int) = findUser { Users.id eq id }

    override fun userByOauthId(id: String) = findUser { Users.oauthId eq id }

    override fun userByEmail(email: String) = findUser { Users.email eq email }

    private fun findUser(select: SqlExpressionBuilder.() -> Op<Boolean>): User? = transaction(db) {
        Users.select(select).firstOrNull()?.let { toUser(it) }
    }

    private fun toUser(rr: ResultRow): User = transaction(db) {
        with(Users) {
            User(
                id = rr[id].value,
                oauthId = rr[oauthId],
                email = rr[email],
                firstName = rr[firstName],
                lastName = rr[lastName],
                picUrl = rr[picUrl]
            )
        }
    }

    ////////////////////////
    // Switches
    ////////////////////////
    override fun createSwitch(aSwitch: Switch): Int = transaction(db) {
        Switches.insertAndGetId {
            it.fromSwitch(aSwitch)
            it[schedule] = EntityID(createSchedule(aSwitch.schedule), Schedules)
        }.value
    }

    override fun updateSwitch(aSwitch: Switch) {
        transaction(db) {
            Switches.update(where = { Switches.id eq aSwitch.id }) {
                it.fromSwitch(aSwitch)
                updateSchedule(aSwitch.schedule)
            }
        }
    }

    private fun UpdateBuilder<*>.fromSwitch(aSwitch: Switch) {
        this[Switches.name] = aSwitch.name
        this[Switches.type] = aSwitch.type.name
        this[Switches.location] = aSwitch.location.name
        this[Switches.locationId] = aSwitch.locationId
        this[Switches.locationStatus] = aSwitch.locationStatus.name
        this[Switches.locationStatusMessage] = aSwitch.locationStatusMessage
        this[Switches.on] = aSwitch.on
        this[Switches.lastUpdate] = DateTime.now().millis
    }

    override fun switch(id: Int) = findSwitch { Switches.id eq id }

    override fun switches(): List<Switch> = transaction(db) {
        Switches.selectAll().map {
            toSwitch(it)
        }
    }

    override fun switchBySchedule(scheduleId: Int) =
        findSwitch { Switches.schedule eq EntityID(scheduleId, Schedules) }

    private fun findSwitch(select: SqlExpressionBuilder.() -> Op<Boolean>): Switch? =
        transaction(db) {
            Switches.select(select).firstOrNull()?.let { toSwitch(it) }
        }

    private fun toSwitch(rr: ResultRow): Switch = transaction(db) {
        with(Switches) {
            Switch(
                id = rr[id].value,
                name = rr[name],
                type = SwitchType.valueOf(rr[type]),
                location = IotLocation.valueOf(rr[location]),
                locationId = rr[locationId],
                locationStatus = LocationStatus.valueOf((rr[locationStatus])),
                locationStatusMessage = rr[locationStatusMessage],
                on = rr[on],
                lastUpdate = rr[lastUpdate],
                schedule = this@HomeH2DB.schedule(rr[schedule].value)!!
            )
        }
    }

    //////////////
    // Schedule
    //////////////
    override fun createSchedule(schedule: Schedule): Int = transaction(db) {
        Schedules.insertAndGetId {
            it.fromSchedule(schedule)
        }.value
    }

    override fun updateSchedule(schedule: Schedule) {
        transaction(db) {
            Schedules.update(where = { Schedules.id eq schedule.id }) {
                it.fromSchedule(schedule)
            }
        }
    }

    private fun UpdateBuilder<*>.fromSchedule(schedule: Schedule) {
        this[Schedules.daysOn] = schedule.daysOn.joinToString { it.name }
        this[Schedules.startTime] = schedule.startTime
        this[Schedules.duration] = schedule.duration
    }

    override fun schedule(id: Int): Schedule? = transaction(db) {
        Schedules.select {
            Schedules.id eq id
        }.firstOrNull()?.let { toSchedule(it) }
    }

    override fun schedules(): List<Schedule> = transaction(db) {
        Schedules.selectAll().map { toSchedule(it) }
    }

    private fun toSchedule(rr: ResultRow): Schedule = transaction(db) {
        with(Schedules) {
            val daysOn = if (rr[daysOn].isNotBlank()) {
                rr[daysOn].split(",").map {
                    DayOfWeek.valueOf(it.trim())
                }.toSet()
            } else {
                emptySet()
            }
            Schedule(
                id = rr[id].value,
                daysOn = daysOn,
                startTime = rr[startTime],
                duration = rr[duration]
            )
        }
    }

    override fun scheduleStatus(): ScheduleStatus = transaction(db) {
        val status = ScheduleStatuses.select { ScheduleStatuses.id.eq(1) }.firstOrNull()
        val returnStatus: ScheduleStatus
        if (status == null) {
            returnStatus = ScheduleStatus("active", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
            ScheduleStatuses.insert {
                it[ScheduleStatuses.status] = returnStatus.status
                it[ScheduleStatuses.pausedUntilDate] = returnStatus.pausedUntilDate
            }
        } else {
            returnStatus = toScheduleStatus(status)
        }
        returnStatus
    }


    override fun updateScheduleStatus(status: ScheduleStatus) {
        transaction(db) {
            ScheduleStatuses.update(where = { ScheduleStatuses.id eq 1 }) {
                it.fromScheduleStatus(status)
            }
        }
    }

    private fun UpdateBuilder<*>.fromScheduleStatus(status: ScheduleStatus) {
        this[ScheduleStatuses.status] = status.status
        this[ScheduleStatuses.pausedUntilDate] = status.pausedUntilDate
    }

    private fun toScheduleStatus(rr: ResultRow): ScheduleStatus = transaction(db) {
        with(ScheduleStatuses) {
            ScheduleStatus(
                status = rr[status],
                pausedUntilDate = rr[pausedUntilDate]
            )
        }
    }
}