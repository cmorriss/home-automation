package io.morrissey.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.morrissey.routes.log
import io.requery.*
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
interface Schedule : Persistable {
    @get:Key @get:Generated
    val id: Int
    @get:Convert(DayOfWeekSetConverter::class)
    var daysOn: Set<@JvmSuppressWildcards DayOfWeek>
    var startTime: String
    var duration: Int
}

class ScheduleDto(
) {
    var id: Int = -1
    var daysOn: Set<DayOfWeek> = emptySet()
    var startTime: String = ""
    var duration: Int = 0
}

fun Schedule.getStartHour(): Int { return startTime.substring(0, 2).toInt() }

fun Schedule.getStartMinute(): Int { return startTime.substring(3, 5).toInt() }

fun emptySchedule(): ScheduleEntity {
    return ScheduleEntity().apply {
        startTime = "12:00"
        duration = 30
    }
}

fun Schedule.shouldStart(now: LocalDateTime): Boolean {
    return daysOn.contains(now.dayOfWeek) &&
            now.hour == getStartHour() &&
            now.minute == getStartMinute()
}

fun Schedule.shouldStart(clock: Clock): Boolean {
    return shouldStart(LocalDateTime.now(clock))
}

fun Schedule.shouldStop(clock: Clock): Boolean {
    val wouldHaveStarted = LocalDateTime.now(clock).minusMinutes(duration.toLong())

    log.debug("Checking should stop for schedule $id, start time would have been on ${wouldHaveStarted.dayOfWeek} at ${wouldHaveStarted.hour}:${wouldHaveStarted.minute} this should start on $daysOn at $startTime")
    return shouldStart(wouldHaveStarted)
}

@Suppress("UNCHECKED_CAST")
class DayOfWeekSetConverter : Converter<Set<@kotlin.jvm.JvmSuppressWildcards DayOfWeek>, String> {
    override fun convertToMapped(type: Class<out Set<DayOfWeek>>?, value: String?): Set<DayOfWeek> {
        return if (value.isNullOrBlank()) {
            emptySet()
        } else {
            value.split(",").fold(mutableSetOf(), { daysOn, nextDay ->
                daysOn.add(DayOfWeek.valueOf(nextDay))
                daysOn
            })
        }
    }

    override fun getPersistedType(): Class<String> {
        return String::class.java
    }

    override fun convertToPersisted(value: Set<DayOfWeek>?): String {
        return value?.joinToString(",") { it.name } ?: ""
    }

    override fun getMappedType(): Class<Set<DayOfWeek>> {
        return Set::class.java as Class<Set<DayOfWeek>>
    }

    override fun getPersistedSize(): Int? = null
}
