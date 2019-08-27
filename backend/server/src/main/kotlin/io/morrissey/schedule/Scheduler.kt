package io.morrissey.schedule

import io.ktor.client.HttpClient
import io.morrissey.model.Switch
import io.morrissey.model.LocationStatus.OK
import io.morrissey.model.ScheduleStatus
import io.morrissey.persistence.HomeDao
import io.morrissey.routes.PhysicalSwitchResult.FailureResult
import io.morrissey.routes.PhysicalSwitchResult.SuccessResult
import io.morrissey.routes.log
import io.morrissey.routes.refreshSwitchState
import io.morrissey.routes.updateSwitch
import org.joda.time.DateTime
import java.time.Clock
import java.util.*

class Scheduler(
    val db: HomeDao,
    scheduleTimer: Timer,
    val httpClient: HttpClient,
    val clock: Clock
) {
    companion object {
        const val FIVE_MINUTES_IN_MILLIS = 5 * 60_000L
    }

    init {
        val delay = calculateDelay(clock)
        log.debug("Current time is ${clock.instant()} and delay is ${delay / 1000} seconds.")
        scheduleTimer.scheduleAtFixedRate(CheckScheduleTask(), calculateDelay(clock), FIVE_MINUTES_IN_MILLIS)
    }

    private fun calculateDelay(clock: Clock): Long {
        val offset = clock.millis() % FIVE_MINUTES_IN_MILLIS
        return if (offset == 0L) 0L else FIVE_MINUTES_IN_MILLIS - offset
    }

    inner class CheckScheduleTask : TimerTask() {
        override fun run() {
            refreshSwitchState(httpClient, db)
            val switches = db.switches()
            if (!isPausedOrStopped()) {
                switches.turnOnIfNeeded()
            }
            switches.turnOffIfNeeded()
        }

        private fun isPausedOrStopped(): Boolean {
            val scheduleStatus = db.scheduleStatus()
            return if (scheduleStatus.status == "paused") {
                val date = DateTime(scheduleStatus.pausedUntilDate)
                if (date.isAfterNow) {
                    log.debug("Schedule is paused until ${scheduleStatus.pausedUntilDate}, so skipping checks to turn on switches.")
                    true
                } else {
                    log.debug("Schedule was paused, but will now be unpaused as the current date is after the paused until date.")
                    db.updateScheduleStatus(ScheduleStatus("active", scheduleStatus.pausedUntilDate))
                    false
                }
            } else {
                scheduleStatus.status == "stopped"
            }
        }

        private fun List<Switch>.turnOnIfNeeded() {
            filter { aSwitch -> aSwitch.schedule.shouldStart(clock) }.forEach { aSwitch -> aSwitch.update(on = true) }
        }

        private fun List<Switch>.turnOffIfNeeded() {
            filter { aSwitch -> aSwitch.schedule.shouldStop(clock) }.forEach { aSwitch -> aSwitch.update(on = false) }
        }

        private fun Switch.update(on: Boolean) {
            log.debug("updating switch $name to on = $on")
            val updatedSwitch = copy(on = on)
            when (val result = updateSwitch(httpClient, updatedSwitch)) {
                is SuccessResult -> db.updateSwitch(
                    copy(
                        on = result.physicalSwitches[0].on,
                        locationStatus = OK,
                        locationStatusMessage = ""
                    )
                )
                is FailureResult -> db.updateSwitch(
                    copy(
                        locationStatus = result.locationStatus,
                        locationStatusMessage = result.statusMessage
                    )
                )
            }
        }
    }
}
