package io.morrissey.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.morrissey.routes.log
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Schedule(
    val id: Int = NO_ID,
    val daysOn: Set<DayOfWeek> = setOf(),
    val startTime: String = "12:00",
    val duration: Int = 30
) {
    private val startHour: Int
        get() = startTime.substring(0, 2).toInt()
    private val startMinute: Int
        get() = startTime.substring(3, 5).toInt()

    private fun shouldStart(now: LocalDateTime): Boolean {
        return daysOn.contains(now.dayOfWeek) &&
                now.hour == startHour &&
                now.minute == startMinute
    }

    fun shouldStart(clock: Clock): Boolean {
        return shouldStart(LocalDateTime.now(clock))
    }

    fun shouldStop(clock: Clock): Boolean {
        val wouldHaveStarted = LocalDateTime.now(clock).minusMinutes(duration.toLong())

        log.debug("Checking should stop for schedule $id, start time would have been on ${wouldHaveStarted.dayOfWeek} at ${wouldHaveStarted.hour}:${wouldHaveStarted.minute} this should start on $daysOn at $startTime")
        return shouldStart(wouldHaveStarted)
    }
}