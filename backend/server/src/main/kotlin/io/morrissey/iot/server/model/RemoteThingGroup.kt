package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity

abstract class RemoteThingGroup<R : RemoteThing<*>, G : IntEntity, D : RemoteThingGroupDto<*, *>>(id: EntityID<Int>) : Group<R, D>(id)

abstract class RemoteThingGroupDto<R : RemoteThingDto<*>, G :IntEntity> : GroupDto<R, G>()
