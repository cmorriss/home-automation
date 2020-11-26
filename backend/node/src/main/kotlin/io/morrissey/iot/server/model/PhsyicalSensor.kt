package io.morrissey.iot.server.model

import com.pi4j.io.gpio.*


data class PhysicalSensor(
    override val id: String,
    override val pin: Int,
    val gpioPin: GpioPinDigitalInput?,
    val hasValue: Boolean = false
) : PhysicalItem() {
    fun toDto(): SensorDto {
        val state = if (hasValue) {
            SensorState(value = gpioPin?.state?.value)
        } else {
            SensorState(
                high = gpioPin?.state?.isHigh, low = gpioPin?.state?.isLow
            )
        }
        return SensorDto(id, state)
    }
}