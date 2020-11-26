package io.morrissey.iot.server.model

data class SensorDto(val id: String, val state: SensorState? = null)

data class SensorState(val high: Boolean? = null, val low: Boolean? = null, val value: Int? = null)