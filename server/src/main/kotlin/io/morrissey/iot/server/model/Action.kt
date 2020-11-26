package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

abstract class Actions(tableName: String) : IntIdTable(tableName)

abstract class Action<D : EntityDto<*>>(id: EntityID<Int>) : TransferableEntity<D>(id)

abstract class ActionDto<A : Action<*>> : EntityDto<A>()
