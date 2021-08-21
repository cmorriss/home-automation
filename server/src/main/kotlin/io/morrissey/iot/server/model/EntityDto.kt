package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity

abstract class TransferableEntity<D : EntityDto<*>>(id: EntityID<Int>) : IntEntity(id) {
    abstract fun toDto(): D
}

abstract class EntityDto<E : IntEntity> {
    abstract val id: Int
    open fun update(): E {
        throw NotImplementedError("The update implementation for this entity is not implemented.")
    }

    open fun create(): E {
        throw NotImplementedError("The create implementation for this entity is not implemented.")
    }

    open fun delete() {
        throw NotImplementedError("The delete implementation for this entity is not implemented.")
    }
}
