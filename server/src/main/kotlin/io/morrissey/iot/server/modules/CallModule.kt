package io.morrissey.iot.server.modules

import com.google.inject.AbstractModule
import io.ktor.application.ApplicationCall

// A module for each call
class CallModule(private val call: ApplicationCall) : AbstractModule() {
    override fun configure() {
        bind(ApplicationCall::class.java).toInstance(call)
    }
}