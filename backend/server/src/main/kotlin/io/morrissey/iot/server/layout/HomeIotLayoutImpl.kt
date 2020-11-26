package io.morrissey.iot.server.layout

import io.morrissey.iot.server.aws.Controller
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.ControlType
import io.morrissey.iot.server.model.Metric
import java.time.DayOfWeek
import javax.inject.Inject

class HomeIotLayoutImpl @Inject constructor(private val controller: Controller, private val synchronizer: AutomationSynchronizer) : HomeIotLayout {
    override fun populate() {
        log.debug("Populating home IoT...")

        homeIot(controller, synchronizer) {
            val isla = control(thingName = "La_Isla_Bonita_Irrigation", name = "La Isla Bonita", type = ControlType.IRRIGATION_VALVE)
            val irish = control(thingName = "Irish_Moss_Irrigation", name = "Irish Moss", type = ControlType.IRRIGATION_VALVE)
            val grass = control(thingName = "Grass_Irrigation", name = "Grass", type = ControlType.IRRIGATION_VALVE)
            val orchard = control(thingName = "Orchard_Irrigation", name = "Orchard", type = ControlType.IRRIGATION_VALVE)
            val garden = control(thingName = "Garden_Irrigation", name = "Garden", type = ControlType.IRRIGATION_VALVE)
            val bushes = control(thingName = "Bushes_Irrigation", name = "Bushes", type = ControlType.IRRIGATION_VALVE)
            val ferns = control(thingName = "Ferns_Irrigation", name = "Ferns", type = ControlType.IRRIGATION_VALVE)
            controlGroup(
                name = "Irrigation", controls = setOf(
                    isla, irish, grass, orchard, garden, bushes, ferns
                )
            )

            val backyardLight = control(thingName = "Backyard_Flood_Light", name = "Flood Light", type = ControlType.LIGHT_SWITCH)
            val cabinetLights = control(thingName = "Kitchen_Cabinet_Lights", name = "Cabinet Lights", type = ControlType.LIGHT_SWITCH)
            controlGroup(name = "Lights", controls = setOf(backyardLight, cabinetLights))

            automationGroup("Lights") {
                scheduledAutomation(
                    control = backyardLight,
                    startCron = "0 19 ? * SUN,MON,TUE,WED,THU,FRI,SAT *",
                    endCron = "30 23 ? * SUN,MON,TUE,WED,THU,FRI,SAT *"
                )
            }

            automationGroup("Irrigation") {
                val irrigationDotw = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)
                scheduledAutomation(control = isla, startCron = "0 18 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = irish, startCron = "30 18 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = grass, startCron = "0 19 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = orchard, startCron = "30 19 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = garden, startCron = "0 20 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = bushes, startCron = "30 20 ? * MON,WED,SAT *", duration = 30)
                scheduledAutomation(control = ferns, startCron = "0 21 ? * MON,WED,SAT *", duration = 30)
            }

            metric("Master Bath Humidity","MASTER_BATH_HUMIDITY", "Home",  1, Metric.Statistic.AVG)
            metric("Soil Moisture Sensor 2", "MoistureSensor2", "Home",1, Metric.Statistic.AVG)
            metric("Soil Moisture Sensor 3","MoistureSensor3", "Home",1, Metric.Statistic.AVG)
        }
    }
}
