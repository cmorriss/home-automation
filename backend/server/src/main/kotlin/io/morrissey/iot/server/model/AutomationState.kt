package io.morrissey.iot.server.model

interface AutomationState {
    val name: String
    var status: AutomationStatusEnum
    var resumeDate: String
    val automations: List<Automation>
}
