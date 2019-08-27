package io.morrissey.model

import com.pi4j.io.gpio.GpioPinDigitalOutput

data class PhysicalSwitch(
    val id: Int = NO_ID,
    val pin: GpioPinDigitalOutput? = null,
    val on: Boolean = false
)