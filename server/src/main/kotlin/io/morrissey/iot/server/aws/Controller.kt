package io.morrissey.iot.server.aws

import io.morrissey.iot.server.json
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.Control
import io.morrissey.iot.server.model.ControlState
import io.morrissey.iot.server.model.ControlType
import io.morrissey.iot.server.model.Controls
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.crt.mqtt.MqttClientConnection
import software.amazon.awssdk.crt.mqtt.QualityOfService
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Controller(
    private val iotDataPlaneClient: IotDataPlaneClient, private val mqttClient: MqttClientConnection
) {
    private val updateAcceptedPattern = Regex("\\\$aws/things/(.*?)/shadow/update/accepted").toPattern()

    init {
        mqttClient.connect().get()
    }

    fun notifyUpdated(controlId: Int) {
        transaction {
            val control = Control[controlId]
            iotDataPlaneClient.publish {
                it.payload(control.toIotPayload())
                it.qos(0)
                it.topic(control.toUpdateTopicName())
            }
        }
    }

    fun pullState(controlId: Int) {
        transaction {
            val control = Control[controlId]
            val shadow = iotDataPlaneClient.getThingShadow {
                it.thingName(control.thingName)
            }.payload().asUtf8String()
            val payload: ControlThingPayload = json.decodeFromString(ControlThingPayload.serializer(), shadow)
            payload.state.reported?.value?.let {
                control.state = it
                control.lastUpdate = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                log.info("Pulled state $it for control thing ${control.thingName}")
            }
        }
    }

    fun listenToState(controlId: Int) {
        transaction {
            val control = Control[controlId]
            mqttClient.subscribe(control.toUpdateAcceptedTopicName(), QualityOfService.AT_LEAST_ONCE) { message ->
                val messageTopic = message?.topic
                val payload = message?.payload?.toString(Charsets.UTF_8)
                log.info("Received message from AWS, topic = $messageTopic, payload = \n$payload")
                if (messageTopic != null && payload != null) {
                    val thingNameMatcher = updateAcceptedPattern.matcher(messageTopic)
                    if (thingNameMatcher.find()) {
                        val thingName = thingNameMatcher.group(1)
                        val newState = json.decodeFromString(ControlThingPayload.serializer(), payload)
                        val newValue = newState.state.reported?.value
                        if (newValue != null) {
                            log.info("Updating control $thingName to value $newValue.")
                            transaction {
                                val topicControl = Control.find { Controls.thingName eq thingName }
                                if (!topicControl.empty()) {
                                    topicControl.first().state = newValue
                                } else {
                                    log.warn("Update failed: Was not able to find a control with the specified thing name.")
                                }
                            }
                        } else {
                            log.info("Skipping message as no new reported value was found.")
                        }
                    }
                } else {
                    log.info("Received a message, but was not able to find a thing name in the topic.")
                }
            }
            log.info("Listening to updates on AWS IoT topic ${control.toUpdateAcceptedTopicName()}")
        }
    }

    private fun Control.toIotPayload(): SdkBytes {
        return when (type) {
            ControlType.LIGHT_SWITCH, ControlType.IRRIGATION_VALVE -> {
                val payload = ControlThingPayload(
                    ControlThingState(
                        desired = ControlValue(state)
                    )
                )
                val payloadText = json.encodeToString(ControlThingPayload.serializer(), payload)
                log.info("Converted control with thing name $thingName to payload $payloadText")
                SdkBytes.fromUtf8String(payloadText)
            }
        }
    }

    private fun Control.toUpdateTopicName(): String {
        return "\$aws/things/$thingName/shadow/update"
    }

    private fun Control.toUpdateAcceptedTopicName(): String {
        return toUpdateTopicName() + "/accepted"
    }

    @Serializable
    data class ControlValue(val value: ControlState? = null)

    @Serializable
    data class ControlThingState(val desired: ControlValue? = null, val reported: ControlValue? = null)

    @Serializable
    data class ControlThingPayload(val state: ControlThingState)
}
