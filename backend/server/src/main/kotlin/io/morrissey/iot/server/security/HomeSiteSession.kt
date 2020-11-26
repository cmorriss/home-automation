package io.morrissey.iot.server.security

data class HomeSiteSession(val id: String, val count: Int = 0)

class AuthenticationException : RuntimeException()
class AuthorizationException : RuntimeException()
