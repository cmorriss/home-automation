package io.morrissey.iot.server.model

import io.morrissey.iot.server.log
import io.morrissey.iot.server.services.AutomationStatusHandler
import io.morrissey.iot.server.services.ResumeDateHandler
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinApiExtension
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject

object Automations : IntIdTable("automation") {
    val eventId = integer("event_id")
    val cron = varchar("cron", 255)
    val eventType = enumeration("event_type", EventType::class)
    val actionId = integer("action_id")
    val actionType = enumeration("action_type", ActionType::class)
    val associatedAutomationId = integer("associated_automation_id")
    val status = enumeration("status", AutomationStatusEnum::class)
    val resumeDate = varchar("resume_date", 255)
    val name = varchar("name", 255)
}

enum class ActionType { CONTROL, AUTOMATION, AUTOMATION_GROUP }

enum class EventType { SCHEDULE, THRESHOLD }

@OptIn(KoinApiExtension::class)
class Automation(
    id: EntityID<Int>,
) : TransferableEntity<AutomationDto>(id), AutomationState {
    companion object : IntEntityClass<Automation>(Automations, Automation::class.java)

    var eventId by Automations.eventId
    var cron by Automations.cron
    var eventType by Automations.eventType
    var actionId by Automations.actionId
    var actionType by Automations.actionType
    var associatedAutomationId by Automations.associatedAutomationId
    var _status by Automations.status
    private val automationStatusHandler by inject(AutomationStatusHandler::class.java) { parametersOf(::_status) }
    override var status by automationStatusHandler
    var _resumeDate by Automations.resumeDate
    private val resumeDateHandler by inject(ResumeDateHandler::class.java) { parametersOf(::_resumeDate) }
    override var resumeDate by resumeDateHandler
    override var name by Automations.name
    override val automations: List<Automation>
        get() = listOf(this)

    override fun toDto(): AutomationDto {
        return transaction {
            val (time, daysOfWeek, dateTime) = convertFromCron(cron)
            AutomationDto(
                id.value,
                eventId,
                time,
                daysOfWeek,
                dateTime,
                eventType,
                actionId,
                actionType,
                associatedAutomationId,
                status,
                resumeDate
            )
        }
    }
}

data class AutomationDto(
    override val id: Int,
    val eventId: Int,
    val time: String,
    val daysOfTheWeek: Set<CronDayOfWeek>,
    val dateTime: String,
    val eventType: EventType,
    val actionId: Int,
    val actionType: ActionType,
    val associatedAutomationId: Int,
    val status: AutomationStatusEnum,
    val resumeDate: String
) : EntityDto<Automation>() {

    override fun create(): Automation {
        return transaction {
            Automation.new {
                eventId = this@AutomationDto.eventId
                cron = convertToCron(
                    this@AutomationDto.time, this@AutomationDto.daysOfTheWeek, this@AutomationDto.dateTime
                )
                eventType = this@AutomationDto.eventType
                actionId = this@AutomationDto.actionId
                actionType = this@AutomationDto.actionType
                associatedAutomationId = this@AutomationDto.associatedAutomationId
                status = this@AutomationDto.status
                resumeDate = this@AutomationDto.resumeDate
            }
        }
    }

    override fun update(): Automation {
        log.info("Updating auotmation deto with id: $id")
        return transaction {
            Automation[id].apply {
                eventId = this@AutomationDto.eventId
                cron = convertToCron(
                    this@AutomationDto.time, this@AutomationDto.daysOfTheWeek, this@AutomationDto.dateTime
                )
                eventType = this@AutomationDto.eventType
                actionId = this@AutomationDto.actionId
                actionType = this@AutomationDto.actionType
                associatedAutomationId = this@AutomationDto.associatedAutomationId
                status = this@AutomationDto.status
                resumeDate = this@AutomationDto.resumeDate
            }
        }
    }
}

enum class CronDayOfWeek {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    fun inc(): CronDayOfWeek {
        return if (ordinal < values().size - 1) {
            values()[ordinal + 1]
        } else {
            values().first()
        }
    }

    fun dec(): CronDayOfWeek {
        return if (ordinal > 0) {
            values()[ordinal - 1]
        } else {
            values().last()
        }
    }
}
