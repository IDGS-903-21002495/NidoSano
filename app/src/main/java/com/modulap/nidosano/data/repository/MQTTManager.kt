package com.modulap.nidosano.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.nio.charset.StandardCharsets.UTF_8

object MQTTManagerHiveMQ {

    private lateinit var mqttClient: Mqtt5AsyncClient

    @RequiresApi(Build.VERSION_CODES.N)
    fun conectar(onMensaje: (topic: String, value: String) -> Unit) {
        mqttClient = MqttClient.builder()
            .useMqttVersion5()
            .serverHost("0c8ff25959a14816b3cfa2771b75e00a.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .buildAsync()

        mqttClient.connectWith()
            .simpleAuth()
            .username("hivemq.webclient.1752384307171")
            .password(UTF_8.encode("<20,.1>3FHRZByGzagqf"))
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    val topics = listOf("temperature", "humidity", "air_quality", "lighting_level")

                    topics.forEach {
                        mqttClient.subscribeWith()
                            .topicFilter(it)
                            .qos(MqttQos.AT_LEAST_ONCE)
                            .send()
                    }

                    mqttClient.publishes(MqttGlobalPublishFilter.ALL) { mensaje: Mqtt5Publish ->
                        val payload = mensaje.payload.map { buffer ->
                            val readOnlyCopy = buffer.asReadOnlyBuffer()
                            val bytes = ByteArray(readOnlyCopy.remaining())
                            readOnlyCopy.get(bytes)
                            String(bytes, UTF_8)
                        }.orElse("Sin datos")

                        onMensaje(mensaje.topic.toString(), payload)
                    }
                } else {
                    onMensaje("error", "Error de conexión: ${throwable.message}")
                }
            }
    }

    fun desconectar() {
        if (::mqttClient.isInitialized) {
            mqttClient.disconnect()
        }
    }
}
