@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.call
import io.ktor.http.*
import io.ktor.locations.handle
import io.ktor.locations.location
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.method
import io.morrissey.iot.server.IdPath
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.EntityDto
import io.morrissey.iot.server.model.TransferableEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class EntityRoutes<D : EntityDto<*>, out E: TransferableEntity<D>>(
    private val route: Route,
    private val singlePath: KClass<out IdPath>,
    private val collectionPath: KClass<*>,
    private val entityClass: IntEntityClass<E>,
    private val entityDtoClass: KClass<D>
) {
    init {
        with(route) {
            location(collectionPath) {
                method(HttpMethod.Get) {
                    handle(collectionPath) {
                        call.respond(getCollection())
                    }
                }
                method(HttpMethod.Post) {
                    handle(collectionPath) {
                        val entityDto = call.receive(entityDtoClass)
                        val createdDto = post(entityDto)
                        call.respond(createdDto)
                    }
                }
                method(HttpMethod.Options) {
                    handle(singlePath) {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }

            location(singlePath) {
                method(HttpMethod.Get) {
                    handle(singlePath) { path ->
                        call.respond(getSingle(path.id))
                    }
                }
                method(HttpMethod.Put) {
                    handle(singlePath) {
                        val entityDto = call.receive(entityDtoClass)
                        val updatedDto = put(entityDto)
                        call.respond(updatedDto)
                    }
                }
                method(HttpMethod.Options) {
                    handle(singlePath) {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }

    open fun getCollection(): List<D> {
        return transaction { entityClass.all().toList().map { it.toDto() } }.also {
            log.debug("GET collection response entity:\n$it")
        }
    }

    open fun getSingle(id: Int): D {
        return transaction { entityClass[id].toDto() }.also {
            log.debug("GET single response entity:\n$it")
        }
    }

    open fun put(entityDto: D): D {
        log.debug("PUT request entity:\n$entityDto")
        return transaction { (entityDto.update() as E).toDto() }.also {
            log.debug("POST response entity:\n$it")
        }
    }

    open fun post(entityDto: D): D {
        log.debug("POST request entity:\n$entityDto")
        return transaction { (entityDto.create() as E).toDto() }.also {
            log.debug("POST response entity:\n$it")
        }
    }
}
