package io.morrissey.iot.server.services

import io.morrissey.iot.server.model.AutomationState
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.model.AutomationStatusEnum.ACTIVE
import io.morrissey.iot.server.model.AutomationStatusEnum.PAUSED
import io.morrissey.iot.server.model.AutomationStatusEnum.STOPPED
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

abstract class ResumeDateHandler {
    abstract val dbProperty: KMutableProperty<String>
    private var isInitialized = false

    open operator fun getValue(automationState: AutomationState, property: KProperty<*>): String {
        return dbProperty.getter.call()
    }

    open operator fun setValue(automationState: AutomationState, property: KProperty<*>, newResumeDate: String) {
        if (isInitialized) {
            val existingPausedUntil = dbProperty.getter.call()
            if (existingPausedUntil != newResumeDate) {
                updateResumeDate(automationState, newResumeDate)
            }
        }
        dbProperty.setter.call(newResumeDate)
        isInitialized = true
    }

    abstract fun updateResumeDate(automationState: AutomationState, newResumeDate: String)
}

interface ResumeDateHandlerFactory {
    fun create(dbProperty: KMutableProperty<String>): ResumeDateHandler
}

abstract class AutomationStatusHandler {
    abstract val dbProperty: KMutableProperty<AutomationStatusEnum>
    private var isInitialized = false

    open operator fun getValue(automationState: AutomationState, property: KProperty<*>): AutomationStatusEnum {
        return dbProperty.getter.call()
    }

    open operator fun setValue(automationState: AutomationState, property: KProperty<*>, newStatus: AutomationStatusEnum) {
        if (isInitialized) {
            when (dbProperty.getter.call()) {
                PAUSED -> when (newStatus) {
                    ACTIVE -> {
                        enableAutomation(automationState)
                        disableResume(automationState)
                    }
                    STOPPED -> {
                        disableResume(automationState)
                    }
                    PAUSED -> {
                    }
                }
                STOPPED -> when (newStatus) {
                    ACTIVE -> {
                        enableAutomation(automationState)
                    }
                    STOPPED -> {
                    }
                    PAUSED -> {
                        enableResume(automationState)
                    }
                }
                ACTIVE -> when (newStatus) {
                    ACTIVE -> {
                    }
                    STOPPED -> {
                        disableAutomation(automationState)
                    }
                    PAUSED -> {
                        disableAutomation(automationState)
                        enableResume(automationState)
                    }
                }
            }
        }
        dbProperty.setter.call(newStatus)
        isInitialized = true
    }

    abstract fun disableAutomation(automationState: AutomationState)

    abstract fun enableAutomation(automationState: AutomationState)

    abstract fun enableResume(automationState: AutomationState)

    abstract fun disableResume(automationState: AutomationState)
}

interface AutomationStatusHandlerFactory {
    fun create(dbProperty: KMutableProperty<AutomationStatusEnum>): AutomationStatusHandler
}
