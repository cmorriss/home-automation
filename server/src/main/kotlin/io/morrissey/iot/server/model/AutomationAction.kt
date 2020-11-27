package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

object AutomationActions : Actions("automation_action") {
    val automation = reference("automation_id", Automations)
    val enabled = bool("enabled")
}

class AutomationAction(id: EntityID<Int>) : Action<AutomationActionDto>(id) {
    companion object : IntEntityClass<AutomationAction>(AutomationActions, AutomationAction::class.java)

    var automation by Automation referencedOn AutomationActions.automation
    var enabled by AutomationActions.enabled

    override fun toDto(): AutomationActionDto {
        return transaction {
            AutomationActionDto(id.value, automation.toDto(), enabled)
        }
    }
}

data class AutomationActionDto(
    override val id: Int, val automation: AutomationDto, val enabled: Boolean
) : ActionDto<AutomationAction>() {
    override fun create(): AutomationAction {
        return transaction {
            AutomationAction.new {
                automation = this@AutomationActionDto.automation.create()
                enabled = this@AutomationActionDto.enabled
            }
        }
    }

    override fun update(): AutomationAction {
        return transaction {
            AutomationAction[id].apply {
                automation = this@AutomationActionDto.automation.update()
                enabled = this@AutomationActionDto.enabled
            }
        }
    }
}
