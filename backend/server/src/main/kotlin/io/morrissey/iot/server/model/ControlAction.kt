package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

object ControlActions : Actions("control_action") {
    val control = reference("control_id", Controls)
    val state = enumerationByName("state", 128, ControlState::class)
}

class ControlAction(id: EntityID<Int>) : Action<ControlActionDto>(id) {
    companion object : IntEntityClass<ControlAction>(ControlActions, ControlAction::class.java)

    var control by Control referencedOn ControlActions.control
    var state by ControlActions.state

    override fun toDto(): ControlActionDto {
        return transaction {
            ControlActionDto(id.value, control.toDto(), state)
        }
    }
}

data class ControlActionDto(
    override val id: Int, val control: ControlDto, val state: ControlState
) : ActionDto<ControlAction>() {
    override fun create(): ControlAction {
        return transaction {
            ControlAction.new {
                control = this@ControlActionDto.control.create()
                state = this@ControlActionDto.state
            }
        }
    }

    override fun update(): ControlAction {
        return transaction {
            ControlAction[id].apply {
                control = this@ControlActionDto.control.update()
                state = this@ControlActionDto.state
            }
        }
    }
}
