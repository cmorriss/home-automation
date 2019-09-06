package io.morrissey.model

import io.requery.*

@Entity
interface ScheduleStatus : Persistable {
    @get:Key
    var id: Int
    var status: String
    var pausedUntilDate: String
}

class ScheduleStatusDto {
    var id: Int = 1
    var status: String = ""
    var pausedUntilDate: String = ""
}