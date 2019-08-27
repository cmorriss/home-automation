package io.morrissey.persistence

import io.morrissey.model.Switch
import io.morrissey.model.Schedule
import io.morrissey.model.ScheduleStatus
import io.morrissey.model.User


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
    fun createSwitch(aSwitch: Switch): Int

    fun updateSwitch(aSwitch: Switch)

    fun switch(id: Int): Switch?

    fun switches(): List<Switch>

    fun switchBySchedule(scheduleId: Int): Switch?

    //////////////////
    // SchedulesRoute
    //////////////////
    fun createSchedule(schedule: Schedule): Int

    fun updateSchedule(schedule: Schedule)

    fun schedule(id: Int): Schedule?

    fun schedules(): List<Schedule>

    ///////////////////
    // Schedule Status
    ///////////////////
    fun scheduleStatus(): ScheduleStatus

    fun updateScheduleStatus(status: ScheduleStatus)
}