package io.morrissey.persistence

import io.morrissey.*
import io.morrissey.model.*
import io.morrissey.model.IotLocation.*
import io.morrissey.model.ScheduleStatus
import io.morrissey.model.SwitchKind.*
import io.requery.*
import io.requery.query.*
import io.requery.sql.*
import org.h2.jdbcx.*
import java.time.*
import java.time.format.*

class HomeH2DB(url: String) : HomeDao {
    val db: KotlinEntityDataStore<Persistable>

    init {
        val dataSource = JdbcDataSource().also { it.setUrl(url) }
        val configuration = KotlinConfiguration(dataSource = dataSource, model = Models.DEFAULT)
        SchemaModifier(dataSource, Models.DEFAULT).createTables(TableCreationMode.CREATE_NOT_EXISTS)
        db = KotlinEntityDataStore(configuration)
        val result = db.data.raw("SELECT * FROM INFORMATION_SCHEMA.TABLES")
        val resultList = result.toList()
        // Create the used tables
        createSwitches()
    }

    private fun createSwitches() {
        createSwitch("islaBonita", "La Isla Bonita", Backyard, 2)
        createSwitch("irishMoss", "Irish Moss", Backyard, 3)
        createSwitch("grass", "Grass", Backyard, 4)
        createSwitch("floodLight", "Flood Light", Backyard, 6, LIGHT_SWITCH)
        createSwitch("orchard", "Orchard", Backyard, 5)
        createSwitch("garden", "Garden", Frontyard, 2)
        createSwitch("bushes", "Bushes", Frontyard, 3)
        createSwitch("ferns", "Ferns", Frontyard, 4)
    }

    private fun createSwitch(
        givenId: String,
        name: String,
        location: IotLocation,
        locationId: Int,
        kind: SwitchKind = IRRIGATION_VALVE
    ) {
        var existing = switchByGivenId(givenId)
        if (existing == null) {
            createSwitch(SwitchEntity().apply {
                this.givenId = givenId
                this.name = name
                this.kind = kind
                this.location = location
                this.locationId = locationId
                lastUpdate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                schedule = schedule(createSchedule(emptySchedule()))!!
            })
        }
    }

    //////////////////
    // User
    /////////////////
    override fun createUser(user: User): Int {
        return db.insert(user).id
    }

    override fun updateUser(user: User) {
        db.update(user)
    }

    override fun user(id: Int) = db.findByKey(User::class, id)

    override fun userByOauthId(id: String) = findUser(UserEntity.OAUTH_ID.eq(id))

    override fun userByEmail(email: String) = findUser(UserEntity.EMAIL.eq(email))

    private fun <L, R> findUser(condition: Condition<L, R>): User? {
        return db.select(User::class).where(condition).get().firstOrNull()
    }

    ////////////////////////
    // Switches
    ////////////////////////
    override fun createSwitch(aSwitch: SwitchEntity): Int {
        return db.insert(aSwitch).id
    }

    override fun updateSwitch(aSwitch: Switch) {
        aSwitch.lastUpdate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
        db.update(aSwitch)
    }

    override fun switch(id: Int) = db.findByKey(Switch::class, id)

    override fun switches(): List<Switch> = db.select(Switch::class).get().toList()

    override fun switchBySchedule(scheduleId: Int) = findSwitch(SwitchEntity.SCHEDULE_ID.eq(scheduleId))

    override fun switchByGivenId(givenId: String) = findSwitch(SwitchEntity.GIVEN_ID.eq(givenId))

    private fun <L, R> findSwitch(condition: Condition<L, R>): Switch? {
        return db.select(Switch::class).where(condition).get().firstOrNull()
    }

    //////////////
    // Schedule
    //////////////
    override fun createSchedule(schedule: ScheduleEntity): Int {
        return db.insert(schedule).id
    }

    override fun updateSchedule(schedule: Schedule) {
        db.update(schedule)
    }

    override fun schedule(id: Int) = db.findByKey(Schedule::class, id)

    override fun schedules(): List<Schedule> = db.select(Schedule::class).get().toList()

    override fun scheduleStatus(): ScheduleStatus {
        return db.findByKey(ScheduleStatus::class, 1) ?: ScheduleStatusEntity().apply {
            id = 1
            status = "active"
            pausedUntilDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            db.insert(this)
        }
    }


    override fun updateScheduleStatus(status: ScheduleStatus) {
        db.update(status)
    }
}