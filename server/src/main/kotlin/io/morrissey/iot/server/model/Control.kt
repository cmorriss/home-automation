package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager

object Controls : RemoteThings<Control>("control") {
    val lastUpdate = varchar("last_update", 128)
    val type = enumerationByName("type", 128, ControlType::class)
    val state = enumerationByName("state", 128, ControlState::class)
    val stateFidelity = enumerationByName("state_fidelity", 128, ControlStateFidelity::class)
}

class Control(id: EntityID<Int>) : RemoteThing<ControlDto>(id) {
    companion object : IntEntityClass<Control>(Controls, Control::class.java)

    override var thingName by Controls.thingName
    override var name by Controls.name
    var lastUpdate by Controls.lastUpdate
    var type by Controls.type
    var state by Controls.state
    var stateFidelity by Controls.stateFidelity

    override fun toDto(): ControlDto {
        return transaction {
            ControlDto(
                id.value, thingName, name, lastUpdate, type, state, stateFidelity
            )
        }
    }
}

enum class ControlType { IRRIGATION_VALVE, LIGHT_SWITCH }

enum class ControlState { ON, OFF }

enum class ControlStateFidelity { LOW, MEDIUM, HIGH }

data class ControlDto(
    override val id: Int,
    override val thingName: String,
    override val name: String,
    val lastUpdate: String,
    val type: ControlType,
    val state: ControlState,
    val stateFidelity: ControlStateFidelity
) : RemoteThingDto<Control>() {

    override fun create(): Control {
        return transaction {
            Control.new {
                thingName = this@ControlDto.thingName
                name = this@ControlDto.name
                lastUpdate = this@ControlDto.lastUpdate
                type = this@ControlDto.type
                state = this@ControlDto.state
                stateFidelity = this@ControlDto.stateFidelity
            }
        }
    }

    override fun update(): Control {
        return transaction {
            Control[id].apply {
                thingName = this@ControlDto.thingName
                name = this@ControlDto.name
                lastUpdate = this@ControlDto.lastUpdate
                type = this@ControlDto.type
                state = this@ControlDto.state
            }
        }
    }
}
