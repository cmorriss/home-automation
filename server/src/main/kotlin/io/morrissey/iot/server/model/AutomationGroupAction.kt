package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

object AutomationGroupActions : Actions("automation_group_action") {
    val automationGroup = reference("automation_group_id", AutomationGroups)
    val status = enumerationByName("status", 128, AutomationStatusEnum::class)
    val pausedUntil = varchar("paused_until_date", 255)
}

class AutomationGroupAction(id: EntityID<Int>) : Action<AutomationGroupActionDto>(id) {
    companion object : IntEntityClass<AutomationGroupAction>(AutomationGroupActions, AutomationGroupAction::class.java)

    var automationGroup by AutomationGroup referencedOn AutomationGroupActions.automationGroup
    var status by AutomationGroupActions.status
    var pausedUntil by AutomationGroupActions.pausedUntil

    override fun toDto(): AutomationGroupActionDto {
        return transaction {
            AutomationGroupActionDto(id.value, automationGroup.toDto(), status, pausedUntil)
        }
    }
}

data class AutomationGroupActionDto(
    override val id: Int, val automationGroup: AutomationGroupDto, val status: AutomationStatusEnum, val pausedUntil: String
) : ActionDto<AutomationGroupAction>() {
    override fun create(): AutomationGroupAction {
        return transaction {
            AutomationGroupAction.new {
                automationGroup = this@AutomationGroupActionDto.automationGroup.create()
                status = this@AutomationGroupActionDto.status
                pausedUntil = this@AutomationGroupActionDto.pausedUntil
            }
        }
    }

    override fun update(): AutomationGroupAction {
        return transaction {
            AutomationGroupAction[id].apply {
                automationGroup = this@AutomationGroupActionDto.automationGroup.update()
                status = this@AutomationGroupActionDto.status
                pausedUntil = this@AutomationGroupActionDto.pausedUntil
            }
        }
    }
}
