package io.morrissey.iot.server.layout

import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.ControlType
import io.morrissey.iot.server.model.Metric

class HomeIotLayoutImpl(
    private val controller: Controller, private val synchronizer: AutomationSynchronizer
) : HomeIotLayout {
    override fun populate() {
        log.debug("Populating home IoT...")

        homeIot(controller, synchronizer) {
            val isla = control(
                thingName = "La_Isla_Bonita_Irrigation", name = "La Isla Bonita", type = ControlType.IRRIGATION_VALVE
            )
            val irish =
                control(thingName = "Irish_Moss_Irrigation", name = "Irish Moss", type = ControlType.IRRIGATION_VALVE)
            val grass = control(thingName = "Grass_Irrigation", name = "Grass", type = ControlType.IRRIGATION_VALVE)
            val orchard =
                control(thingName = "Orchard_Irrigation", name = "Orchard", type = ControlType.IRRIGATION_VALVE)
            val garden = control(thingName = "Garden_Irrigation", name = "Garden", type = ControlType.IRRIGATION_VALVE)
            val bushes = control(thingName = "Bushes_Irrigation", name = "Bushes", type = ControlType.IRRIGATION_VALVE)
            val ferns = control(thingName = "Ferns_Irrigation", name = "Ferns", type = ControlType.IRRIGATION_VALVE)
            controlGroup(
                name = "Irrigation", controls = setOf(
                    isla, irish, grass, orchard, garden, bushes, ferns
                )
            )

            val backyardLight =
                control(thingName = "Backyard_Flood_Light", name = "Flood Light", type = ControlType.LIGHT_SWITCH)
            val cabinetLights =
                control(thingName = "Kitchen_Cabinet_Lights", name = "Cabinet Lights", type = ControlType.LIGHT_SWITCH)
            val livingRoomLamp =
                control(thingName = "TP_Link_Switch_Lamp", name = "Living Room Lamp", type = ControlType.LIGHT_SWITCH)
            controlGroup(name = "Lights", controls = setOf(backyardLight, cabinetLights, livingRoomLamp))

            automationGroup("Lights") {
                scheduledAutomation(
                    control = backyardLight,
                    startCron = "0 19 ? * SUN,MON,TUE,WED,THU,FRI,SAT *",
                    endCron = "0 0 ? * SUN,MON,TUE,WED,THU,FRI,SAT *"
                )
            }

            automationGroup("Irrigation") {
                scheduledAutomation(control = isla, startCron = "0 18 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = irish, startCron = "30 18 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = grass, startCron = "0 19 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = orchard, startCron = "30 19 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = garden, startCron = "0 20 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = bushes, startCron = "30 20 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = ferns, startCron = "0 21 ? * MON,WED,SAT *", duration = 30)
            }

            metric(
                name = "Master Bath Humidity",
                externalName = "Master_Bath_Humidity",
                externalNamespace = "Home",
                period = 300,
                statistic = Metric.Statistic.AVG
            )
            metric(
                name = "Humidity Baseline",
                externalName = "Master_Bath_Humidity_Baseline",
                externalNamespace = "Home",
                period = 300,
                statistic = Metric.Statistic.AVG
            )
        }
    }
}
