package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction

object Thresholds : Events("threshold") {
}

class Threshold(id: EntityID<Int>) : Event<ThresholdDto>(id) {
    companion object : IntEntityClass<Threshold>(Thresholds, Threshold::class.java)

    override fun toDto(): ThresholdDto {
        return transaction {
            ThresholdDto(
                id.value
            )
        }
    }
}

data class ThresholdDto(
    override val id: Int
) : EventDto<Threshold>() {
    override fun update(): Threshold {
        return transaction {
            Threshold[id].apply {
            }
        }
    }

    override fun create(): Threshold {
        return Threshold.new {
        }
    }
}
