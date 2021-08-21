package io.morrissey.iot.server.routes

import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

object AuthorizedRoute : Qualifier {
    override val value: QualifierValue = "AuthorizedRoute"
}

object AuthenticatedRoute : Qualifier {
    override val value: QualifierValue = "AuthenticatedRoute"
}
