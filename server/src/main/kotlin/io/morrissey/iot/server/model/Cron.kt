package io.morrissey.iot.server.model

import com.cronutils.descriptor.CronDescriptor
import com.cronutils.model.definition.CronDefinition
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val cloudWatchCronDef: CronDefinition = CronDefinitionBuilder.defineCron()
    .withMinutes()
    .and()
    .withHours()
    .and()
    .withDayOfMonth()
    .supportsL()
    .supportsW()
    .supportsQuestionMark()
    .and()
    .withMonth()
    .and()
    .withDayOfWeek()
    .withMondayDoWValue(2)
    .supportsHash()
    .supportsL()
    .supportsQuestionMark()
    .and()
    .withYear()
    .and()
    .instance()
val cloudWatchCronParser: CronParser = CronParser(cloudWatchCronDef)
val cronDescriptor: CronDescriptor = CronDescriptor.instance(Locale.US)

const val MINUTE_FIELD = 0
const val HOUR_FIELD = 1
const val DAY_OF_MONTH_FIELD = 2
const val MONTH_FIELD = 3
const val DAY_OF_WEEK_FIELD = 4
const val YEAR_FIELD = 5

data class CronConversion(val time: String, val daysOfWeek: Set<CronDayOfWeek>, val dateTime: String)

fun convertFromCron(cronExpression: String): CronConversion {
    val cronParts = cronExpression.split(' ')
    val minute = cronParts[MINUTE_FIELD].toInt()
    val hour = cronParts[HOUR_FIELD].toInt()
    return if (cronParts[DAY_OF_WEEK_FIELD] == "?") {
        val dayOfMonth = cronParts[DAY_OF_MONTH_FIELD].toInt()
        val month = cronParts[MONTH_FIELD].toInt()
        val year = cronParts[YEAR_FIELD].toInt()
        val dateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute)
        CronConversion("", emptySet(), dateTime.format(DateTimeFormatter.ISO_DATE))
    } else {
        val daysOfWeek = cronParts[DAY_OF_WEEK_FIELD].toDaysOfWeek()
        CronConversion(String.format("%02d:%02d", hour, minute), daysOfWeek, "")
    }
}

fun String.toDaysOfWeek(): Set<CronDayOfWeek> {
    return split(',').map { CronDayOfWeek.valueOf(it) }.toSet()
}

fun convertToCron(time: String, daysOfWeek: Set<CronDayOfWeek>, dateTime: String): String {
    return if (time.isBlank() && daysOfWeek.isEmpty() && dateTime.isNotBlank()) {
        convertToCron(dateTime)
    } else if (time.isNotBlank() && daysOfWeek.isNotEmpty() && dateTime.isBlank()) {
        convertToCron(time, daysOfWeek)
    } else {
        throw IllegalArgumentException("One, and only one, of either time and days of week or dateTime must be specified. Found time=$time, daysOfWeek=$daysOfWeek, and dateTime=$dateTime.")
    }
}

fun convertToCron(time: String, daysOfWeek: Set<CronDayOfWeek>): String {
    val (hour, minute) = time.split(':').run { this[0] to this[1] }
    val cronDaysOfWeek = daysOfWeek.joinToString(",") { it.name.subSequence(0, 3) }
    return "$minute $hour ? * $cronDaysOfWeek *"
}

fun convertToCron(dateTime: String): String {
    val localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_DATE)
    return "${localDateTime.minute} ${localDateTime.hour} ${localDateTime.dayOfMonth} ${localDateTime.monthValue} ? ${localDateTime.year}"
}
