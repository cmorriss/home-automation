package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

abstract class Groups<I : IntEntity, G : Group<I, *>>(tableName: String) : IntIdTable(tableName) {
    var name = varchar("name", 255)
    var itemIds = text("item_ids")
}

abstract class Group<I : IntEntity, D : EntityDto<*>>(id: EntityID<Int>) : TransferableEntity<D>(id) {
    abstract val name: String
    abstract val items: List<I>
}

abstract class GroupDto<I : EntityDto<*>, G : IntEntity> : EntityDto<G>() {
    abstract val name: String
    abstract val items: List<I>
}
