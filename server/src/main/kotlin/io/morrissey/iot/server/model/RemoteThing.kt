package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntIdTable

abstract class RemoteThings<R : RemoteThing<*>>(name: String) : IntIdTable(name) {
    var thingName = varchar("given_id", 255)
    var name = varchar("name", 255)
}

abstract class RemoteThing<D : EntityDto<*>>(id: EntityID<Int>) : TransferableEntity<D>(id) {
    abstract var thingName: String
    abstract var name: String
}

abstract class RemoteThingDto<R : RemoteThing<*>> : EntityDto<R>() {
    abstract val givenId: String
    abstract val name: String
}

