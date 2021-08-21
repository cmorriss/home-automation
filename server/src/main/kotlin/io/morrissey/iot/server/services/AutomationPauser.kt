package io.morrissey.iot.server.services

import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.AutomationState
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.model.AutomationStatusEnum.ACTIVE
import io.morrissey.iot.server.model.AutomationStatusEnum.PAUSED
import io.morrissey.iot.server.model.AutomationStatusEnum.STOPPED
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

abstract class ResumeDateHandler {
    abstract val dbProperty: KMutableProperty<String>

    open operator fun getValue(automationState: AutomationState, property: KProperty<*>): String {
        return dbProperty.getter.call()
    }

    open operator fun setValue(automationState: AutomationState, property: KProperty<*>, newResumeDate: String) {
        try {
            val existingPausedUntil = dbProperty.getter.call()
            if (existingPausedUntil != newResumeDate) {
                updateResumeDate(automationState, newResumeDate)
            }
        } catch (e: NullPointerException) {
            // When creating a new database instance, there won't be an existing value which causes an NPE when calling the
            // getter. I can't seem to find a way around just catching the NPE in this case.
        }
        dbProperty.setter.call(newResumeDate)
    }

    abstract fun updateResumeDate(automationState: AutomationState, newResumeDate: String)
}

abstract class AutomationStatusHandler {
    abstract val dbProperty: KMutableProperty<AutomationStatusEnum>

    open operator fun getValue(automationState: AutomationState, property: KProperty<*>): AutomationStatusEnum {
        return dbProperty.getter.call()
    }

    open operator fun setValue(automationState: AutomationState, property: KProperty<*>, newStatus: AutomationStatusEnum) {
        log.info("Received call to set status on ${automationState.name} to $newStatus")

        try {
            val existingStatus = dbProperty.getter.call()
            log.info("Setting the automation status value from $existingStatus to $newStatus")
            when (existingStatus) {
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
        } catch (e: NullPointerException) {
            // When creating a new database instance, there won't be an existing value which causes an NPE when calling the
            // getter. I can't seem to find a way around just catching the NPE in this case.
        }
        dbProperty.setter.call(newStatus)
    }

    abstract fun disableAutomation(automationState: AutomationState)

    abstract fun enableAutomation(automationState: AutomationState)

    abstract fun enableResume(automationState: AutomationState)

    abstract fun disableResume(automationState: AutomationState)
}
