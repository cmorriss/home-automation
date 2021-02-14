package io.morrissey.iot.server.model

import io.morrissey.iot.server.log
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Metrics : IntIdTable("metric") {
    val name = varchar("name", 255)
    val externalName = varchar("external_name", 255)
    val externalNamespace = varchar("external_namespace", 255)
    val period = integer("period")
    val statistic = enumeration("statistic", Metric.Statistic::class)
    val dimensions = varchar("dimensions", 1024)
}

class Metric(
    id: EntityID<Int>
) : TransferableEntity<MetricDto>(id) {
    companion object : IntEntityClass<Metric>(Metrics, Metric::class.java)

    var name by Metrics.name
    var externalName by Metrics.externalName
    var externalNamespace by Metrics.externalNamespace
    var period by Metrics.period
    var statistic by Metrics.statistic
    var dimensions: List<MetricDimension> by Metrics.dimensions.transform({ dimensions ->
                                                                              dimensions.joinToString(";") {
                                                                                  "${it.name}:${it.value}"
                                                                              }
                                                                          }, { dimensionsString ->
                                                                              dimensionsString.split(";")
                                                                                  .filter { it.isNotBlank() }
                                                                                  .map {
                                                                                      val components = it.split(":")
                                                                                      MetricDimension(components[0], components[1])
                                                                                  }
                                                                          })

    override fun toDto(): MetricDto {
        return transaction {
            MetricDto(
                id = id.value,
                name = name,
                externalName = externalName,
                externalNamespace = externalNamespace,
                period = period,
                statistic = statistic,
                dimensions = dimensions
            )
        }
    }

    enum class Statistic(val cwName: String) {
        MIN("Minimum"),
        MAX("Maximum"),
        SUM("Sum"),
        AVG("Average"),
        COUNT("SampleCount"),
        P100("p100.0"),
        P99("p99.0"),
        P90("p90.0"),
        P50("p50.0")
    }
}

data class MetricDto(
    override val id: Int,
    val name: String,
    val externalName: String,
    val externalNamespace: String,
    val period: Int,
    val statistic: Metric.Statistic,
    val dimensions: List<MetricDimension>
) : EntityDto<Metric>() {
    override fun create(): Metric {
        return transaction {
            Metric.new {
                name = this@MetricDto.name
                externalName = this@MetricDto.externalName
                externalNamespace = this@MetricDto.externalNamespace
                period = this@MetricDto.period
                statistic = this@MetricDto.statistic
                dimensions = this@MetricDto.dimensions
            }
        }
    }

    override fun update(): Metric {
        log.info("Updating metric dto with id: $id")
        return transaction {
            Metric[id].apply {
                name = this@MetricDto.name
                externalName = this@MetricDto.externalName
                externalNamespace = this@MetricDto.externalNamespace
                period = this@MetricDto.period
                statistic = this@MetricDto.statistic
                dimensions = this@MetricDto.dimensions
            }
        }
    }
}
