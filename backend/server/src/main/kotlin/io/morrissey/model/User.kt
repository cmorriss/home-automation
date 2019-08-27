package io.morrissey.model

data class User(
    val id: Int = NO_ID,
    val oauthId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val picUrl: String
)