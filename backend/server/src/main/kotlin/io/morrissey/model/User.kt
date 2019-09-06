package io.morrissey.model

import io.requery.*

@Table(name = "AuthenticationUsers")
@Entity
interface User : Persistable {
    @get:Key
    @get:Generated
    val id: Int
    var oauthId: String
    var email: String
    var firstName: String
    var lastName: String
    var picUrl: String
}