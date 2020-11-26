package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.DayOfWeek

object Schedules : Events("schedule") {
    val cron = text("cron")
}

class Schedule(id: EntityID<Int>) : Event<ScheduleDto>(id) {
    companion object : IntEntityClass<Schedule>(Schedules, Schedule::class.java)

    var cron: String by Schedules.cron

    override fun toDto(): ScheduleDto {
        return transaction {
            val (time, daysOfWeek, dateTime) = convertFromCron(cron)
            ScheduleDto(
                id.value, time, daysOfWeek, dateTime
            )
        }
    }
}

data class ScheduleDto(
    override val id: Int, val time: String, val daysOfTheWeek: Set<DayOfWeek>, val dateTime: String
) : EventDto<Schedule>() {
    override fun update(): Schedule {
        return transaction {
            Schedule[id].apply {
                cron = convertToCron(time, daysOfTheWeek, dateTime)
            }
        }
    }

    override fun create(): Schedule {
        return Schedule.new {
            cron = convertToCron(this@ScheduleDto.time, this@ScheduleDto.daysOfTheWeek, this@ScheduleDto.dateTime)
        }
    }
}
