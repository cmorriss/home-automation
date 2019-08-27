package io.morrissey.persistence

import org.jetbrains.exposed.dao.IntIdTable

object Users : IntIdTable() {
    val oauthId = varchar("oauth_id", 256).uniqueIndex()
    val createDate = datetime("create_date")
    val email = varchar("email", 254)
    val picUrl = varchar("picUrl", 1024)
    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
}







