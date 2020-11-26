package io.morrissey.iot.server.aws

import com.google.inject.assistedinject.Assisted
import io.morrissey.iot.server.model.AutomationState
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.model.convertToCron
import io.morrissey.iot.server.services.AutomationStatusHandler
import io.morrissey.iot.server.services.ResumeDateHandler
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient
import javax.inject.Inject
import kotlin.reflect.KMutableProperty

class AwsResumeDateHandler @Inject constructor(
    @Assisted override val dbProperty: KMutableProperty<String>,
    private val cwClient: CloudWatchEventsClient
) : ResumeDateHandler() {
    override fun updateResumeDate(automationState: AutomationState, newResumeDate: String) {
        val existingRule = cwClient.listRules {
            it.namePrefix(automationState.name)
        }.rules().singleOrNull { it.name() == automationState.name }
        if (existingRule != null) {
            cwClient.putRule {
                it.name(automationState.name)
                it.scheduleExpression("cron(${convertToCron(newResumeDate)})")
            }
        }
    }
}

class AwsAutomationStatusHandler @Inject constructor(
    @Assisted override val dbProperty: KMutableProperty<AutomationStatusEnum>,
    private val cwClient: CloudWatchEventsClient,
    private val synchronizer: AutomationSynchronizer
) : AutomationStatusHandler() {
    override fun enableAutomation(automationState: AutomationState) {
        automationState.automations.forEach { automation ->
            synchronizer.setEnabled(automation.eventId, true)
        }
    }

    override fun disableAutomation(automationState: AutomationState) {
        automationState.automations.forEach { automation ->
            synchronizer.setEnabled(automation.eventId, false)
        }
    }

    override fun enableResume(automationState: AutomationState) {
        automationState.automations.forEach { automation ->

        }
    }

    override fun disableResume(automationState: AutomationState) {

    }
}
