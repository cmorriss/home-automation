package io.morrissey.iot.server.layout

import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.*
import io.morrissey.iot.server.model.CronDayOfWeek.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun homeIot(controller: Controller, synchronizer: AutomationSynchronizer, automationDef: HomeAutomationDef.() -> Unit) {
    transaction {
        HomeAutomationDef(controller, synchronizer).automationDef()
    }
}

class HomeAutomationDef(val controller: Controller, val synchronizer: AutomationSynchronizer)


class AutomationGroupDef(val name: String) {
    val automations: MutableSet<Automation> = mutableSetOf()
    val EVERYDAY = values().toSet()
    val WEEKDAYS = setOf(MON, TUE, WED, THU, FRI)
    val WEEKENDS = setOf(SUN, SAT)
}

fun HomeAutomationDef.control(thingName: String, name: String, type: ControlType): Control {
    return transaction {
        var control = Control.find { Controls.thingName eq thingName }.firstOrNull()
        if (control == null) {
            control = Control.new {
                this.thingName = thingName
                this.name = name
                this.type = type
                this.state = ControlState.OFF
                lastUpdate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            }
        } else {
            control.name = name
            control.type = type
        }
        controller.pullState(control.id.value)
        controller.listenToState(control.id.value)
        control
    }
}

fun HomeAutomationDef.controlGroup(name: String, controls: Set<Control>): ControlGroup {
    var controlGroup = ControlGroup.find { ControlGroups.name eq name }.firstOrNull()
    if (controlGroup == null) {
        controlGroup = ControlGroup.new {
            this.name = name
            this.items = controls.toList()
        }
    } else {
        val (toAdd, _, toRemove) = findChanges(
            controlGroup.items.toSet(),
            controls
        ) { existing, new ->
            existing.thingName == new.thingName
        }
        controlGroup.items = controlGroup.items.minus(toRemove).plus(toAdd)
    }
    return controlGroup
}

fun HomeAutomationDef.automationGroup(name: String, groupCreate: AutomationGroupDef.() -> Unit): AutomationGroup {
    val def = AutomationGroupDef(name).apply(groupCreate)
    var automationGroup = AutomationGroup.find { AutomationGroups.name eq name }.firstOrNull()
    if (automationGroup == null) {
        automationGroup = AutomationGroup.new {
            this.name = name
            this.items = def.automations.toList()
            this.resumeDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
            this.status = AutomationStatusEnum.ACTIVE
        }
        log.info("Created new automation group:")
        automationGroup.logState()
        log.info("Adding ${automationGroup.items.size} new automations for automation group $name...")
        automationGroup.items.forEach(synchronizer::create)
    } else {
        log.info("Found existing automation group:")
        automationGroup.logState()
        val (toAdd, toKeep, toRemove) = findChanges(
            automationGroup.items.toSet(),
            def.automations
        ) { existing, new ->
            existing.actionId == new.actionId && existing.eventId == new.eventId
        }
        log.info("Adding ${toAdd.size} new schedules for automation group $name...")
        toAdd.forEach(synchronizer::create)
        log.info("Pulling ${toKeep.size} schedules for automation group $name...")
        toKeep.forEach(synchronizer::pull)
        log.info("Removing ${toRemove.size} schedules for automation group $name...")
        toRemove.forEach(synchronizer::remove)
        automationGroup.items = automationGroup.items.minus(toRemove).plus(toAdd)
    }
    return automationGroup
}

fun HomeAutomationDef.metric(
    name: String,
    externalName: String,
    externalNamespace: String,
    period: Int,
    statistic: Metric.Statistic,
    dimensions: List<MetricDimension> = emptyList()
) {
    val metric =
        Metric.find { (Metrics.externalName eq externalName) and (Metrics.externalNamespace eq externalNamespace) }
            .firstOrNull()
    if (metric == null) {
        log.info("Adding new metric $name")
        Metric.new {
            this.name = name
            this.externalName = externalName
            this.externalNamespace = externalNamespace
            this.period = period
            this.statistic = statistic
            this.dimensions = dimensions
        }
    } else {
        log.info("Found existing metric with external name: $externalName, updating values...")
        metric.name = name
        metric.period = period
        metric.statistic = statistic
        metric.dimensions = dimensions
    }
}

fun AutomationGroup.logState() {
    val automationDescriptions =
        items.map { "Automation(id=${it.id.value}, eventId=${it.eventId}, actionId=${it.actionId})\n" }
    log.info("Automation group state with name $name and automations:\n$automationDescriptions\n")
}

fun AutomationGroupDef.scheduledAutomation(
    control: Control,
    startCron: String,
    duration: Int
): Pair<Automation, Automation> {
    val endCron = calcEndCron(startCron, duration)
    return scheduledAutomation(control, startCron, endCron)
}

fun AutomationGroupDef.scheduledAutomation(
    control: Control,
    startCron: String,
    endCron: String
): Pair<Automation, Automation> {
    val startAction = controlAction(control, ControlState.ON)
    val startAutomation =
        scheduledAutomation(startAction, startCron)
    val endAction = controlAction(control, ControlState.OFF)
    val endAutomation =
        scheduledAutomation(endAction, endCron)
    startAutomation.associatedAutomationId = endAutomation.id.value
    automations.add(startAutomation)
    automations.add(endAutomation)
    return startAutomation to endAutomation
}

fun scheduledAutomation(action: Action<*>, cron: String): Automation {
    var automation = Automation.find { Automations.actionId eq action.id.value }.firstOrNull()
    if (automation == null) {
        automation = Automation.new {
            this.actionId = action.id.value
            this.actionType = when (action) {
                is ControlAction -> ActionType.CONTROL
                is AutomationGroupAction -> ActionType.AUTOMATION_GROUP
                is AutomationAction -> ActionType.AUTOMATION
                else -> throw IllegalArgumentException("Unknown action type found for class ${action.javaClass.canonicalName}.")
            }
            this.cron = cron
            this.eventId = -1
            this.eventType = EventType.SCHEDULE
            this.associatedAutomationId = -1
            this.status = AutomationStatusEnum.ACTIVE
            this.resumeDate = ""
            this.name = "Automation for action ${action.id.value} and schedule $cron"
        }
    }
    return automation
}

fun calcEndCron(startCron: String, duration: Int): String {
    val (startHour, startMin) = startCron.toTimeInts()
    val durationHours = duration / 60
    val durationMinutes = duration % 60
    var endHour = startHour + durationHours
    var endMin = startMin + durationMinutes
    if (endMin > 59) {
        endMin -= 60
        endHour++
    }
    return if (endHour > 23) {
        endHour -= 24
        startCron.shiftDayForward()
    } else {
        startCron
    }.updateTimeInts(endHour, endMin)
}

private fun String.toTimeInts(): Pair<Int, Int> {
    return split(' ').let { it[HOUR_FIELD].toInt() to it[MINUTE_FIELD].toInt() }
}

private fun String.updateTimeInts(hour: Int, minute: Int): String {
    return StringBuffer("$minute $hour").apply {
        this@updateTimeInts.split(' ').forEachIndexed { i, part ->
            if (i > 1) {
                append(' ')
                append(part)
            }
        }
    }.toString()
}

private fun String.shiftDayForward(): String {
    return StringBuffer().apply {
        this@shiftDayForward.split(' ').forEachIndexed { i, part ->
            if (i != 0) append(' ')
            if (i == WEEKDAY_CRON_INDEX) {
                append(part.replace(Regex("\\w\\w\\w")) { matchResult: MatchResult ->
                    CronDayOfWeek.valueOf(matchResult.value).inc().name
                })
            } else {
                append(part)
            }
        }
    }.toString()
}

const val WEEKDAY_CRON_INDEX = 4

private fun Int.shiftDayForward() = if (this < 6) this + 1 else 0

fun controlAction(control: Control, state: ControlState): ControlAction {
    var controlAction = ControlActions.selectAll().singleOrNull {
        val existingControl = Control[it[ControlActions.control]]
        val existingState = it[ControlActions.state]
        existingControl.thingName == control.thingName && existingState == state
    }?.let {
        ControlAction[it[ControlActions.id]]
    }
    if (controlAction == null) {
        controlAction = ControlAction.new {
            this.control = control
            this.state = state
        }
    }
    return controlAction
}

fun <E : IntEntity> findChanges(
    existingItems: Set<E>,
    newItems: Set<E>,
    comparator: (E, E) -> Boolean
): DataChanges<E> {
    val toKeep = existingItems.filter { existingItem -> newItems.any { newItem -> comparator(existingItem, newItem) } }
        .toSet()
    val toAdd = newItems.subtract(toKeep)
    val toRemove = existingItems.subtract(toKeep)
    return DataChanges(toAdd, toKeep, toRemove)
}

data class DataChanges<E : IntEntity>(val toAdd: Set<E>, val toKeep: Set<E>, val toRemove: Set<E>)
