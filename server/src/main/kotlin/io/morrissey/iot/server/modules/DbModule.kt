package io.morrissey.iot.server.modules

import io.morrissey.iot.server.aws.AwsAutomationStatusHandler
import io.morrissey.iot.server.aws.AwsResumeDateHandler
import io.morrissey.iot.server.model.AutomationStatusEnum
import io.morrissey.iot.server.services.AutomationStatusHandler
import io.morrissey.iot.server.services.ResumeDateHandler
import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.reflect.KMutableProperty

fun dbModule(): Module {
    return module {
        factory<AutomationStatusHandler> { (dbProperty: KMutableProperty<AutomationStatusEnum>) ->
            AwsAutomationStatusHandler(dbProperty, get(), get())
        }
        factory<ResumeDateHandler> { (dbProperty: KMutableProperty<String>) ->
            AwsResumeDateHandler(dbProperty, get())
        }
    }
}
