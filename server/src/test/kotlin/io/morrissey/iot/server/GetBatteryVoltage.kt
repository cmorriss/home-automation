package io.morrissey.iot.server

import io.morrissey.iot.server.aws.offset
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.max

class GetBatteryVoltage {
}

fun main() {
    val startTime = LocalDateTime.of(2021, 3, 6, 20, 0, 3)
    val fileText = File("/home/daddy/moisture-sensors.log").readText()
    val offset = ZoneId.systemDefault().rules.getOffset(Instant.now())
    val voltages = fileText.split('\n')
        .filter { it.endsWith("}") }
        .filter { it.contains(Regex("remainingBatteryPercent")) }
        .map { it.substring(7..14) to it.substring((it.length - 7)..(it.length - 4)) }
        .map {
            val (hour, minute, second) = it.first.split(':').map { time -> time.toInt() }
            val dateTime = LocalDateTime.of(2021, 3, 6, hour, minute, second)
            dateTime to it.second.toInt()
        }.filter { it.first.isAfter(startTime) }
        .sortedBy { it.first.toEpochSecond(offset) }
    var maxDrop = 0
    for (i in 1..(voltages.size - 1)) {
        val drop = voltages[i - 1].second - voltages[i].second
        if (drop > maxDrop) maxDrop = drop
    }
    println("last voltage = ${voltages[voltages.size - 1]}")
    println("voltages length = ${voltages.size}")
    println("max drop = $maxDrop")

}
