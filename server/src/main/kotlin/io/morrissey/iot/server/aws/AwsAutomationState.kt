package io.morrissey.iot.server.aws

import com.google.inject.assistedinject.Assisted
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.AutomationState
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.model.convertToCron
import io.morrissey.iot.server.services.AutomationStatusHandler
import io.morrissey.iot.server.services.ResumeDateHandler
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import javax.inject.Inject
import kotlin.reflect.KMutableProperty

class AwsResumeDateHandler @Inject constructor(
    @Assisted override val dbProperty: KMutableProperty<String>,
    private val ebClient: EventBridgeClient
) : ResumeDateHandler() {
    override fun updateResumeDate(automationState: AutomationState, newResumeDate: String) {
//        val existingRule = ebClient.listRules {
//            it.namePrefix(automationState.name)
//        }.rules().singleOrNull { it.name() == automationState.name }
//        if (existingRule != null) {
//            ebClient.putRule {
//                it.name(automationState.name)
//                it.scheduleExpression("cron(${convertToCron(newResumeDate)})")
//            }
//        }
    }
}

class AwsAutomationStatusHandler @Inject constructor(
    @Assisted override val dbProperty: KMutableProperty<AutomationStatusEnum>,
    private val ebClient: EventBridgeClient,
    private val synchronizer: AutomationSynchronizer
) : AutomationStatusHandler() {
    override fun enableAutomation(automationState: AutomationState) {
        log.info("Enabling automation for automation group ${automationState.name}")
        automationState.automations.forEach { automation ->
            synchronizer.setEnabled(automation, true)
        }
    }

    override fun disableAutomation(automationState: AutomationState) {
        log.info("Disabling automation for automation group ${automationState.name}")
        automationState.automations.forEach { automation ->
            synchronizer.setEnabled(automation, false)
        }
    }

    override fun enableResume(automationState: AutomationState) {
        automationState.automations.forEach { automation ->

        }
    }

    override fun disableResume(automationState: AutomationState) {

    }
}
