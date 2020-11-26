package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

class ListTransformer<E : IntEntity>(private val entityClass: IntEntityClass<E>) {
    fun toList(input: String): List<E> {
        return if (input.isBlank()) {
            emptyList()
        } else {
            input.split(":").map(String::toInt).map { transaction { entityClass.findById(it)!! } }
        }
    }

    fun fromList(input: List<E>): String {
        return input.joinToString(":") {
            transaction {
                it.id.toString()
            }
        }
    }
}

fun <E : IntEntity> listColumn(
    column: Column<String>, entityClass: IntEntityClass<E>
): ColumnWithTransform<String, List<E>> {
    val listTransformer = ListTransformer(entityClass)
    return ColumnWithTransform(column, listTransformer::fromList, listTransformer::toList)
}