package io.morrissey.iot.server.visibility

import io.micrometer.core.instrument.Clock
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import java.time.Duration


fun influxMeterRegistry(): InfluxMeterRegistry {
    val config = object : InfluxConfig {
        override fun step(): Duration {
            return Duration.ofSeconds(10)
        }

        override fun db(): String {
            return "mydb"
        }

        override fun get(k: String): String? {
            return null // accept the rest of the defaults
        }
    }
    return InfluxMeterRegistry(config, Clock.SYSTEM)
}