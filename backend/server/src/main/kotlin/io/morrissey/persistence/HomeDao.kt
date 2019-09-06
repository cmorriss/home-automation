package io.morrissey.persistence

import io.morrissey.model.*


interface HomeDao {
    ////////////////
    // User
    ////////////////
    fun createUser(user: User): Int

    fun updateUser(user: User)

    fun user(id: Int): User?

    fun userByOauthId(id: String): User?

    fun userByEmail(email: String): User?

    /////////////////////
    // Switches
    /////////////////////
    fun createSwitch(aSwitch: SwitchEntity): Int

    fun updateSwitch(aSwitch: Switch)

    fun switch(id: Int): Switch?

    fun switches(): List<Switch>

    fun switchBySchedule(scheduleId: Int): Switch?

    fun switchByGivenId(givenId: String): Switch?

    //////////////////
    // SchedulesRoute
    //////////////////
    fun createSchedule(schedule: ScheduleEntity): Int

    fun updateSchedule(schedule: Schedule)

    fun schedule(id: Int): Schedule?

    fun schedules(): List<Schedule>

    ///////////////////
    // Schedule Status
    ///////////////////
    fun scheduleStatus(): ScheduleStatus

    fun updateScheduleStatus(status: ScheduleStatus)
}