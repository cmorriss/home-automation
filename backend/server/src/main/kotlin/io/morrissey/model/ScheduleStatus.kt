package io.morrissey.model

data class ScheduleStatus(
    val status: String,
    val pausedUntilDate: String
)