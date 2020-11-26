package io.morrissey.iot.server.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object Users : IntIdTable("user") {
    val oauthId = varchar("oauth_id", 255)
    val email = varchar("email", 255)
    val firstName = varchar("first_name", 128)
    val lastName = varchar("lastName", 128)
    val picUrl = varchar("pic_url", 2048)
}

class User(id: EntityID<Int>) : TransferableEntity<UserDto>(id) {
    companion object : IntEntityClass<User>(Users)

    var oauthId by Users.oauthId
    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var picUrl by Users.picUrl

    override fun toDto(): UserDto {
        return transaction {
            UserDto(
                id.value, oauthId, email, firstName, lastName, picUrl
            )
        }
    }
}

data class UserDto(
    override val id: Int,
    val oauthId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val picUrl: String
) : EntityDto<User>()
