package io.morrissey.iot.server.model

data class ControlDto(val id: String, val state: ControlState)

enum class ControlState { ON, OFF }