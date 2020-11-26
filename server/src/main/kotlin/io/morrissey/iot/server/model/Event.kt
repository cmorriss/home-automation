package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

abstract class Events(tableName: String) : IntIdTable(tableName)

abstract class Event<E : EntityDto<*>>(id: EntityID<Int>) : TransferableEntity<E>(id)

abstract class EventDto<E : IntEntity> : EntityDto<E>()
