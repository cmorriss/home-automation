package io.morrissey.iot.server.model

import com.google.inject.Injector
import io.morrissey.iot.server.log
import io.morrissey.iot.server.services.AutomationStatusHandlerFactory
import io.morrissey.iot.server.services.ResumeDateHandlerFactory
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.transaction
import javax.inject.Inject

enum class AutomationStatusEnum { ACTIVE, PAUSED, STOPPED }

object AutomationGroups : Groups<Automation, AutomationGroup>("automation_group") {
    val status = enumeration("status", AutomationStatusEnum::class)
    val resumeDate = varchar("resume_date", 255)
}

class AutomationGroup(
    id: EntityID<Int>,
    automationStatusHandlerFactory: AutomationStatusHandlerFactory,
    resumeDateHandlerFactory: ResumeDateHandlerFactory
) : Group<Automation, AutomationGroupDto>(id), AutomationState {
    companion object : IntEntityClass<AutomationGroup>(
        AutomationGroups, AutomationGroup::class.java
    ) {
        @Inject
        private lateinit var injector: Injector

        override fun createInstance(entityId: EntityID<Int>, row: ResultRow?) : AutomationGroup {
              return AutomationGroup(
                  entityId,
                  injector.getInstance(AutomationStatusHandlerFactory::class.java),
                  injector.getInstance(ResumeDateHandlerFactory::class.java)
              )
        }
    }

    var _status by AutomationGroups.status
    override var status by automationStatusHandlerFactory.create(::_status)
    var _resumeDate by AutomationGroups.resumeDate
    override var resumeDate by resumeDateHandlerFactory.create(::_resumeDate)
    override var name by AutomationGroups.name
    override var items: List<Automation> by AutomationGroups.itemIds.transform({ automations ->
                                                                                   automations.joinToString(":") {
                                                                                       it.id.toString()
                                                                                   }
                                                                               }, { itemsString ->
                                                                                   itemsString.split(":")
                                                                                       .filter { it.isNotBlank() }
                                                                                       .map(String::toInt)
                                                                                       .map { Automation.findById(it)!! }
                                                                               })
    override val automations: List<Automation>
        get() {
            return transaction { items }
        }

    override fun toDto(): AutomationGroupDto {
        return transaction {
            AutomationGroupDto(id.value, resumeDate, status, name, items.map { it.toDto() })
        }
    }
}

data class AutomationGroupDto(
    override val id: Int,
    val resumeDate: String,
    val status: AutomationStatusEnum,
    override val name: String,
    override val items: List<AutomationDto>
) : GroupDto<AutomationDto, AutomationGroup>() {
    override fun update(): AutomationGroup {
        log.info("Updating automation group with name $name, items size is ${items.size}")
        return transaction {
            AutomationGroup[this@AutomationGroupDto.id].apply {
                name = this@AutomationGroupDto.name
                items = this@AutomationGroupDto.items.map(AutomationDto::update)
                resumeDate = this@AutomationGroupDto.resumeDate
                status = this@AutomationGroupDto.status
            }
        }
    }
}
