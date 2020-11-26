package io.morrissey.iot.server.model

data class MetricData(val metric: MetricDto, val values: List<Double>, val startTime: String, val endTime: String)
