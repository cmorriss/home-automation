package io.morrissey.iot.server.model

import io.morrissey.iot.server.log
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

object ControlGroups : Groups<Control, ControlGroup>("control_group")

class ControlGroup(id: EntityID<Int>) : RemoteThingGroup<Control, ControlGroup, ControlGroupDto>(id) {
    companion object : IntEntityClass<ControlGroup>(ControlGroups, ControlGroup::class.java)

    override var name by ControlGroups.name
    override var items: List<Control> by ControlGroups.itemIds.transform({ controls ->
                                                                             controls.joinToString(":") {
                                                                                 it.id.toString()
                                                                             }
                                                                         }, { itemsString ->
                                                                             itemsString.split(":")
                                                                                 .filter { it.isNotBlank() }
                                                                                 .map(String::toInt)
                                                                                 .map { Control.findById(it)!! }
                                                                         })

    override fun toDto(): ControlGroupDto {
        return transaction {
            ControlGroupDto(
                id.value,
                name,
                items.map { it.toDto() })
        }
    }
}

data class ControlGroupDto(
    override val id: Int, override val name: String, override val items: List<ControlDto>
) : RemoteThingGroupDto<ControlDto, ControlGroup>() {
    override fun create(): ControlGroup {
        log.info("Creating control group with name $name, items size is ${items.size}")
        return transaction {
            ControlGroup.new {
                name = this@ControlGroupDto.name
                items = this@ControlGroupDto.items.map { Control[it.id] }
            }
        }
    }

    override fun update(): ControlGroup {
        log.info("Updating control group with name $name, items size is ${items.size}")
        return transaction {
            ControlGroup[this@ControlGroupDto.id].apply {
                name = this@ControlGroupDto.name
                items = this@ControlGroupDto.items.map { Control[it.id] }
            }
        }
    }
}
