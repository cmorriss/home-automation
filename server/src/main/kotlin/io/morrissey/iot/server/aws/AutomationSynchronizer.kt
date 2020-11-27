package io.morrissey.iot.server.aws

import com.cronutils.converter.CronConverter
import io.morrissey.iot.server.HomeServerConfig
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.ActionType.AUTOMATION
import io.morrissey.iot.server.model.ActionType.AUTOMATION_GROUP
import io.morrissey.iot.server.model.ActionType.CONTROL
import io.morrissey.iot.server.model.Automation
import io.morrissey.iot.server.model.Control
import io.morrissey.iot.server.model.ControlAction
import io.morrissey.iot.server.model.CronDayOfWeek
import io.morrissey.iot.server.model.DAY_OF_WEEK_FIELD
import io.morrissey.iot.server.model.HOUR_FIELD
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import software.amazon.awssdk.services.eventbridge.model.ListRulesRequest
import software.amazon.awssdk.services.eventbridge.model.Rule
import software.amazon.awssdk.services.eventbridge.model.Target
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutomationSynchronizer @Inject constructor(
    private val ebClient: EventBridgeClient, private val homeServerConfig: HomeServerConfig
) {
    fun synchronize(automationId: Int) {
        transaction {
            val automation = Automation[automationId]
            val existingRule = getRule(automation)
            if (existingRule != null) {
                push(automation, existingRule)
            } else {
                create(automation)
            }
        }
    }

    fun remove(automation: Automation) {
        val ruleName = automation.toCloudWatchRuleName()
        log.info("Removing automation with rule name $ruleName from cloud watch.")
        val target = findExistingEventTarget(ruleName)
        ebClient.removeTargets {
            it.rule(ruleName)
            it.ids(target.id())
        }
        ebClient.deleteRule {
            it.name(ruleName)
        }
    }

    fun create(automation: Automation) {
        val ruleName = automation.toCloudWatchRuleName()
        log.info("Creating schedule with rule name $ruleName in cloud watch.")
        val eventTarget = buildEventTarget(automation, homeServerConfig)

        ebClient.putRule {
            it.name(ruleName)
            val cronExpression = automation.cron.toCloudWatchExpression()
            log.info("Sending the following cron expression: $cronExpression")
            it.scheduleExpression(cronExpression)
        }
        ebClient.putTargets {
            it.rule(ruleName)
            it.targets(eventTarget)
        }
    }

    fun pull(automation: Automation) {
        log.info("pulling automation info for automation ${automation.id.value}")
        val rule = getRule(automation)
        if (rule != null) {
            automation.cron = rule.scheduleExpression().fromCloudWatchExpression()
        } else {
            log.warn("Attempted to pull the schedule with rule name ${automation.toCloudWatchRuleName()} but it was not found!")
        }
    }

    fun push(automation: Automation, rule: Rule) {
        log.info("Pushing automation with rule name ${rule.name()} to cloud watch.")
        updateAutomation(automation, rule)
    }

    fun setEnabled(automation: Automation, enabled: Boolean) {
        transaction {
            val rule = getRule(automation)
            if (rule != null) {
                if (enabled) {
                    ebClient.enableRule {
                        it.name(rule.name())
                    }
                } else {
                    ebClient.disableRule {
                        it.name(rule.name())
                    }
                }
            } else {
                throw IllegalStateException("The Event Bridge rule associated with automation id ${automation.id.value} could not be found.")
            }
        }
    }

    fun removeAll() {
        val request = ListRulesRequest.builder().build()
        ebClient.listRules(request).rules().forEach { rule ->
            val ruleName = rule.name()
            val target = findExistingEventTarget(ruleName)
            ebClient.removeTargets {
                it.rule(ruleName)
                it.ids(target.id())
            }
            ebClient.deleteRule {
                it.name(ruleName)
            }
        }
    }

    private fun getRule(automation: Automation): Rule? {
        val scheduleRuleName = automation.toEventBridgeRuleName()
        return ebClient.listRules {
            it.namePrefix(scheduleRuleName)
        }.rules().singleOrNull { it.name() == scheduleRuleName }
    }

    private fun updateAutomation(automation: Automation, rule: Rule) {
        val eventTarget = buildEventTarget(automation, homeServerConfig)
        val cronExpression = automation.cron.toCloudWatchExpression()
        if (rule.scheduleExpression() != cronExpression) {
            ebClient.putRule {
                it.name(rule.name())
                it.scheduleExpression(cronExpression)
            }
        }
        ebClient.putTargets {
            it.rule(rule.name())
            it.targets(eventTarget)
        }
    }

    private fun findTargetsToUpdate(
        eventTargets: Map<String, Target>, existingEventTargets: Map<String, Target>
    ): Collection<Target> {
        return eventTargets.filter { (id, target) ->
            val existingTarget = existingEventTargets[id]
                ?: throw RuntimeException("Unable to locate existing event target with id $id")
            existingTarget.input() != target.input() || existingTarget.arn() != target.arn()
        }.values
    }

    private fun findExistingEventTarget(ruleName: String) =
        ebClient.listTargetsByRule { it.rule(ruleName) }.targets().singleOrNull()
            ?: throw IllegalStateException("The rule $ruleName has multiple targets. Only a single target is currently supported.")

    private fun buildEventTarget(automation: Automation, homeServerConfig: HomeServerConfig): Target {
        log.debug("Looking for target events for automation id ${automation.id.value}")
        return when (automation.actionType) {
            CONTROL -> {
                val controlAction = ControlAction[automation.actionId]
                log.debug("Found this control, ${controlAction.control.name} action and adding control to list of targets")
                log.debug("Setting target to ${controlAction.toTarget(homeServerConfig)}")
                controlAction.toTarget(
                    homeServerConfig
                )
            }
            AUTOMATION -> {
                throw UnsupportedOperationException("Automation actions are not yet supported")
            }
            AUTOMATION_GROUP -> {
                throw UnsupportedOperationException("Automation group actions are not yet supported")
            }
        }
    }

    private fun String.fromCloudWatchExpression() =
        substringAfter("cron(").substring(0, length - "cron()".length).convertCron(GMT_TIME_ZONE, LOCAL_TIME_ZONE)

    private fun String.toCloudWatchExpression() = "cron(${this.convertCron(LOCAL_TIME_ZONE, GMT_TIME_ZONE)})"
}

fun Automation.toEventBridgeRuleName(): String {
    return "${id}_automation"
}

/**
 * Convert cron time from local to GMT timezone
 */
fun String.convertCron(from: ZoneId, to: ZoneId): String {
    val offsetDiff = to.offset() - from.offset()
    return CronConverter().using(this).from(from).to(to).convert().convertCronDays(this, offsetDiff)
}

fun String.convertCronDays(oldCronExpression: String, offsetDiff: Int): String {
    val newCronParts = split(' ').toMutableList()
    val oldCronParts = oldCronExpression.split(' ')
    val newHour = newCronParts[HOUR_FIELD].toInt()
    val oldHour = oldCronParts[HOUR_FIELD].toInt()

    newCronParts[DAY_OF_WEEK_FIELD] = if (offsetDiff > 0 && newHour < oldHour) {
        newCronParts[DAY_OF_WEEK_FIELD].modifyDays(CronDayOfWeek::inc)
    } else if (offsetDiff < 0 && newHour > oldHour) {
        newCronParts[DAY_OF_WEEK_FIELD].modifyDays(CronDayOfWeek::dec)
    } else {
        newCronParts[DAY_OF_WEEK_FIELD]
    }
    return newCronParts.joinToString(" ")
}

fun String.modifyDays(modFunc: CronDayOfWeek.() -> CronDayOfWeek): String {
    val dotw = split(",").map { CronDayOfWeek.valueOf(it) }.toMutableList()
    return dotw.map { it.modFunc() }.joinToString(",") { it.name }
}

fun ZoneId.offset() = rules.getOffset(Instant.now()).get(ChronoField.OFFSET_SECONDS)

val GMT_TIME_ZONE: ZoneId = ZoneId.of("GMT0")
val LOCAL_TIME_ZONE: ZoneId = ZoneId.systemDefault()

fun ControlAction.toTarget(homeServerConfig: HomeServerConfig): Target {
    return Target.builder()
        .arn(homeServerConfig.awsIotTriggerLambdaArn)
        .id(control.toTargetId())
        .input("{ \"thing_name\": \"${control.thingName}\", \"value\": \"${state.name}\" }")
        .build()
}

fun Control.toTargetId(): String {
    return "CONTROL_${id}_$thingName"
}

fun Automation.toCloudWatchRuleName(): String {
    return "Automation_${id.value}"
}
