@file:Suppress("EXPERIMENTAL_API_USAGE")

package io.morrissey.iot.server.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.morrissey.iot.server.IdPath
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.EntityDto
import io.morrissey.iot.server.model.TransferableEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.Qualifier
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
abstract class EntityRoutes<D : EntityDto<*>, out E : TransferableEntity<D>>(
    routeQualifier: Qualifier,
    private val singlePath: KClass<out IdPath>,
    private val collectionPath: KClass<*>,
    private val entityClass: IntEntityClass<E>,
    private val entityDtoClass: KClass<D>
) : KoinComponent {
    init {
        val route : Route = get(routeQualifier)
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
                        respondWithExplicitType(call, createdDto)
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
                        val entityDto = getSingle(path.id)
                        try {
                            respondWithExplicitType(call, entityDto)
                        } catch (e: NotImplementedError) {
                            log.warn(
                                "An attempt was made to execute the GET method on the entity of type ${entityDtoClass::class.qualifiedName} and id ${entityDto.id}."
                            )
                            call.respond(HttpStatusCode.NotImplemented)
                        }
                    }
                }
                method(HttpMethod.Put) {
                    handle(singlePath) {
                        val entityDto = call.receive(entityDtoClass)
                        try {
                            val updatedDto = put(entityDto)
                            respondWithExplicitType(call, updatedDto)
                        } catch (e: NotImplementedError) {
                            log.warn(
                                "An attempt was made to execute the PUT method on the entity of type ${entityDtoClass::class.qualifiedName} and id ${entityDto.id}."
                            )
                            call.respond(HttpStatusCode.NotImplemented)
                        }
                    }
                }
                method(HttpMethod.Options) {
                    handle(singlePath) {
                        call.respond(HttpStatusCode.OK)
                    }
                }
                method(HttpMethod.Delete) {
                    handle(singlePath) { path ->
                        log.debug("DELETE request for entity of type ${entityDtoClass::class.qualifiedName} and id ${path.id}")
                        try {
                            transaction { entityClass[path.id].delete() }
                            log.debug("DELETE response OK")
                            call.respond(HttpStatusCode.OK)
                        } catch (e: NotImplementedError) {
                            log.warn(
                                "An attempt was made to execute the DELETE method on the entity of type ${entityDtoClass::class.qualifiedName} and id ${path.id}."
                            )
                            call.respond(HttpStatusCode.NotImplemented)
                        }
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

    abstract suspend fun respondWithExplicitType(call: ApplicationCall, entityDto: D)
}
