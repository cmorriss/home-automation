package io.morrissey.iot.server.model

import com.google.inject.Injector
import io.morrissey.iot.server.log
import io.morrissey.iot.server.services.AutomationStatusHandlerFactory
import io.morrissey.iot.server.services.ResumeDateHandlerFactory
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

object Automations : IntIdTable("automation") {
    val eventId = integer("event_id")
    val eventType = enumeration("event_type", EventType::class)
    val actionId = integer("action_id")
    val actionType = enumeration("action_type", ActionType::class)
    val associatedAutomationId = integer("associated_automation_id")
    val status = enumeration("status", AutomationStatusEnum::class)
    val resumeDate = varchar("resume_date", 255)
    val name = varchar("name", 255)
}

enum class ActionType { CONTROL, SCHEDULE, AUTOMATION_GROUP }

enum class EventType { SCHEDULE, THRESHOLD }

class Automation(
    id: EntityID<Int>,
    automationStatusHandlerFactory: AutomationStatusHandlerFactory,
    resumeDateHandlerFactory: ResumeDateHandlerFactory
) : TransferableEntity<AutomationDto>(id), AutomationState {
    companion object : IntEntityClass<Automation>(Automations, Automation::class.java) {
        @Inject
        private lateinit var injector: Injector

        override fun createInstance(entityId: EntityID<Int>, row: ResultRow?) : Automation {
            return Automation(
                entityId,
                injector.getInstance(AutomationStatusHandlerFactory::class.java),
                injector.getInstance(ResumeDateHandlerFactory::class.java)
            )
        }
    }

    var eventId by Automations.eventId
    var eventType by Automations.eventType
    var actionId by Automations.actionId
    var actionType by Automations.actionType
    var associatedAutomationId by Automations.associatedAutomationId
    var _status by Automations.status
    override var status by automationStatusHandlerFactory.create(::_status)
    var _resumeDate by Automations.resumeDate
    override var resumeDate by resumeDateHandlerFactory.create(::_resumeDate)
    override var name by Automations.name
    override val automations: List<Automation>
        get() = listOf(this)

    override fun toDto(): AutomationDto {
        return transaction {
            AutomationDto(
                id.value, eventId, eventType, actionId, actionType, associatedAutomationId, status, resumeDate
            )
        }
    }
}

data class AutomationDto(
    override val id: Int,
    val eventId: Int,
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
