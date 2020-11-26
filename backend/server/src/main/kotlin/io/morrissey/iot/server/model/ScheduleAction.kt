package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

object ScheduleActions : Actions("schedule_action") {
    val schedule = reference("schedule_id", Schedules)
    val enabled = bool("enabled")
}

class ScheduleAction(id: EntityID<Int>) : Action<ScheduleActionDto>(id) {
    companion object : IntEntityClass<ScheduleAction>(ScheduleActions, ScheduleAction::class.java)

    var schedule by Schedule referencedOn ScheduleActions.schedule
    var enabled by ScheduleActions.enabled

    override fun toDto(): ScheduleActionDto {
        return transaction {
            ScheduleActionDto(id.value, schedule.toDto(), enabled)
        }
    }
}

data class ScheduleActionDto(
    override val id: Int, val control: ScheduleDto, val enabled: Boolean
) : ActionDto<ScheduleAction>() {
    override fun create(): ScheduleAction {
        return transaction {
            ScheduleAction.new {
                schedule = this@ScheduleActionDto.control.create()
                enabled = this@ScheduleActionDto.enabled
            }
        }
    }

    override fun update(): ScheduleAction {
        return transaction {
            ScheduleAction[id].apply {
                schedule = this@ScheduleActionDto.control.update()
                enabled = this@ScheduleActionDto.enabled
            }
        }
    }
}
