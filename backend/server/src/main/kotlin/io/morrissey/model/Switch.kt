package io.morrissey.model

import io.morrissey.model.IotLocation.*
import io.morrissey.model.LocationStatus.UNKNOWN
import io.morrissey.model.SwitchKind.*
import io.requery.*
import io.requery.ReferentialAction.*

@Entity
interface Switch : Persistable {
    @get:Key
    @get:Generated
    val id: Int
    var givenId: String
    var name: String
    var kind: SwitchKind
    var location: IotLocation
    var locationId: Int
    var locationStatus: LocationStatus
    var locationStatusMessage: String
    @get:Column(value = "false", name = "_onState")
    var on: Boolean
    var lastUpdate: String
    @get:ForeignKey(delete = CASCADE, update = CASCADE, references = Schedule::class)
    @get:OneToOne
    var schedule: Schedule
}

class SwitchDto {
    var id: Int = -1
    var givenId: String = ""
    var name: String = ""
    var kind: SwitchKind = IRRIGATION_VALVE
    var location: IotLocation = Frontyard
    var locationId: Int = -1
    var locationStatus: LocationStatus = UNKNOWN
    var locationStatusMessage: String = ""
    var on: Boolean = false
    var lastUpdate: String = ""
    var schedule: ScheduleDto = ScheduleDto()
}

enum class SwitchKind { IRRIGATION_VALVE, LIGHT_SWITCH }

enum class IotLocation(val apiUrl: String) {
    Backyard("http://backyard:8040/api"), Frontyard("http://frontyard:8040/api")
}

enum class LocationStatus {
    OK, ERROR, DISCONNECTED, UNKNOWN
}