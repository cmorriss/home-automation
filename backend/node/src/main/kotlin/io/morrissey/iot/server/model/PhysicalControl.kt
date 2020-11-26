package io.morrissey.iot.server.model

import com.pi4j.io.gpio.GpioPinDigitalOutput

data class PhysicalControl(
    override val id: String,
    override val pin: Int,
    val gpioPin: GpioPinDigitalOutput? = null,
    val state: ControlState = ControlState.OFF
) : PhysicalItem()