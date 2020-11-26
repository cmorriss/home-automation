package io.morrissey.iot.server.aws

import com.amazonaws.services.iot.client.AWSIotMessage
import com.amazonaws.services.iot.client.AWSIotMqttClient
import com.amazonaws.services.iot.client.AWSIotTopic
import com.google.gson.Gson
import io.morrissey.iot.server.log
import io.morrissey.iot.server.model.Control
import io.morrissey.iot.server.model.ControlState
import io.morrissey.iot.server.model.ControlType
import io.morrissey.iot.server.model.Controls
import org.jetbrains.exposed.sql.transactions.transaction
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Controller @Inject constructor(
    private val iotDataPlaneClient: IotDataPlaneClient, private val mqttClient: AWSIotMqttClient
) {
    private val updateAcceptedPattern = Regex("\\\$aws/things/(.*?)/shadow/update/accepted").toPattern()
    private val gson = Gson()

    init {
        mqttClient.connect()
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
            val payload = gson.fromJson(shadow, ControlThingPayload::class.java)
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
            mqttClient.subscribe(control.toUpdateAcceptedTopic())
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
                val payloadText = gson.toJson(payload)
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

    private fun Control.toUpdateAcceptedTopic(): AWSIotTopic {
        return object : AWSIotTopic(toUpdateAcceptedTopicName()) {
            override fun onMessage(message: AWSIotMessage?) {
                val messageTopic = message?.topic
                val payload = message?.stringPayload
                log.info("Received message from AWS, topic = $messageTopic, payload = \n$payload")
                if (messageTopic != null && payload != null) {
                    val thingNameMatcher = updateAcceptedPattern.matcher(messageTopic)
                    if (thingNameMatcher.find()) {
                        val thingName = thingNameMatcher.group(1)
                        val newState = gson.fromJson(payload, ControlThingPayload::class.java)
                        val newValue = newState?.state?.reported?.value
                        if (newValue != null) {
                            log.info("Updating control $thingName to value $newValue.")
                            transaction {
                                val control = Control.find { Controls.thingName eq thingName }
                                if (!control.empty()) {
                                    control.first().state = newValue
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
        }
    }

    data class ControlValue(val value: ControlState? = null)
    data class ControlThingState(val desired: ControlValue? = null, val reported: ControlValue? = null)
    data class ControlThingPayload(val state: ControlThingState)
}
